package com.inventory.customer.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.inventory.customer.client.RetailerClient;
import com.inventory.customer.entity.Order;
import com.inventory.customer.repository.OrderRepository;


@Service
public class OrderService {
    private final RetailerClient retailerClient;
    private final OrderRepository repository;

    public OrderService(RetailerClient retailerClient , OrderRepository repository){
        this.retailerClient = retailerClient;
        this.repository = repository;
    }

    public Order placeOrder(Long productId, int quantity, String customerName) {

        Map<String, Object> product = retailerClient.getProduct(productId);

        Order order = new Order();
        order.setProductId(productId);
        order.setProductName((String) product.get("name"));
        order.setPrice(((Number) product.get("price")).doubleValue());
        order.setQuantity(quantity);
        order.setCustomerName(customerName);
        order.setOrderTime(LocalDateTime.now());

        retailerClient.placeOrder(productId, quantity, customerName);

        return repository.save(order);
    }
    

}
