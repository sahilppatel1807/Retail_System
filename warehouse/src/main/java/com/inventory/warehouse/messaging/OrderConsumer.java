package com.inventory.warehouse.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inventory.warehouse.dto.OrderUpdateDTO;
import com.inventory.warehouse.service.ItemService;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Service
@Slf4j
public class OrderConsumer {

    private final ItemService itemService;
    private final StatusUpdateProducer statusUpdateProducer;

    @Value("${warehouse.id:1}")
    private Long warehouseId;

    public OrderConsumer(ItemService itemService, StatusUpdateProducer statusUpdateProducer) {
        this.itemService = itemService;
        this.statusUpdateProducer = statusUpdateProducer;
    }

    @RabbitListener(queues = "order.routed.warehouse.${warehouse.id:1}")
    public void consumeOrder(Map<String, Object> orderData) {
        String orderId = (String) orderData.get("orderId");
        Long productId = Long.valueOf(orderData.get("productId").toString());
        int quantity = (int) orderData.get("quantity");
        Long retailerId = Long.valueOf(orderData.get("retailerId").toString());

        log.info("📦 Warehouse {} received order {} for product {}", warehouseId, orderId, productId);

        try {
            // Process the sale
            com.inventory.warehouse.entity.Item item = itemService.sellItem(retailerId, productId, quantity);
            
            // Send success status update
            statusUpdateProducer.sendStatusUpdate(new OrderUpdateDTO(orderId, "COMPLETED", "Order processed successfully by warehouse " + warehouseId, item.getPrice()));
            log.info("✅ Order {} completed by warehouse {}", orderId, warehouseId);
            
        } catch (Exception e) {
            log.error("❌ Failed to process order {}: {}", orderId, e.getMessage());
            // Send failed status update
            statusUpdateProducer.sendStatusUpdate(new OrderUpdateDTO(orderId, "FAILED", e.getMessage(), 0.0f));
        }
    }
}
