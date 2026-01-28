package com.inventory.retailer.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.inventory.retailer.client.WarehouseClient;
import com.inventory.retailer.dto.ItemResponse;
import com.inventory.retailer.entity.Purchase;
import com.inventory.retailer.entity.Sale;
import com.inventory.retailer.repository.PurchaseRepository;
import com.inventory.retailer.repository.SaleRepository;

@Service
public class RetailerService {

    private final WarehouseClient warehouseClient;
    private final SaleRepository saleRepository;
    private final PurchaseRepository repository;

    @Value("${retailer.id:0}")
    private Long retailerId;

    public RetailerService(PurchaseRepository repository, WarehouseClient warehouseClient, SaleRepository sellRepository) {
        this.repository = repository;
        this.warehouseClient = warehouseClient;
        this.saleRepository = sellRepository;
    }

    public Purchase buyFromWarehouse(Long warehouseId, Long itemId, int quantity) {

        // Use WarehouseClient to call warehouse service with warehouseId
        ItemResponse item = warehouseClient.buyFromWarehouse(warehouseId, retailerId, itemId, quantity);

        // Check if this product already exists in retailer inventory
        return repository.findByProductName(item.getProductName())
                .map(existingPurchase -> {
                    // Product exists - merge quantities and update price
                    existingPurchase.setQuantity(existingPurchase.getQuantity() + quantity);
                    existingPurchase.setPrice(item.getPrice()); // Update to latest price
                    return repository.save(existingPurchase);
                })
                .orElseGet(() -> {
                    // Product doesn't exist - create new
                    Purchase retailerItem = new Purchase();
                    retailerItem.setRetailerId(retailerId);  // Set from environment
                    retailerItem.setWarehouseId(warehouseId); // Track which warehouse
                    retailerItem.setWarehouseItemId(item.getId());
                    retailerItem.setProductName(item.getProductName());
                    retailerItem.setPrice(item.getPrice());
                    retailerItem.setQuantity(quantity);
                    return repository.save(retailerItem);
                });
    }

    public List<Purchase> getAllItems() {
        return repository.findAll();
    }

    public Purchase findItem(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found with id: " + id));
    }

    public Sale placeOrder(Long productId, int quantity, String customerName) {

        Purchase product = repository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }

        // reduce retailer stock
        product.setQuantity(product.getQuantity() - quantity);
        repository.save(product);

        // create sale record
        Sale sale = new Sale();
        sale.setRetailerId(retailerId);  // Set from environment
        sale.setProductId(productId);
        sale.setQuantity(quantity);
        sale.setCustomerName(customerName);
        sale.setSoldAt(LocalDateTime.now());

        return saleRepository.save(sale);
    }

    public Purchase updatePurchase(Long id, Purchase updatedPurchase) {
        Purchase existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found with id: " + id));
        
        existing.setWarehouseItemId(updatedPurchase.getWarehouseItemId());
        existing.setProductName(updatedPurchase.getProductName());
        existing.setPrice(updatedPurchase.getPrice());
        existing.setQuantity(updatedPurchase.getQuantity());
        
        return repository.save(existing);
    }

}
