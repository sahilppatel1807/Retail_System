package com.inventory.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {
    private String orderId;
    private String status;
    private String message;
    private float price;
}
