package com.inventory.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_inventory_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseInventoryHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long warehouseId;
    private Long productId;  // References Item.id
    private String productName;
    
    @Column(nullable = false)
    private String transactionType;  // ADDED, SOLD, ADJUSTED
    
    private int quantity;  // Quantity in this transaction
    private float priceAtTransaction;
    
    private int stockBefore;  // Stock before transaction
    private int stockAfter;   // Stock after transaction
    
    private Long retailerId;  // NULL if ADDED, populated if SOLD
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    private String notes;
}