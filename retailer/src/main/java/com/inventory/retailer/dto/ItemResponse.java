package com.inventory.retailer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemResponse {
    private Long id;
    private String productName;
    private float price;
    private int stockOnHand;

}
