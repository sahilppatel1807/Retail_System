package com.inventory.warehouse_central.service;

import com.inventory.warehouse_central.client.WarehouseClient;
import com.inventory.warehouse_central.config.WarehouseConfig.WarehouseInfo;
import com.inventory.warehouse_central.dto.ItemResponse;
import com.inventory.warehouse_central.dto.PurchaseRequest;
import com.inventory.warehouse_central.model.InventoryItem;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
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
        System.out.println("\n🎯 Routing purchase request:");
        System.out.println("   Product ID: " + request.getProductId());
        System.out.println("   Quantity: " + request.getQuantity());
        System.out.println("   Retailer ID: " + request.getRetailerId());

        // STEP 1: Check cache for warehouses with stock
        List<WarehouseInfo> candidateWarehouses = inventoryService.findWarehousesWithStock(
                request.getProductId(),
                request.getQuantity()
        );

        if (!candidateWarehouses.isEmpty()) {
            System.out.println("✅ Found " + candidateWarehouses.size() + " warehouse(s) in cache");
            
            // Try warehouses from cache
            for (int i = 0; i < candidateWarehouses.size(); i++) {
                WarehouseInfo warehouse = candidateWarehouses.get(i);
                
                System.out.println("\n📍 Attempt " + (i + 1) + "/" + candidateWarehouses.size() + 
                                  ": Trying Warehouse " + warehouse.getId());

                try {
                    ItemResponse response = warehouseClient.buyItem(
                            warehouse,
                            request.getProductId(),
                            request.getQuantity(),
                            request.getRetailerId()
                    );

                    response.setWarehouseId(warehouse.getId());
                    
                    System.out.println("✅ SUCCESS! Purchased from Warehouse " + warehouse.getId());
                    
                    return response;

                } catch (Exception e) {
                    System.out.println("❌ Failed: " + e.getMessage());
                    
                    // Mark this warehouse as out of stock in cache
                    inventoryService.markWarehouseOutOfStock(
                            request.getProductId(),
                            warehouse.getId()
                    );
                    
                    System.out.println("   Trying next warehouse...");
                }
            }
        }

        // STEP 2: Cache failed - try all warehouses directly (fallback)
        System.out.println("\n⚠️ Cache didn't help. Trying all warehouses directly...");
        
        return tryAllWarehousesDirectly(request);
    }

    /**
     * Fallback: Try all warehouses one by one (old approach)
     */
    private ItemResponse tryAllWarehousesDirectly(PurchaseRequest request) {
        List<WarehouseInfo> allWarehouses = inventoryService.getAllWarehouses();
        
        for (WarehouseInfo warehouse : allWarehouses) {
            try {
                System.out.println("🔍 Trying Warehouse " + warehouse.getId() + " directly...");
                
                ItemResponse response = warehouseClient.buyItem(
                        warehouse,
                        request.getProductId(),
                        request.getQuantity(),
                        request.getRetailerId()
                );

                response.setWarehouseId(warehouse.getId());
                
                System.out.println("✅ SUCCESS! Purchased from Warehouse " + warehouse.getId());
                
                return response;

            } catch (Exception e) {
                System.out.println("❌ Warehouse " + warehouse.getId() + " failed: " + e.getMessage());
            }
        }

        // All warehouses failed
        System.out.println("\n❌ ALL WAREHOUSES FAILED");
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