package com.inventory.retailer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    private Long id;
    private String orderId;
    private String status;
    private LocalDateTime placedAt;
    private LocalDateTime updatedAt;
}
