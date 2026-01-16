package com.inventory.retailer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.retailer.dto.PurchaseRequest;
import com.inventory.retailer.entity.Purchase;
import com.inventory.retailer.service.RetailerService;

@RestController
public class PurchaseController {
    private final RetailerService service;

    public PurchaseController(RetailerService service) {
        this.service = service;
    }

    @PostMapping("/buy")
    public Purchase buy(@RequestBody PurchaseRequest request) {
        return service.buyFromWarehouse(request.getItemId(), request.getQuantity());
    }

    @GetMapping("/all")
    public List<Purchase> getAllItems(){
        return service.getAllItems();
    }

     @GetMapping("/{id}")
    public Purchase getItem(@PathVariable Long id) {
        return service.findItem(id);
    }
}
