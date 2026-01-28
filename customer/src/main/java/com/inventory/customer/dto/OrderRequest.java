package com.inventory.customer.dto;

import lombok.Data;

@Data
public class OrderRequest { 
    Long productId;
    int quantity;
    String customerName;
}
