package com.inventory.warehouse_central.messaging;

import com.inventory.warehouse_central.dto.StockUpdateMessage;
import com.inventory.warehouse_central.service.InventoryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
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
        
        System.out.println("📥 Received stock update from Warehouse " + message.getWarehouseId() + 
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
        
        System.out.println("✅ Cache updated successfully");
    }
}