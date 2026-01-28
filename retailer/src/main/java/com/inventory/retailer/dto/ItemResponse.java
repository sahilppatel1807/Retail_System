package com.inventory.retailer.dto;

import lombok.Data;

@Data
public class ItemResponse {
    private Long id;
    private String productName;
    private float price;
    private int stockOnHand;

}
