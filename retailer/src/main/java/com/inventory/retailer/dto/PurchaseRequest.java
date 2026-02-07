package com.inventory.retailer.dto;

import lombok.Data;

@Data
public class PurchaseRequest {
    private Long itemId;
    private int quantity;
}
