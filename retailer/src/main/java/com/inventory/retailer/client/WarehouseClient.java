package com.inventory.retailer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.inventory.retailer.dto.ItemResponse;
import com.inventory.retailer.dto.WarehousePurchaseRequest;

@Component
public class WarehouseClient {

    private final RestClient restClient;

    @Value("${warehouse.central.host:order-service}")
    private String warehouseCentralHost;

    @Value("${warehouse.central.port:8084}")
    private int warehouseCentralPort;

    public WarehouseClient() {
        this.restClient = RestClient.builder().build();
    }

    // Buy from order-service (it handles routing to actual warehouses)
    public ItemResponse buyFromWarehouse(Long retailerId, Long itemId, int quantity) {
        WarehousePurchaseRequest request = new WarehousePurchaseRequest();
        request.setRetailerId(retailerId);
        request.setProductId(itemId);
        request.setQuantity(quantity);

        return restClient.post()
                .uri("http://" + warehouseCentralHost + ":" + warehouseCentralPort + "/api/order-service/purchase")
                .body(request)
                .retrieve()
                .body(ItemResponse.class);
    }
}