package com.inventory.warehouse.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "item")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long warehouseId;  // Which warehouse owns this item
    private String productName;
    private float price;
    private int stockOnHand;

    public Item(String productName, float price, int stockOnHand){
        this.productName = productName;
        this.price = price;
        this.stockOnHand = stockOnHand;
    }
}
