package com.inventory.warehouse_central.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.inventory.warehouse_central.config.WarehouseConfig.WarehouseInfo;
import com.inventory.warehouse_central.dto.ItemResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WarehouseClient {

    /**
     * Buy item directly from a specific warehouse
     */
    public ItemResponse buyItem(
            WarehouseInfo warehouse,
            Long productId,
            int quantity,
            Long retailerId
    ) {
        log.info("🔗 Calling Warehouse " + warehouse.getId() + 
                          " to buy " + quantity + " units of Product " + productId);

        // Build the full URL
        String fullUrl = warehouse.getBaseUrl() + "/api/warehouse/buy" +
                         "?itemId=" + productId +
                         "&quantity=" + quantity +
                         "&retailerId=" + retailerId;

        log.info("   URL: " + fullUrl);

        // Create simple RestClient
        RestClient restClient = RestClient.create();

        // Make POST request
        ItemResponse response = restClient
                .post()
                .uri(fullUrl)
                .retrieve()
                .body(ItemResponse.class);

        log.info("✅ Successfully purchased from Warehouse " + warehouse.getId());

        return response;
    }
}