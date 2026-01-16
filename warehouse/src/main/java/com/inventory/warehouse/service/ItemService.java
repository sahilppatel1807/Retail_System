package com.inventory.warehouse.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.inventory.warehouse.entity.Item;
import com.inventory.warehouse.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ItemService {
    private final ItemRepository repository;

    public ItemService(ItemRepository repository){
        this.repository=repository;
    }

    public Item createItem(Item item){
        Item newItem = new Item(item.getProductName(),item.getPrice(),item.getStockOnHand());
        return repository.save(newItem);
    }

    public Item findItem(Long id){
        return repository.findById(id).orElseThrow(()-> new EntityNotFoundException("ENTITY NOT FOUND WITH THE "+id));
    }
    public List<Item> getAllItems(){
        return repository.findAll();
    }
}
