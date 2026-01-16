package com.inventory.retailer.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.inventory.retailer.client.WarehouseClient;
import com.inventory.retailer.dto.ItemResponse;
import com.inventory.retailer.entity.Purchase;
import com.inventory.retailer.entity.Sale;
import com.inventory.retailer.repository.PurchaseRepository;
import com.inventory.retailer.repository.SaleRepository;

@Service
public class RetailerService {

    private final WarehouseClient warehouseClient;
    private final SaleRepository saleRepository;
    private final RestTemplate restTemplate;
    private final PurchaseRepository repository;

    public RetailerService(RestTemplate restTemplate, PurchaseRepository repository, WarehouseClient warehouseClient, SaleRepository sellRepository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
        this.warehouseClient = warehouseClient;
        this.saleRepository = sellRepository;
    }

    public Purchase buyFromWarehouse(Long itemId, int quantity) {

        String url = "http://localhost:8081/items/buy"
                + "?itemId=" + itemId
                + "&quantity=" + quantity;

        ItemResponse item = restTemplate.postForObject(
                url,
                null,
                ItemResponse.class);

        Purchase retailerItem = new Purchase();
        retailerItem.setWarehouseItemId(item.getId());
        retailerItem.setProductName(item.getProductName());
        retailerItem.setPrice(item.getPrice());
        retailerItem.setQuantity(quantity);

        return repository.save(retailerItem);
    }

    public List<Purchase> getAllItems() {
        return repository.findAll();
    }

    public Purchase findItem(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found with id: " + id));
    }

    public Sale placeOrder(Long productId, int quantity, String customerName) {

        Purchase product = repository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }

        // reduce retailer stock
        product.setQuantity(product.getQuantity() - quantity);
        repository.save(product);

        // create sale record
        Sale sale = new Sale();
        sale.setProductId(productId);
        sale.setQuantity(quantity);
        sale.setCustomerName(customerName);
        sale.setSoldAt(LocalDateTime.now());

        return saleRepository.save(sale);
    }

}
