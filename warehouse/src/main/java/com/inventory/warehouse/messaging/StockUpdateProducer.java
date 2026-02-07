package com.inventory.warehouse.messaging;

import com.inventory.warehouse.dto.StockUpdateMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StockUpdateProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${warehouse.id}")
    private Long warehouseId;

    private static final String EXCHANGE = "warehouse.exchange";
    private static final String ROUTING_KEY = "warehouse.stock.update";

    public StockUpdateProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send stock update message to RabbitMQ
     */
    public void sendStockUpdate(Long productId, String productName, int newStock, float price) {
        
        StockUpdateMessage message = new StockUpdateMessage(
            warehouseId,
            productId,
            productName,
            newStock,
            price
        );

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
        
        System.out.println("📤 [Warehouse-" + warehouseId + "] Sent stock update: Product " + 
                          productId + " now has " + newStock + " units");
    }
}