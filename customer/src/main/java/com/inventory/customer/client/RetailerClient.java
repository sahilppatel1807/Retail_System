package com.inventory.customer.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.inventory.customer.dto.OrderResponse;
import com.inventory.customer.dto.ProductResponse;

@Component
public class RetailerClient {
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${retailer.host:localhost}")
    private String retailerHost;

    public ProductResponse getProduct(Long id){
        return restTemplate.getForObject(
            "http://" + retailerHost + ":8082/api/retailer/" + id,
            ProductResponse.class
        );
    }

    public List<ProductResponse> getAllProducts() {
        return restTemplate.getForObject(
            "http://" + retailerHost + ":8082/api/retailer/all",
            List.class
        );
    }


    public OrderResponse placeOrder(Long productId, int quantity, String customerName){
        return restTemplate.postForObject("http://" + retailerHost + ":8082/api/retailer/orders" +
            "?productId=" + productId +
            "&quantity=" + quantity +
            "&customerName=" + customerName,
            null,
            OrderResponse.class
        );
    }

}

