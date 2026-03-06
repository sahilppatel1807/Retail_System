package com.inventory.warehouse.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pending_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String orderId;
    
    private Long productId;
    private String productName;
    private int quantity;
    private Long retailerId;
    private Long warehouseId;
    
    private LocalDateTime receivedAt;
}
