package com.inventory.retailer.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.inventory.retailer.config.WarehouseConfig;
import com.inventory.retailer.dto.ItemResponse;

@Component
public class WarehouseClient {

    private final RestTemplate restTemplate;
    private final WarehouseConfig warehouseConfig;

    public WarehouseClient(RestTemplate restTemplate, WarehouseConfig warehouseConfig) {
        this.restTemplate = restTemplate;
        this.warehouseConfig = warehouseConfig;
    }

    // Buy from warehouse using only warehouseId
    public ItemResponse buyFromWarehouse(Long warehouseId, Long retailerId, Long itemId, int quantity) {
        // Get warehouse details from config
        WarehouseConfig.WarehouseInfo warehouse = warehouseConfig.getWarehouse(warehouseId);
        
        String url = "http://" + warehouse.getHost() + ":" + warehouse.getPort()
                     + "/api/warehouse/buy?retailerId=" + retailerId 
                     + "&itemId=" + itemId + "&quantity=" + quantity;
        
        return restTemplate.postForObject(url, null, ItemResponse.class);
    }

    // Get all items from specific warehouse
    public List<ItemResponse> getWarehouseItems(Long warehouseId) {
        WarehouseConfig.WarehouseInfo warehouse = warehouseConfig.getWarehouse(warehouseId);
        
        String url = "http://" + warehouse.getHost() + ":" + warehouse.getPort() 
                     + "/api/warehouse/all";
        
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ItemResponse>>() {})
                .getBody();
    }

    // Get specific item from warehouse
    public ItemResponse getWarehouseItem(Long warehouseId, Long itemId) {
        WarehouseConfig.WarehouseInfo warehouse = warehouseConfig.getWarehouse(warehouseId);
        
        String url = "http://" + warehouse.getHost() + ":" + warehouse.getPort() 
                     + "/api/warehouse/" + itemId;
        
        return restTemplate.getForObject(url, ItemResponse.class);
    }
}