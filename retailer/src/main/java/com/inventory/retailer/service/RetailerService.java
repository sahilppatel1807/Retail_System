package com.inventory.retailer.service;

import java.util.List;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.inventory.retailer.dto.ItemResponse;
import com.inventory.retailer.entity.Purchase;
import com.inventory.retailer.repository.PurchaseRepository;


@Service
public class RetailerService {
    private final RestTemplate restTemplate;
    private final PurchaseRepository repository;

    public RetailerService(RestTemplate restTemplate, PurchaseRepository repository){
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    public Purchase buyFromWarehouse(Long itemId, int quantity) {

        String url = "http://localhost:8081/items/buy"
                + "?itemId=" + itemId
                + "&quantity=" + quantity;

        ItemResponse item = restTemplate.postForObject(
                url,
                null,
                ItemResponse.class
        );

        Purchase retailerItem = new Purchase();
        retailerItem.setWarehouseItemId(item.getId());
        retailerItem.setProductName(item.getProductName());
        retailerItem.setPrice(item.getPrice());
        retailerItem.setQuantity(quantity);

        return repository.save(retailerItem);
    }

    public List<Purchase> getAllItems(){
        return repository.findAll();
    }

    public Purchase findItem(Long id) {
        return repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Purchase not found with id: " + id));
    }


}
