package com.inventory.order_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderId;  // ORD-2025-02-15-12345
    
    private String referenceId;  // REF-67890 (for retailer tracking)
    
    private Long retailerId;
    private Long productId;
    private String productName;
    private int quantity;
    
    private Long warehouseId;  // Which warehouse will fulfill this
    
    @Column(nullable = false)
    private String status;  // ACCEPTED, ROUTED, PROCESSING, COMPLETED, FAILED
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private float price;
    
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    private String notes;  
}
