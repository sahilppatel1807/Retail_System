package com.inventory.warehouse.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.inventory.warehouse.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StatusUpdateProducer {
    private final RabbitTemplate rabbitTemplate;

    public StatusUpdateProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendStatusUpdate(Object orderStatusUpdate) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.STATUS_UPDATE_EXCHANGE,
            RabbitMQConfig.STATUS_UPDATE_ROUTING_KEY,
            orderStatusUpdate
        );
        log.info("📨 Sent status update for order to exchange: {}", RabbitMQConfig.STATUS_UPDATE_EXCHANGE);
    }
}
