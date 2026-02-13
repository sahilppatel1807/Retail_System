package com.inventory.retailer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "retailer_inventory_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetailerInventoryHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long retailerId;
    private Long productId;
    private String productName;
    
    @Column(nullable = false)
    private String transactionType;  // PURCHASED, SOLD
    
    private int quantity;
    private float priceAtTransaction;
    
    private int stockBefore;
    private int stockAfter;
    
    private Long referenceId;  // Links to purchase.id or sale.id
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    private String notes;
}