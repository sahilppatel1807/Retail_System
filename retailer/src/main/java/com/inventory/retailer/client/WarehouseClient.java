package com.inventory.retailer.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.inventory.retailer.dto.ItemResponse;

@Component
public class WarehouseClient {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Buy items from warehouse
     * @param itemId The warehouse item ID
     * @param quantity The quantity to purchase
     * @return ItemResponse with item details
     */
    public ItemResponse buyFromWarehouse(Long itemId, int quantity) {
        String url = "http://localhost:8081/api/warehouse/buy"
                + "?itemId=" + itemId
                + "&quantity=" + quantity;

        return restTemplate.postForObject(url, null, ItemResponse.class);
    }

    /**
     * Get all items from warehouse
     * @return List of all warehouse items
     */
    public ItemResponse[] getAllItems() {
        return restTemplate.getForObject(
            "http://localhost:8081/api/warehouse/all",
            ItemResponse[].class
        );
    }

    /**
     * Get specific item from warehouse
     * @param itemId The warehouse item ID
     * @return Item details
     */
    public ItemResponse getItem(Long itemId) {
        return restTemplate.getForObject(
            "http://localhost:8081/api/warehouse/" + itemId,
            ItemResponse.class
        );
    }
}