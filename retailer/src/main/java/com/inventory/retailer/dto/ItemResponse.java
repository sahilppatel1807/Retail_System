package com.inventory.retailer.dto;

import lombok.Data;

@Data
public class ItemResponse {
    private String orderId;
    private String status;
    private Long productId;
    private String productName;
    private int quantity;
    private float price;
}
