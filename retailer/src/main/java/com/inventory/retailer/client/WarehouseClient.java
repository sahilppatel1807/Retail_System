package com.inventory.retailer.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WarehouseClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public void reduceStock(Long warehouseItemId, int quantity) {
        restTemplate.postForObject(
            "http://localhost:8081/api/warehouse/" +
            warehouseItemId +
            "/reduce?quantity=" +
            quantity,
            null,
            Void.class
        );
    }
}