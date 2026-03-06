package com.inventory.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage {
    String orderId;
    Long productId;
    String productName;
    int quantity;
    Long retailerId;
}
