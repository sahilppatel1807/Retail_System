package com.inventory.retailer.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inventory.retailer.client.WarehouseClient;
import com.inventory.retailer.dto.ItemResponse;
import com.inventory.retailer.entity.Purchase;
import com.inventory.retailer.entity.RetailerInventory;
import com.inventory.retailer.entity.RetailerInventoryHistory;
import com.inventory.retailer.entity.Sale;
import com.inventory.retailer.repository.PurchaseRepository;
import com.inventory.retailer.repository.RetailerInventoryHistoryRepository;
import com.inventory.retailer.repository.RetailerInventoryRepository;
import com.inventory.retailer.repository.SaleRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RetailerService {

    private final PurchaseRepository purchaseRepository;
    private final SaleRepository saleRepository;
    private final RetailerInventoryRepository inventoryRepository;
    private final RetailerInventoryHistoryRepository inventoryHistoryRepository;
    private final WarehouseClient warehouseClient;

    @Value("${retailer.id}")
    private Long retailerId;

    public RetailerService(
            PurchaseRepository purchaseRepository,
            SaleRepository saleRepository,
            RetailerInventoryRepository inventoryRepository,
            RetailerInventoryHistoryRepository inventoryHistoryRepository,
            WarehouseClient warehouseClient) {
        this.purchaseRepository = purchaseRepository;
        this.saleRepository = saleRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
        this.warehouseClient = warehouseClient;
    }

    /**
     * Buy items from warehouse via warehouse-central
     */
    @Transactional
    public Purchase buyFromWarehouse(Long itemId, int quantity) {
        log.info("\n🛒 [Retailer-" + retailerId + "] Buying from warehouse:");
        log.info("   Item ID: " + itemId);
        log.info("   Quantity: " + quantity);

        // Call warehouse-central to buy item
        ItemResponse itemResponse = warehouseClient.buyFromWarehouse(retailerId, itemId, quantity);

        log.info("✅ Received from Warehouse-" + itemResponse.getWarehouseId());

        // STEP 1: Create Purchase record (transaction history)
        Purchase purchase = new Purchase();
        purchase.setRetailerId(retailerId);
        purchase.setWarehouseId(itemResponse.getWarehouseId());
        purchase.setWarehouseItemId(itemResponse.getId());
        purchase.setProductName(itemResponse.getProductName());
        purchase.setPrice(itemResponse.getPrice());
        purchase.setQuantity(quantity);

        Purchase savedPurchase = purchaseRepository.save(purchase);
        log.info("📝 Purchase record created (ID: " + savedPurchase.getId() + ")");

        // STEP 2: Update RetailerInventory (current stock)
        updateInventoryAfterPurchase(
                itemResponse.getId(),
                itemResponse.getProductName(),
                quantity,
                itemResponse.getPrice(),
                savedPurchase.getId()
        );

        return savedPurchase;
    }

    /**
     * Place order (sell to customer)
     */
    @Transactional
    public Sale placeOrder(Long productId, int quantity, String customerName) {
        log.info("\n💰 [Retailer-" + retailerId + "] Selling to customer:");
        log.info("   Product ID: " + productId);
        log.info("   Quantity: " + quantity);
        log.info("   Customer: " + customerName);

        // STEP 1: Check if we have the product in inventory
        RetailerInventory inventory = inventoryRepository
                .findByRetailerIdAndProductId(retailerId, productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));

        // STEP 2: Check if we have enough stock
        if (inventory.getQuantityOnHand() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock. Available: " + inventory.getQuantityOnHand() +
                            ", Requested: " + quantity
            ); 
        }

        // STEP 3: Create Sale record (transaction history)
        Sale sale = new Sale();
        sale.setRetailerId(retailerId);
        sale.setProductId(productId);
        sale.setProductName(inventory.getProductName());
        sale.setQuantitySold(quantity);
        sale.setSellingPrice(inventory.getAveragePurchasePrice() * 1.2f); // 20% markup
        sale.setCustomerName(customerName);
        sale.setSaleDate(LocalDateTime.now());

        Sale savedSale = saleRepository.save(sale);
        log.info("📝 Sale record created (ID: " + savedSale.getId() + ")");

        // STEP 4: Update RetailerInventory (reduce stock)
        updateInventoryAfterSale(productId, quantity, savedSale.getId());

        return savedSale;
    }

    /**
     * Update inventory after purchasing from warehouse
     */
    private void updateInventoryAfterPurchase(
            Long productId,
            String productName,
            int quantity,
            float price,
            Long purchaseId
    ) {
        RetailerInventory inventory = inventoryRepository
                .findByRetailerIdAndProductId(retailerId, productId)
                .orElse(null);

        int stockBefore;
        int stockAfter;
        float newAveragePrice;

        if (inventory == null) {
            // New product - create inventory record
            stockBefore = 0;
            stockAfter = quantity;
            newAveragePrice = price;

            inventory = new RetailerInventory();
            inventory.setRetailerId(retailerId);
            inventory.setProductId(productId);
            inventory.setProductName(productName);
            inventory.setQuantityOnHand(stockAfter);
            inventory.setAveragePurchasePrice(newAveragePrice);
            inventory.setLastUpdated(LocalDateTime.now());

            log.info("➕ Created new inventory entry");

        } else {
            // Existing product - update inventory
            stockBefore = inventory.getQuantityOnHand();
            stockAfter = stockBefore + quantity;

            // Calculate new average price
            // Formula: (old_stock * old_price + new_quantity * new_price) / total_stock
            float oldTotalCost = stockBefore * inventory.getAveragePurchasePrice();
            float newTotalCost = quantity * price;
            newAveragePrice = (oldTotalCost + newTotalCost) / stockAfter;

            inventory.setQuantityOnHand(stockAfter);
            inventory.setAveragePurchasePrice(newAveragePrice);
            inventory.setLastUpdated(LocalDateTime.now());

            log.info("📝 Updated existing inventory");
        }

        inventoryRepository.save(inventory);

        log.info("   Stock: " + stockBefore + " → " + stockAfter);
        log.info("   Avg Price: $" + String.format("%.2f", newAveragePrice));

        // Record history
        recordInventoryHistory(
                productId,
                productName,
                "PURCHASED",
                quantity,
                price,
                stockBefore,
                stockAfter,
                purchaseId,
                "Purchased from warehouse"
        );
    }

    /**
     * Update inventory after selling to customer
     */
    private void updateInventoryAfterSale(Long productId, int quantity, Long saleId) {
        RetailerInventory inventory = inventoryRepository
                .findByRetailerIdAndProductId(retailerId, productId)
                .orElseThrow(() -> new RuntimeException("Product not in inventory"));

        int stockBefore = inventory.getQuantityOnHand();
        int stockAfter = stockBefore - quantity;

        inventory.setQuantityOnHand(stockAfter);
        inventory.setLastUpdated(LocalDateTime.now());

        inventoryRepository.save(inventory);

        log.info("📦 Updated inventory after sale");
        log.info("   Stock: " + stockBefore + " → " + stockAfter);

        // Record history
        recordInventoryHistory(
                productId,
                inventory.getProductName(),
                "SOLD",
                quantity,
                inventory.getAveragePurchasePrice(),
                stockBefore,
                stockAfter,
                saleId,
                "Sold to customer"
        );
    }

    /**
     * Record inventory history
     */
    private void recordInventoryHistory(
            Long productId,
            String productName,
            String transactionType,
            int quantity,
            float price,
            int stockBefore,
            int stockAfter,
            Long referenceId,
            String notes
    ) {
        RetailerInventoryHistory history = new RetailerInventoryHistory();
        history.setRetailerId(retailerId);
        history.setProductId(productId);
        history.setProductName(productName);
        history.setTransactionType(transactionType);
        history.setQuantity(quantity);
        history.setPriceAtTransaction(price);
        history.setStockBefore(stockBefore);
        history.setStockAfter(stockAfter);
        history.setReferenceId(referenceId);
        history.setTransactionDate(LocalDateTime.now());
        history.setNotes(notes);

        inventoryHistoryRepository.save(history);

        log.info("📊 Inventory history recorded: " + transactionType);
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get current inventory for this retailer
     */
    public List<RetailerInventory> getCurrentInventory() {
        return inventoryRepository.findByRetailerId(retailerId);
    }

    /**
     * Get inventory for a specific product
     */
    public RetailerInventory getProductInventory(Long productId) {
        return inventoryRepository
                .findByRetailerIdAndProductId(retailerId, productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not in inventory"));
    }

    /**
     * Get full inventory history for this retailer
     */
    public List<RetailerInventoryHistory> getInventoryHistory() {
        return inventoryHistoryRepository.findByRetailerIdOrderByTransactionDateDesc(retailerId);
    }

    /**
     * Get inventory history for a specific product
     */
    public List<RetailerInventoryHistory> getProductHistory(Long productId) {
        return inventoryHistoryRepository.findByProductIdOrderByTransactionDateDesc(productId);
    }

    /**
     * Get all purchases (buying history)
     */
    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    /**
     * Get all sales
     */
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    /**
     * Get specific sale
     */
    public Sale getSale(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found with id: " + id));
    }

    /**
     * Update purchase record
     */
    public Purchase updatePurchase(Long id, Purchase purchase) {
        Purchase existing = purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found with id: " + id));

        existing.setProductName(purchase.getProductName());
        existing.setPrice(purchase.getPrice());
        existing.setQuantity(purchase.getQuantity());

        return purchaseRepository.save(existing);
    }

    /**
     * Get all purchase items (legacy - for backward compatibility)
     */
    public List<Purchase> getAllItems() {
        return purchaseRepository.findAll();
    }

    /**
     * Find specific purchase item (legacy - for backward compatibility)
     */
    public Purchase findItem(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase not found with id: " + id));
    }
}