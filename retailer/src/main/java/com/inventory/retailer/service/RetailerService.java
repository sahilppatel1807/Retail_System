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
import com.inventory.retailer.entity.OrderTracking;
import com.inventory.retailer.repository.OrderTrackingRepository;
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
    private final OrderTrackingRepository orderTrackingRepository;
    private final WarehouseClient warehouseClient;

    @Value("${retailer.id}")
    private Long retailerId;

    public RetailerService(
            PurchaseRepository purchaseRepository,
            SaleRepository saleRepository,
            RetailerInventoryRepository inventoryRepository,
            RetailerInventoryHistoryRepository inventoryHistoryRepository,
            OrderTrackingRepository orderTrackingRepository,
            WarehouseClient warehouseClient) {
        this.purchaseRepository = purchaseRepository;
        this.saleRepository = saleRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
        this.orderTrackingRepository = orderTrackingRepository;
        this.warehouseClient = warehouseClient;
    }

    /**
     * Buy items from warehouse via order-service (Async Flow)
     */
    @Transactional
    public OrderTracking buyFromWarehouse(Long itemId, int quantity) {
        log.info("\n🛒 [Retailer-" + retailerId + "] Requesting buy from warehouse:");
        log.info("   Item ID: " + itemId);
        log.info("   Quantity: " + quantity);

        // Call order-service to initiate purchase
        ItemResponse itemResponse = warehouseClient.buyFromWarehouse(retailerId, itemId, quantity);

        log.info("✅ Order Accepted with ID: " + itemResponse.getOrderId());

        // Create OrderTracking record
        OrderTracking tracking = new OrderTracking();
        tracking.setOrderId(itemResponse.getOrderId());
        tracking.setProductId(itemId);
        tracking.setProductName(itemResponse.getProductName());
        tracking.setQuantity(quantity);
        tracking.setStatus(itemResponse.getStatus());
        tracking.setPrice(itemResponse.getPrice());
        
        return orderTrackingRepository.save(tracking);
    }

    /**
     * Fulfill the purchase when order is COMPLETED
     */
    @Transactional
    public void fulfillPurchase(String orderId, float price) {
        OrderTracking tracking = orderTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order tracking not found for: " + orderId));

        if ("COMPLETED".equals(tracking.getStatus())) {
            log.info("⚠️ Order {} already fulfilled", orderId);
            return;
        }

        log.info("🎊 Fulfilling order {}: {} x {} @ ${}", orderId, tracking.getProductName(), tracking.getQuantity(), price);

        // Update tracking price
        tracking.setPrice(price);

        // STEP 1: Create Purchase record (transaction history)
        Purchase purchase = new Purchase();
        purchase.setRetailerId(retailerId);
        purchase.setProductName(tracking.getProductName());
        purchase.setPrice(price);
        purchase.setQuantity(tracking.getQuantity());
        purchase.setWarehouseItemId(tracking.getProductId()); // Using internal product ID

        Purchase savedPurchase = purchaseRepository.save(purchase);
        
        // STEP 2: Update RetailerInventory (current stock)
        updateInventoryAfterPurchase(
                tracking.getProductId(),
                tracking.getProductName(),
                tracking.getQuantity(),
                price,
                savedPurchase.getId()
        );

        // STEP 3: Update tracking status
        tracking.setStatus("COMPLETED");
        orderTrackingRepository.save(tracking);
        
        log.info("✅ Order {} fulfilled and added to inventory", orderId);
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
        sale.setSellingPrice(inventory.getAveragePurchasePrice() * 1.15f); // 15% markup
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

    /**
     * Get order tracking by order ID (Customized Response)
     */
    public com.inventory.retailer.dto.OrderTrackingResponse getOrderTrackingCustom(String orderId) {
        OrderTracking tracking = orderTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order tracking not found for: " + orderId));
        
        return new com.inventory.retailer.dto.OrderTrackingResponse(
            tracking.getId(),
            tracking.getOrderId(),
            tracking.getStatus(),
            tracking.getCreatedAt(),
            tracking.getUpdatedAt()
        );
    }

    /**
     * Get all order tracking records (Customized Response)
     */
    public List<com.inventory.retailer.dto.OrderTrackingResponse> getAllTracking() {
        return orderTrackingRepository.findAll().stream()
                .map(tracking -> new com.inventory.retailer.dto.OrderTrackingResponse(
                        tracking.getId(),
                        tracking.getOrderId(),
                        tracking.getStatus(),
                        tracking.getCreatedAt(),
                        tracking.getUpdatedAt()
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get order tracking by order ID
     */
    public OrderTracking getOrderTracking(String orderId) {
        return orderTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order tracking not found for: " + orderId));
    }
}