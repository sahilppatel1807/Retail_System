package com.inventory.customer.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.inventory.customer.client.RetailerClient;
import com.inventory.customer.dto.ProductResponse;
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

        // Get product details from retailer
        ProductResponse product = retailerClient.getProduct(productId);

        // Create local order record
        Order order = new Order();
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        order.setPrice(product.getPrice());
        order.setQuantity(quantity);
        order.setCustomerName(customerName);
        order.setOrderTime(LocalDateTime.now());

        // Place order with retailer
        retailerClient.placeOrder(productId, quantity, customerName);

        // Save order locally
        return repository.save(order);
    }

    public List<ProductResponse> getRetailerProducts() {
        return retailerClient.getAllProducts();
    }

    public List<Order> getAllOrders(){
        return repository.findAll();
    }

    public Order getOrderById(Long id){
        return repository.findById(id).orElseThrow(()-> new RuntimeException("no order found") );
    }
    

}
