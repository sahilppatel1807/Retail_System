package com.inventory.customer.client;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RetailerClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getProduct(Long id){
        return restTemplate.getForObject(
            "http://localhost:8082/" + id,
            Map.class
        );
    }

    public void placeOrder(Long productId, int quantity, String customerName){
        restTemplate.postForObject("http://localhost:8082/orders" +
            "?productId=" + productId +
            "&quantity=" + quantity +
            "&customerName=" + customerName,
            null,
            Void.class
        );
    }

}
