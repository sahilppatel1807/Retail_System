package com.inventory.order_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.inventory.order_service.dto.StockUpdateMessage;
import com.inventory.order_service.service.InventoryService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StockUpdateConsumer {

    private final InventoryService inventoryService;

    public StockUpdateConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Listen to stock update messages from RabbitMQ
     * This method is called automatically when a message arrives
     */
    @RabbitListener(queues = "stock.updates.queue")
    public void handleStockUpdate(StockUpdateMessage message) {
        
        log.info("📥 Received stock update from Warehouse " + message.getWarehouseId() + 
                          ": Product " + message.getProductId() + 
                          " now has " + message.getNewStock() + " units");
        
        // Update local cache
        inventoryService.updateProductStock(
            message.getProductId(),
            message.getProductName(),
            message.getWarehouseId(),
            message.getNewStock(),
            message.getPrice()
        );
        
        log.info("✅ Cache updated successfully");
    }

}