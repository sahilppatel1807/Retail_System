package com.inventory.customer.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private Long productId;
    private int quantity;
    private String customerName;
    private LocalDateTime soldAt;
}