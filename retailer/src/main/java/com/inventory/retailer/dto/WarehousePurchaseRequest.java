package com.inventory.retailer.dto;

import lombok.Data;

@Data
public class WarehousePurchaseRequest {
    private Long retailerId;
    private Long productId;
    private int quantity;
}
