package com.inventory.warehouse.controller;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.warehouse.entity.Item;
import com.inventory.warehouse.entity.PendingOrder;
import com.inventory.warehouse.entity.WarehouseInventoryHistory;
import com.inventory.warehouse.messaging.StatusUpdateProducer;
import com.inventory.warehouse.repository.PendingOrderRepository;
import com.inventory.warehouse.service.AuthService;
import com.inventory.warehouse.service.ItemService;

@RestController
@RequestMapping("/api/warehouse")
@CrossOrigin(origins = "*")  // Allow React to connect
public class ItemController {
    private final ItemService service;
    private final AuthService authService;
    private final RabbitTemplate rabbitTemplate;
    private final PendingOrderRepository pendingOrderRepository;
    private final StatusUpdateProducer statusUpdateProducer;

    public ItemController(ItemService service, AuthService authService, RabbitTemplate rabbitTemplate, PendingOrderRepository pendingOrderRepository, StatusUpdateProducer statusUpdateProducer) {
        this.service = service;
        this.authService = authService;
        this.rabbitTemplate = rabbitTemplate;
        this.pendingOrderRepository = pendingOrderRepository;
        this.statusUpdateProducer = statusUpdateProducer;
    }

    @PostMapping("/create")
    public Item create(@RequestBody Item item) {
        return service.createItem(item);
    }

    @GetMapping("/all")
    public List<Item> getAllItem() {
        return service.getAllItems();
    }

    @GetMapping("/{id}")
    public Item getItem(@PathVariable Long id) {
        return service.findItem(id);
    }

    @PostMapping("/buy")
    public Item buyItem(
            @RequestParam Long retailerId,
            @RequestParam Long itemId,
            @RequestParam int quantity) {
        return service.sellItem(retailerId, itemId, quantity);
    }

    @PutMapping("/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item item) {
        return service.updateItem(id, item);
    }

    /**
     * Get inventory history for a specific product
     */
    @GetMapping("/history/product/{productId}")
    public List<WarehouseInventoryHistory> getProductHistory(@PathVariable Long productId) {
        return service.getProductHistory(productId);
    }
    
    /**
     * Get all inventory history for this warehouse
     */
    @GetMapping("/history")
    public List<WarehouseInventoryHistory> getWarehouseHistory() {
        return service.getWarehouseHistory();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<PendingOrder>> getOrders(
            @RequestHeader("Authorization") String authHeader) {
        
        // Validate token
        String token = authHeader.replace("Bearer ", "");
        Long warehouseId = authService.validateToken(token);
        
        // Get pending orders from database
        List<PendingOrder> orders = pendingOrderRepository.findByWarehouseId(warehouseId);
        System.out.println("📋 Found " + orders.size() + " pending orders for warehouse " + warehouseId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/orders/{orderId}/fulfill")
    public ResponseEntity<String> fulfillOrder(
            @PathVariable String orderId,
            @RequestHeader("Authorization") String authHeader) {
        
        System.out.println("🔧 Processing fulfillment for order: " + orderId);
        
        // Validate token
        String token = authHeader.replace("Bearer ", "");
        Long warehouseId = authService.validateToken(token);
        
        // Get pending order
        PendingOrder order = pendingOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Verify it belongs to this warehouse
        if (!order.getWarehouseId().equals(warehouseId)) {
            return ResponseEntity.status(403).body("❌ Not your order!");
        }
        
        try {
            // Process the sale
            service.sellItem(order.getRetailerId(), order.getProductId(), order.getQuantity());
            
            // Send success status
            statusUpdateProducer.sendStatusUpdate(
                new com.inventory.warehouse.dto.OrderUpdateDTO(
                    orderId, 
                    "COMPLETED", 
                    "Order fulfilled by warehouse " + warehouseId, 
                    0f
                )
            );
            
            // Delete from pending
            pendingOrderRepository.delete(order);
            
            System.out.println("✅ Order " + orderId + " fulfilled successfully");
            
            return ResponseEntity.ok("✅ Order fulfilled successfully");
            
        } catch (Exception e) {
            System.out.println("❌ Failed to fulfill order: " + e.getMessage());
            
            // Send failed status
            statusUpdateProducer.sendStatusUpdate(
                new com.inventory.warehouse.dto.OrderUpdateDTO(
                    orderId, 
                    "FAILED", 
                    e.getMessage(), 
                    0f
                )
            );
            
            return ResponseEntity.status(500).body("❌ Failed: " + e.getMessage());
        }
    }





}
