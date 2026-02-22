package com.inventory.order_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    private Long productId;
    private String productName;
    private Long warehouseId;
    private int stockOnHand;
    private float price;
}