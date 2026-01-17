package com.inventory.customer.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.customer.entity.Order;
import com.inventory.customer.service.OrderService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/customer")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/orders")
    public Order placeOrder(
        @RequestParam Long productId,
        @RequestParam int quantity,
        @RequestParam String customerName
    ) {
        return service.placeOrder(productId, quantity, customerName);
    }

    @GetMapping("/products")
    public List<Map<String, Object>> getRetailerProducts() {
        return service.getRetailerProducts();
    }

    @GetMapping("/all")
    public List<Order> getAllOrders(){
        return service.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id){
        return service.getOrderById(id);
    }
}
