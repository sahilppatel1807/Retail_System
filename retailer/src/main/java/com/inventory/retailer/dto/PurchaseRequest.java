package com.inventory.retailer.dto;

import lombok.Data;

@Data
public class PurchaseRequest {
    private Long warehouseId;  // Which warehouse to buy from
    private Long itemId;
    private int quantity;
}
