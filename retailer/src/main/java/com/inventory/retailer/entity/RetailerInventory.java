package com.inventory.retailer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "retailer_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetailerInventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long retailerId;
    private Long productId;  // Original product ID from warehouse
    private String productName;
    
    private int quantityOnHand;  // Current stock
    private float averagePurchasePrice;  // Average price paid to warehouse
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
}