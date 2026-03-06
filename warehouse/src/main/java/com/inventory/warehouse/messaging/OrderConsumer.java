package com.inventory.warehouse.messaging;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inventory.warehouse.entity.PendingOrder;
import com.inventory.warehouse.repository.PendingOrderRepository;
import com.inventory.warehouse.service.ItemService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderConsumer {

    private final ItemService itemService;
    private final StatusUpdateProducer statusUpdateProducer;
    private final PendingOrderRepository pendingOrderRepository;

    @Value("${warehouse.id:1}")
    private Long warehouseId;

    public OrderConsumer(ItemService itemService, StatusUpdateProducer statusUpdateProducer, PendingOrderRepository pendingOrderRepository) {
        this.itemService = itemService;
        this.statusUpdateProducer = statusUpdateProducer;
        this.pendingOrderRepository = pendingOrderRepository;
    }

    @RabbitListener(queues = "order.routed.warehouse.${warehouse.id:1}")
    public void consumeOrder(Map<String, Object> orderData) {
        String orderId = (String) orderData.get("orderId");
        Long productId = Long.valueOf(orderData.get("productId").toString());
        int quantity = (int) orderData.get("quantity");
        Long retailerId = Long.valueOf(orderData.get("retailerId").toString());

        log.info("📦 Warehouse {} received order {}", warehouseId, orderId);

        // SAVE TO DATABASE instead of processing immediately
        PendingOrder pendingOrder = new PendingOrder();
        pendingOrder.setOrderId(orderId);
        pendingOrder.setProductId(productId);
        pendingOrder.setProductName("Product-" + productId); // Or fetch from item table
        pendingOrder.setQuantity(quantity);
        pendingOrder.setRetailerId(retailerId);
        pendingOrder.setWarehouseId(warehouseId);
        pendingOrder.setReceivedAt(LocalDateTime.now());
        
        pendingOrderRepository.save(pendingOrder);
        
        log.info("✅ Order {} saved to pending orders", orderId);
    }
}
