package com.inventory.order_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.inventory.order_service.entity.OrderItem;
import com.inventory.order_service.repository.OrderItemRepository;

@Service
public class OrderItemService {
    
    private final OrderItemRepository orderItemRepository;
    private final OrderProducer orderProducer;
    private final InventoryService inventoryService;
    
    public OrderItemService(OrderItemRepository orderItemRepository, OrderProducer orderProducer, InventoryService inventoryService) {
        this.orderItemRepository = orderItemRepository;
        this.orderProducer = orderProducer;
        this.inventoryService = inventoryService;
    }
    
    /**
     * Create a new order with ACCEPTED status
     */
    public OrderItem createOrder(
            Long retailerId,
            Long productId,
            String productName,
            int quantity,
            Long warehouseId
    ) {
        OrderItem orderItem = new OrderItem();
        
        // Generate unique order ID
        orderItem.setOrderId(generateOrderId());
        
        // Generate reference ID for retailer
        orderItem.setReferenceId(generateReferenceId());
        
        // Set order details
        orderItem.setRetailerId(retailerId);
        orderItem.setProductId(productId);
        
        // Fetch product name if not provided
        if (productName == null || productName.isEmpty()) {
            orderItem.setProductName(inventoryService.getProductName(productId));
            orderItem.setPrice(inventoryService.getPrice(productId));
        } else {
            orderItem.setProductName(productName);
            // If productName is provided, we might still need the price from cache
            orderItem.setPrice(inventoryService.getPrice(productId));
        }
        
        orderItem.setQuantity(quantity);
        orderItem.setWarehouseId(warehouseId);
        
        // Set status
        orderItem.setStatus("ACCEPTED");
        
        // Set timestamps
        orderItem.setCreatedAt(LocalDateTime.now());
        
        // Save to database first to ensure it exists before routing
        OrderItem savedOrder = orderItemRepository.save(orderItem);

        // Send to RabbitMQ
        orderProducer.sendOrderMessage(savedOrder);
        
        return savedOrder;
    }
    
    /**
     * Update order status
     */
    public OrderItem updateOrderStatus(String orderId, String newStatus) {
        OrderItem orderItem = orderItemRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        orderItem.setStatus(newStatus);
        orderItem.setUpdatedAt(LocalDateTime.now());
        
        // If completed, set completion timestamp
        if ("COMPLETED".equals(newStatus)) {
            orderItem.setCompletedAt(LocalDateTime.now());
        }
        
        return orderItemRepository.save(orderItem);
    }
    
    /**
     * Get order by order ID
     */
    public OrderItem getOrder(String orderId) {
        return orderItemRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }
    
    /**
     * Get all orders with specific status
     */
    public List<OrderItem> getOrdersByStatus(String status) {
        return orderItemRepository.findByStatus(status);
    }
    
    /**
     * Get all orders for a retailer
     */
    public List<OrderItem> getRetailerOrders(Long retailerId) {
        return orderItemRepository.findByRetailerId(retailerId);
    }
    
    /**
     * Get all orders
     */
    public List<OrderItem> getAllOrders() {
        return orderItemRepository.findAll();
    }
    
    /**
     * Add notes to order (for error messages, etc.)
     */
    public OrderItem addNotes(String orderId, String notes) {
        OrderItem orderItem = getOrder(orderId);
        orderItem.setNotes(notes);
        return orderItemRepository.save(orderItem);
    }
    
    /**
     * Generate unique order ID
     * Format: ORD-YYYY-MM-DD-XXXXX
     */
    private String generateOrderId() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = String.format("%d-%02d-%02d", 
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + uniquePart;
    }
    
    /**
     * Generate reference ID for retailer
     * Format: REF-XXXXX
     */
    private String generateReferenceId() {
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "REF-" + uniquePart;
    }
}