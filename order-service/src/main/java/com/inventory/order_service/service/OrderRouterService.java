package com.inventory.order_service.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.order_service.config.RabbitMQConfig;
import com.inventory.order_service.config.WarehouseConfig.WarehouseInfo;
import com.inventory.order_service.entity.OrderItem;
import com.inventory.order_service.repository.OrderItemRepository;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Service
@Slf4j
public class OrderRouterService {

    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;
    private final OrderItemRepository orderItemRepository;

    public OrderRouterService(InventoryService inventoryService, RabbitTemplate rabbitTemplate, OrderItemRepository orderItemRepository) {
        this.inventoryService = inventoryService;
        this.rabbitTemplate = rabbitTemplate;
        this.orderItemRepository = orderItemRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_ACCEPTED_QUEUE)
    @Transactional
    public void routeOrder(OrderItem order) {
        log.info("🎯 Routing order: {} for product: {}", order.getOrderId(), order.getProductName());

        // Find candidate warehouses
        List<WarehouseInfo> candidates = inventoryService.findWarehousesWithStock(
                order.getProductId(),
                order.getQuantity()
        );

        if (candidates.isEmpty()) {
            log.warn("❌ No warehouse found for order: {}", order.getOrderId());
            order.setStatus("OUT_OF_STOCK");
            orderItemRepository.save(order);
            return;
        }

        // Select the first warehouse (highest stock as per InventoryService logic)
        WarehouseInfo selected = candidates.get(0);
        log.info("✅ Selected Warehouse: {}", selected.getId());

        // Update order status/warehouse in DB
        order.setStatus("ROUTED");
        order.setWarehouseId(selected.getId());
        orderItemRepository.save(order);

        // Send to specific warehouse queue
        String routingKey = getRoutingKeyForWarehouse(selected.getId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_ROUTED_EXCHANGE, routingKey, order);
        log.info("📨 Order {} dispatched to warehouse: {} via routing key: {}", order.getOrderId(), selected.getId(), routingKey);
    }

    private String getRoutingKeyForWarehouse(Long warehouseId) {
        if (warehouseId == 1) return RabbitMQConfig.ORDER_ROUTED_ROUTING_KEY_WAREHOUSE1;
        if (warehouseId == 2) return RabbitMQConfig.ORDER_ROUTED_ROUTING_KEY_WAREHOUSE2;
        if (warehouseId == 3) return RabbitMQConfig.ORDER_ROUTED_ROUTING_KEY_WAREHOUSE3;
        throw new IllegalArgumentException("Unknown warehouse ID: " + warehouseId);
    }
}
