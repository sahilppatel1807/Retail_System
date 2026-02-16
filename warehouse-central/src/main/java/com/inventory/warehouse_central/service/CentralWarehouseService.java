package com.inventory.warehouse_central.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.inventory.warehouse_central.client.WarehouseClient;
import com.inventory.warehouse_central.config.WarehouseConfig.WarehouseInfo;
import com.inventory.warehouse_central.dto.ItemResponse;
import com.inventory.warehouse_central.dto.PurchaseRequest;
import com.inventory.warehouse_central.model.InventoryItem;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CentralWarehouseService {

    private final InventoryService inventoryService;
    private final WarehouseClient warehouseClient;

    public CentralWarehouseService(
            InventoryService inventoryService,
            WarehouseClient warehouseClient
    ) {
        this.inventoryService = inventoryService;
        this.warehouseClient = warehouseClient;
    }

    public ItemResponse routePurchase(PurchaseRequest request) {
        log.info("\n🎯 Routing purchase request:");
        log.info("   Product ID: " + request.getProductId());
        log.info("   Quantity: " + request.getQuantity());
        log.info("   Retailer ID: " + request.getRetailerId());

        // STEP 1: Check cache for warehouses with stock
        List<WarehouseInfo> candidateWarehouses = inventoryService.findWarehousesWithStock(
                request.getProductId(),
                request.getQuantity()
        );

        if (!candidateWarehouses.isEmpty()) {
            log.info("✅ Found " + candidateWarehouses.size() + " warehouse(s) in cache");
            
            // Try warehouses from cache
            for (int i = 0; i < candidateWarehouses.size(); i++) {
                WarehouseInfo warehouse = candidateWarehouses.get(i);
                
                log.info("\n📍 Attempt " + (i + 1) + "/" + candidateWarehouses.size() + 
                                  ": Trying Warehouse " + warehouse.getId());

                try {
                    ItemResponse response = warehouseClient.buyItem(
                            warehouse,
                            request.getProductId(),
                            request.getQuantity(),
                            request.getRetailerId()
                    );

                    response.setWarehouseId(warehouse.getId());
                    
                    log.info("✅ SUCCESS! Purchased from Warehouse " + warehouse.getId());
                    
                    return response;

                } catch (Exception e) {
                    log.info("❌ Failed: " + e.getMessage());
                    
                    // Mark this warehouse as out of stock in cache
                    inventoryService.markWarehouseOutOfStock(
                            request.getProductId(),
                            warehouse.getId()
                    );
                    
                    log.info("   Trying next warehouse...");
                }
            }
        }

        // STEP 2: Cache failed - try all warehouses directly (fallback)
        log.info("\n⚠️ Cache didn't help. Trying all warehouses directly...");
        
        return tryAllWarehousesDirectly(request);
    }

    /**
     * Fallback: Try all warehouses one by one (old approach)
     */
    private ItemResponse tryAllWarehousesDirectly(PurchaseRequest request) {
        List<WarehouseInfo> allWarehouses = inventoryService.getAllWarehouses();
        
        for (WarehouseInfo warehouse : allWarehouses) {
            try {
                log.info("🔍 Trying Warehouse " + warehouse.getId() + " directly...");
                
                ItemResponse response = warehouseClient.buyItem(
                        warehouse,
                        request.getProductId(),
                        request.getQuantity(),
                        request.getRetailerId()
                );

                response.setWarehouseId(warehouse.getId());
                
                log.info("✅ SUCCESS! Purchased from Warehouse " + warehouse.getId());
                
                return response;

            } catch (Exception e) {
                log.info("❌ Warehouse " + warehouse.getId() + " failed: " + e.getMessage());
            }
        }

        // All warehouses failed
        log.info("\n❌ ALL WAREHOUSES FAILED");
        throw new RuntimeException(
                "Product " + request.getProductId() + " is not available in any warehouse"
        );
    }
    /**
     * Get inventory snapshot for debugging
     */
    public Map<Long, List<InventoryItem>> getInventorySnapshot() {
        return inventoryService.getInventorySnapshot();
    }
}