package com.inventory.warehouse.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inventory.warehouse.entity.Item;
import com.inventory.warehouse.entity.WarehouseInventoryHistory;
import com.inventory.warehouse.messaging.StockUpdateProducer;
import com.inventory.warehouse.repository.ItemRepository;
import com.inventory.warehouse.repository.WarehouseInventoryHistoryRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ItemService {
    private final ItemRepository repository;
    private final WarehouseInventoryHistoryRepository historyRepository;
    private final StockUpdateProducer stockUpdateProducer;

    @Value("${warehouse.id:1}")
    private Long warehouseId;

    public ItemService(
            ItemRepository repository, 
            WarehouseInventoryHistoryRepository historyRepository,
            StockUpdateProducer stockUpdateProducer) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.stockUpdateProducer = stockUpdateProducer;
    }

    /**
     * Broadcast all items to Order Service on startup
     */
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void broadcastStockOnStartup() {
        log.info("🚀 System ready. Broadcasting initial stock for Warehouse {}...", warehouseId);
        List<Item> items = repository.findAll();
        for (Item item : items) {
            stockUpdateProducer.sendStockUpdate(
                item.getId(),
                item.getProductName(),
                item.getStockOnHand(),
                item.getPrice()
            );
        }
        log.info("✅ Broadcasted {} items.", items.size());
    }

    @Transactional
    public Item createItem(Item item) {
        item.setWarehouseId(warehouseId);
        
        return repository.findByProductName(item.getProductName())
                .map(existingItem -> {
                    // Item exists - add to stock
                    int stockBefore = existingItem.getStockOnHand();
                    int quantityAdded = item.getStockOnHand();
                    int stockAfter = stockBefore + quantityAdded;
                    
                    existingItem.setStockOnHand(stockAfter);
                    existingItem.setPrice(item.getPrice());
                    Item saved = repository.save(existingItem);
                    
                    // Record history
                    recordHistory(
                        saved.getId(),
                        saved.getProductName(),
                        "ADDED",
                        quantityAdded,
                        saved.getPrice(),
                        stockBefore,
                        stockAfter,
                        null,
                        "Restocked inventory"
                    );
                    
                    // Send update to RabbitMQ
                    stockUpdateProducer.sendStockUpdate(
                        saved.getId(),
                        saved.getProductName(),
                        saved.getStockOnHand(),
                        saved.getPrice()
                    );
                    
                    log.info("✅ Added " + quantityAdded + " units. Stock: " + 
                                      stockBefore + " → " + stockAfter);
                    
                    return saved;
                })
                .orElseGet(() -> {
                    // New item
                    Item newItem = new Item();
                    newItem.setProductName(item.getProductName());
                    newItem.setPrice(item.getPrice());
                    newItem.setStockOnHand(item.getStockOnHand());
                    newItem.setWarehouseId(warehouseId);
                    Item saved = repository.save(newItem);
                    
                    // Record history
                    recordHistory(
                        saved.getId(),
                        saved.getProductName(),
                        "ADDED",
                        saved.getStockOnHand(),
                        saved.getPrice(),
                        0,
                        saved.getStockOnHand(),
                        null,
                        "Initial stock"
                    );
                    
                    // Send update to RabbitMQ
                    stockUpdateProducer.sendStockUpdate(
                        saved.getId(),
                        saved.getProductName(),
                        saved.getStockOnHand(),
                        saved.getPrice()
                    );
                    
                    log.info("✅ Created new product with " + saved.getStockOnHand() + " units");
                    
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
        
        int stockBefore = item.getStockOnHand();
        int stockAfter = stockBefore - quantity;
        
        item.setStockOnHand(stockAfter);
        Item saved = repository.save(item);
        
        // Record history
        recordHistory(
            saved.getId(),
            saved.getProductName(),
            "SOLD",
            quantity,
            saved.getPrice(),
            stockBefore,
            stockAfter,
            retailerId,
            "Sold to Retailer " + retailerId
        );
        
        // Send stock update to RabbitMQ
        stockUpdateProducer.sendStockUpdate(
            saved.getId(),
            saved.getProductName(),
            saved.getStockOnHand(),
            saved.getPrice()
        );
        
        log.info("✅ [Warehouse-" + warehouseId + "] Sold " + quantity + 
                          " units of " + saved.getProductName() + 
                          ". Stock: " + stockBefore + " → " + stockAfter);
        
        return saved;
    }

    @Transactional
    public Item updateItem(Long id, Item updatedItem) {
        Item existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        
        int stockBefore = existing.getStockOnHand();
        int stockAfter = updatedItem.getStockOnHand();
        int difference = stockAfter - stockBefore;
        
        existing.setProductName(updatedItem.getProductName());
        existing.setPrice(updatedItem.getPrice());
        existing.setStockOnHand(stockAfter);
        
        Item saved = repository.save(existing);
        
        // Record history
        recordHistory(
            saved.getId(),
            saved.getProductName(),
            "ADJUSTED",
            Math.abs(difference),
            saved.getPrice(),
            stockBefore,
            stockAfter,
            null,
            difference > 0 ? "Stock increased" : "Stock decreased"
        );
        
        // Send update to RabbitMQ
        stockUpdateProducer.sendStockUpdate(
            saved.getId(),
            saved.getProductName(),
            saved.getStockOnHand(),
            saved.getPrice()
        );
        
        log.info("✅ Updated product. Stock: " + stockBefore + " → " + stockAfter);
        
        return saved;
    }
    
    /**
     * Get inventory history for a product
     */
    public List<WarehouseInventoryHistory> getProductHistory(Long productId) {
        return historyRepository.findByProductIdOrderByTransactionDateDesc(productId);
    }
    
    /**
     * Get all inventory history for this warehouse
     */
    public List<WarehouseInventoryHistory> getWarehouseHistory() {
        return historyRepository.findByWarehouseIdOrderByTransactionDateDesc(warehouseId);
    }
    
    /**
     * Helper method to record inventory history
     */
    private void recordHistory(
            Long productId,
            String productName,
            String transactionType,
            int quantity,
            float price,
            int stockBefore,
            int stockAfter,
            Long retailerId,
            String notes
    ) {
        WarehouseInventoryHistory history = new WarehouseInventoryHistory();
        history.setWarehouseId(warehouseId);
        history.setProductId(productId);
        history.setProductName(productName);
        history.setTransactionType(transactionType);
        history.setQuantity(quantity);
        history.setPriceAtTransaction(price);
        history.setStockBefore(stockBefore);
        history.setStockAfter(stockAfter);
        history.setRetailerId(retailerId);
        history.setTransactionDate(LocalDateTime.now());
        history.setNotes(notes);
        
        historyRepository.save(history);
    }
}