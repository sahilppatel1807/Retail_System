package com.inventory.warehouse_central.controller;

import com.inventory.warehouse_central.dto.ItemResponse;
import com.inventory.warehouse_central.dto.PurchaseRequest;
import com.inventory.warehouse_central.model.InventoryItem;
import com.inventory.warehouse_central.service.CentralWarehouseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse-central")
public class CentralWarehouseController {

    private final CentralWarehouseService service;

    public CentralWarehouseController(CentralWarehouseService service) {
        this.service = service;
    }

    /**
     * Routes a purchase request to available warehouses
     */
    @PostMapping("/purchase")
    public ResponseEntity<ItemResponse> purchase(@Valid @RequestBody PurchaseRequest request) {
        System.out.println("\n📨 Received purchase request:");
        System.out.println("   Product ID: " + request.getProductId());
        System.out.println("   Quantity: " + request.getQuantity());
        System.out.println("   Retailer ID: " + request.getRetailerId());
        
        ItemResponse response = service.routePurchase(request);
        
        System.out.println("✅ Purchase completed - returned to retailer\n");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Warehouse Central is running");
    }
    
    /**
     * Debug endpoint to see current cache state
     */
    @GetMapping("/inventory")
    public ResponseEntity<Map<Long, List<InventoryItem>>> getInventory() {
        return ResponseEntity.ok(service.getInventorySnapshot());
    }
}