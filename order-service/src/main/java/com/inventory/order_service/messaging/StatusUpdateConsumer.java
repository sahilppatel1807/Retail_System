package com.inventory.order_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.inventory.order_service.config.RabbitMQConfig;
import com.inventory.order_service.dto.OrderUpdateDTO;
import com.inventory.order_service.entity.OrderItem;
import com.inventory.order_service.service.OrderItemService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.inventory.order_service.entity.OrderItem;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StatusUpdateConsumer {

    private final OrderItemService orderItemService;
    private final RabbitTemplate rabbitTemplate;

    public StatusUpdateConsumer(OrderItemService orderItemService, RabbitTemplate rabbitTemplate) {
        this.orderItemService = orderItemService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.STATUS_UPDATE_QUEUE)
    public void consumeStatusUpdate(OrderUpdateDTO update) {
        log.info("📢 Received status update for order {}: {} - {}", update.getOrderId(), update.getStatus(), update.getMessage());
        
        try {
            OrderItem orderItem = orderItemService.updateOrderStatus(update.getOrderId(), update.getStatus());
            orderItemService.addNotes(update.getOrderId(), update.getMessage());
            log.info("✅ Database updated for order {}", update.getOrderId());

            // Set the price in the update DTO so the retailer gets it
            update.setPrice(orderItem.getPrice());

            // Forward to retailer status exchange
            String routingKey = "retailer." + orderItem.getRetailerId();
            rabbitTemplate.convertAndSend(RabbitMQConfig.RETAILER_STATUS_EXCHANGE, routingKey, update);
            log.info("📤 Status update forwarded to {} with routing key {}", RabbitMQConfig.RETAILER_STATUS_EXCHANGE, routingKey);

        } catch (Exception e) {
            log.error("❌ Failed to update status for order {}: {}", update.getOrderId(), e.getMessage());
        }
    }
}
