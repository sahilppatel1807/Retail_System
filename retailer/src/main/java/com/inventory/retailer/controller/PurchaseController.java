package com.inventory.retailer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.retailer.dto.PurchaseRequest;
import com.inventory.retailer.entity.Purchase;
import com.inventory.retailer.entity.RetailerInventory;
import com.inventory.retailer.entity.RetailerInventoryHistory;
import com.inventory.retailer.entity.Sale;
import com.inventory.retailer.service.RetailerService;

@RestController
@RequestMapping("/api/retailer")
public class PurchaseController {
    private final RetailerService service;

    public PurchaseController(RetailerService service) {
        this.service = service;
    }

    // ==================== PURCHASE ENDPOINTS ====================

    /**
     * Buy from warehouse
     */
    @PostMapping("/buy")
    public com.inventory.retailer.entity.OrderTracking buy(@RequestBody com.inventory.retailer.dto.PurchaseRequest request) {
        return service.buyFromWarehouse(request.getItemId(), request.getQuantity());
    }

    @GetMapping("/track/{orderId}")
    public com.inventory.retailer.dto.OrderTrackingResponse trackOrder(@PathVariable String orderId) {
        return service.getOrderTrackingCustom(orderId);
    }

    @GetMapping("/track/all")
    public List<com.inventory.retailer.dto.OrderTrackingResponse> getAllTracking() {
        return service.getAllTracking();
    }

    /**
     * Get all purchases (buying history from warehouse)
     */
    @GetMapping("/purchases")
    public List<Purchase> getAllPurchases() {
        return service.getAllPurchases();
    }

    /**
     * Update purchase record
     */
    @PutMapping("/purchases/{id}")
    public Purchase updatePurchase(@PathVariable Long id, @RequestBody Purchase purchase) {
        return service.updatePurchase(id, purchase);
    }

    // ==================== SALES ENDPOINTS ====================

    /**
     * Sell to customer (place order)
     */
    @PostMapping("/orders")
    public Sale placeOrder(
            @RequestParam Long productId,
            @RequestParam int quantity,
            @RequestParam String customerName) {
        return service.placeOrder(productId, quantity, customerName);
    }

    /**
     * Get all sales
     */
    @GetMapping("/sales")
    public List<Sale> getAllSales() {
        return service.getAllSales();
    }

    /**
     * Get specific sale
     */
    @GetMapping("/sales/{id}")
    public Sale getSale(@PathVariable Long id) {
        return service.getSale(id);
    }

    // ==================== INVENTORY ENDPOINTS ====================

    /**
     * Get current inventory (what's in stock right now)
     */
    @GetMapping("/inventory")
    public List<RetailerInventory> getCurrentInventory() {
        return service.getCurrentInventory();
    }

    /**
     * Get inventory for a specific product
     */
    @GetMapping("/inventory/product/{productId}")
    public RetailerInventory getProductInventory(@PathVariable Long productId) {
        return service.getProductInventory(productId);
    }

    /**
     * Get full inventory history (all movements)
     */
    @GetMapping("/inventory/history")
    public List<RetailerInventoryHistory> getInventoryHistory() {
        return service.getInventoryHistory();
    }

    /**
     * Get inventory history for a specific product
     */
    @GetMapping("/inventory/history/product/{productId}")
    public List<RetailerInventoryHistory> getProductHistory(@PathVariable Long productId) {
        return service.getProductHistory(productId);
    }

    // ==================== LEGACY ENDPOINTS (Backward Compatibility) ====================

    /**
     * Get all items (legacy - returns purchases)
     */
    @GetMapping("/all")
    public List<Purchase> getAllItems() {
        return service.getAllItems();
    }

    /**
     * Get item by ID (legacy - returns purchase)
     */
    @GetMapping("/{id}")
    public Purchase getItem(@PathVariable Long id) {
        return service.findItem(id);
    }

}
