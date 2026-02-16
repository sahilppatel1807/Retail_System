package com.inventory.warehouse_central.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.warehouse_central.dto.ItemResponse;
import com.inventory.warehouse_central.dto.PurchaseRequest;
import com.inventory.warehouse_central.model.InventoryItem;
import com.inventory.warehouse_central.service.CentralWarehouseService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/warehouse-central")
@Slf4j
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
        log.info("\n📨 Received purchase request:");
        log.info("   Product ID: " + request.getProductId());
        log.info("   Quantity: " + request.getQuantity());
        log.info("   Retailer ID: " + request.getRetailerId());
        
        ItemResponse response = service.routePurchase(request);
        
        log.info("✅ Purchase completed - returned to retailer\n");
        
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