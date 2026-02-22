package com.inventory.order_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.inventory.order_service.config.WarehouseConfig;
import com.inventory.order_service.config.WarehouseConfig.WarehouseInfo;
import com.inventory.order_service.entity.InventoryItem;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InventoryService {

    // In-memory cache: Key = productId, Value = List of warehouses that have it
    private final Map<Long, List<InventoryItem>> inventoryCache = new ConcurrentHashMap<>();

    private final WarehouseConfig warehouseConfig;

    public InventoryService(WarehouseConfig warehouseConfig) {
        this.warehouseConfig = warehouseConfig;
    }

    /**
     * Update or add product stock in cache
     */
    public void updateProductStock(Long productId, String productName, Long warehouseId, int newStock, float price) {
        
        // Get or create list for this product
        List<InventoryItem> warehouseList = inventoryCache.computeIfAbsent(productId, k -> new ArrayList<>());

        // Find if this warehouse already has this product in cache
        InventoryItem existing = warehouseList.stream()
                .filter(item -> item.getWarehouseId().equals(warehouseId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // Update existing entry
            existing.setStockOnHand(newStock);
            existing.setPrice(price);
            existing.setProductName(productName);
            log.info("📝 Updated cache: Product " + productId + " in Warehouse " + warehouseId + " → " + newStock + " units");
        } else {
            // Add new entry
            InventoryItem newItem = new InventoryItem(productId, productName, warehouseId, newStock, price);
            warehouseList.add(newItem);
            log.info("➕ Added to cache: Product " + productId + " in Warehouse " + warehouseId + " → " + newStock + " units");
        }
    }

    /**
     * Find warehouses that have the product with sufficient stock
     * Returns them sorted by stock level (highest first)
     */
    public List<WarehouseInfo> findWarehousesWithStock(Long productId, int requiredQuantity) {
        List<InventoryItem> inventoryItems = inventoryCache.get(productId);

        if (inventoryItems == null || inventoryItems.isEmpty()) {
            log.info("⚠️ Product " + productId + " not found in cache");
            return new ArrayList<>();
        }

        // Filter warehouses with enough stock and sort by stock level
        List<WarehouseInfo> result = new ArrayList<>();
        
        inventoryItems.stream()
                .filter(item -> item.getStockOnHand() >= requiredQuantity)
                .sorted((a, b) -> Integer.compare(b.getStockOnHand(), a.getStockOnHand()))  // Highest stock first
                .forEach(item -> {
                    // Find warehouse info
                    warehouseConfig.getList().stream()
                            .filter(w -> w.getId().equals(item.getWarehouseId()))
                            .findFirst()
                            .ifPresent(result::add);
                });

        return result;
    }

    /**
     * Get all warehouses
     */
    public List<WarehouseInfo> getAllWarehouses() {
        return warehouseConfig.getList();
    }

    /**
     * Remove warehouse from product's available list (when out of stock)
     */
    public void markWarehouseOutOfStock(Long productId, Long warehouseId) {
        List<InventoryItem> items = inventoryCache.get(productId);
        
        if (items != null) {
            items.removeIf(item -> item.getWarehouseId().equals(warehouseId));
            log.info("🚫 Marked Warehouse " + warehouseId + " as out of stock for Product " + productId);
        }
    }

    /**
     * Get product name from cache by productId
     */
    public String getProductName(Long productId) {
        List<InventoryItem> items = inventoryCache.get(productId);
        if (items != null && !items.isEmpty()) {
            return items.get(0).getProductName();
        }
        return "Unknown Product";
    }

    /**
     * Get price from cache by productId
     */
    public float getPrice(Long productId) {
        List<InventoryItem> items = inventoryCache.get(productId);
        if (items != null && !items.isEmpty()) {
            return items.get(0).getPrice();
        }
        return 0.0f;
    }

    /**
     * Get current inventory snapshot (for debugging/monitoring)
     */
    public Map<Long, List<InventoryItem>> getInventorySnapshot() {
        return new ConcurrentHashMap<>(inventoryCache);
    }
}