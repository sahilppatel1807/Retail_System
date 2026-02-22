package com.inventory.order_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.order_service.dto.PurchaseRequest;
import com.inventory.order_service.entity.InventoryItem;
import com.inventory.order_service.entity.OrderItem;
import com.inventory.order_service.service.OrderService;
import com.inventory.order_service.service.OrderItemService;
import com.inventory.order_service.service.OrderProducer;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/order-service")
@Slf4j
public class OrderController {

    private final OrderService service;
    private final OrderItemService orderItemService;
    private final OrderProducer orderProducer;

    public OrderController(OrderService service, OrderItemService orderItemService, OrderProducer orderProducer) {
        this.service = service;
        this.orderItemService = orderItemService;
        this.orderProducer = orderProducer;
    }

    /**
     * Routes a purchase request to available warehouses
     */
    @PostMapping("/purchase")
    public ResponseEntity<OrderItem> purchase(@Valid @RequestBody PurchaseRequest request) {
        log.info("\n📨 Received purchase request:");
        log.info("   Product ID: " + request.getProductId());
        log.info("   Quantity: " + request.getQuantity());
        log.info("   Retailer ID: " + request.getRetailerId());
        
        OrderItem orderItem = orderItemService.createOrder(
            request.getRetailerId(),
            request.getProductId(),
            null, // productName will be fetched by service
            request.getQuantity(),
            null  // warehouseId will be selected by router
        );
        
        log.info("✅ Order {} accepted and sent for routing\n", orderItem.getOrderId());
        
        return ResponseEntity.ok(orderItem);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running");
    }
    
    /**
     * Debug endpoint to see current cache state
     */
    @GetMapping("/inventory")
    public ResponseEntity<Map<Long, List<InventoryItem>>> getInventory() {
        return ResponseEntity.ok(service.getInventorySnapshot());
    }

    @PostMapping("/test-order")
    public ResponseEntity<OrderItem> createTestOrder() {
        log.info("\n📨 added to the database:");
        OrderItem orderItem = orderItemService.createOrder(
            1L,              // retailerId
            5L,              // productId
            "Laptop",        // productName
            10,              // quantity
            1L               // warehouseId
        );
        
        return ResponseEntity.ok(orderItem);
    }
    
    /**
     * Get all orders
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderItem>> getAllOrders() {
        return ResponseEntity.ok(orderItemService.getAllOrders());
    }
    
    /**
     * Get order by order ID
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderItem> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderItemService.getOrder(orderId));
    }

    @PostMapping
    public String sendOrder(@RequestBody String order) {
        orderProducer.sendOrderMessage(order);
        return "Order sent to RabbitMQ!";
    }
}
