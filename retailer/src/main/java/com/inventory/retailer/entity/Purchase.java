package com.inventory.retailer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "retailer")
@Data
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long retailerId;
    private Long warehouseId;  // Which warehouse this purchase came from

    private Long warehouseItemId;

    private String productName;
    private float price;
    private int quantity;
}
