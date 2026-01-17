package com.inventory.customer.client;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RetailerClient {
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${retailer.host:localhost}")
    private String retailerHost;

    public Map<String, Object> getProduct(Long id){
        return restTemplate.getForObject(
            "http://" + retailerHost + ":8082/api/retailer/" + id,
            Map.class
        );
    }

    public List<Map<String, Object>> getAllProducts() {
        return restTemplate.getForObject(
            "http://" + retailerHost + ":8082/api/retailer/all",
            List.class
        );
    }


    public void placeOrder(Long productId, int quantity, String customerName){
        restTemplate.postForObject("http://" + retailerHost + ":8082/api/retailer/orders" +
            "?productId=" + productId +
            "&quantity=" + quantity +
            "&customerName=" + customerName,
            null,
            Void.class
        );
    }

}
