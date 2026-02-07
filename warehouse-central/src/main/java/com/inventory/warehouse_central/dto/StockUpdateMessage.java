package com.inventory.warehouse_central.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateMessage {
    private Long warehouseId;
    private Long productId;
    private String productName;
    private int newStock;
    private float price;
}