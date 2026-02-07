package com.inventory.warehouse.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inventory.warehouse.entity.Item;
import com.inventory.warehouse.messaging.StockUpdateProducer;
import com.inventory.warehouse.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ItemService {
    private final ItemRepository repository;
    private final StockUpdateProducer stockUpdateProducer;

    @Value("${warehouse.id:0}")
    private Long warehouseId;

    public ItemService(ItemRepository repository, StockUpdateProducer stockUpdateProducer) {
        this.repository = repository;
        this.stockUpdateProducer = stockUpdateProducer;
    }

    public Item createItem(Item item) {
        item.setWarehouseId(warehouseId);
        
        return repository.findByProductName(item.getProductName())
                .map(existingItem -> {
                    existingItem.setStockOnHand(existingItem.getStockOnHand() + item.getStockOnHand());
                    existingItem.setPrice(item.getPrice());
                    Item saved = repository.save(existingItem);
                    
                    // Send update to RabbitMQ
                    stockUpdateProducer.sendStockUpdate(
                        saved.getId(),
                        saved.getProductName(),
                        saved.getStockOnHand(),
                        saved.getPrice()
                    );
                    
                    return saved;
                })
                .orElseGet(() -> {
                    Item newItem = new Item();
                    newItem.setProductName(item.getProductName());
                    newItem.setPrice(item.getPrice());
                    newItem.setStockOnHand(item.getStockOnHand());
                    newItem.setWarehouseId(warehouseId);
                    Item saved = repository.save(newItem);
                    
                    // Send update to RabbitMQ
                    stockUpdateProducer.sendStockUpdate(
                        saved.getId(),
                        saved.getProductName(),
                        saved.getStockOnHand(),
                        saved.getPrice()
                    );
                    
                    return saved;
                });
    }

    public Item findItem(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ENTITY NOT FOUND WITH THE " + id));
    }

    public List<Item> getAllItems() {
        return repository.findAll();
    }

    @Transactional
    public Item sellItem(Long retailerId, Long itemId, int quantity) {

        Item item = repository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getStockOnHand() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }
        
        item.setStockOnHand(item.getStockOnHand() - quantity);
        Item saved = repository.save(item);
        
        // Send stock update to RabbitMQ
        stockUpdateProducer.sendStockUpdate(
            saved.getId(),
            saved.getProductName(),
            saved.getStockOnHand(),
            saved.getPrice()
        );
        
        System.out.println("✅ [Warehouse-" + warehouseId + "] Sold " + quantity + 
                          " units of " + saved.getProductName() + 
                          ". Remaining stock: " + saved.getStockOnHand());
        
        return saved;
    }

    public Item updateItem(Long id, Item updatedItem) {
        Item existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        
        existing.setProductName(updatedItem.getProductName());
        existing.setPrice(updatedItem.getPrice());
        existing.setStockOnHand(updatedItem.getStockOnHand());
        
        Item saved = repository.save(existing);
        
        // Send update to RabbitMQ
        stockUpdateProducer.sendStockUpdate(
            saved.getId(),
            saved.getProductName(),
            saved.getStockOnHand(),
            saved.getPrice()
        );
        
        return saved;
    }
}