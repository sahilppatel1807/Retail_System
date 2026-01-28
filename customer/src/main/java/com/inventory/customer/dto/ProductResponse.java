package com.inventory.customer.dto;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private Long warehouseItemId;
    private String productName;
    private float price;
    private int quantity;
}
