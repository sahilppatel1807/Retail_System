package com.inventory.order_service.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.inventory.order_service.config.RabbitMQConfig;

@Service
public class OrderProducer {
    private final RabbitTemplate rabbitTemplate;
    
    public OrderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send an order message to RabbitMQ
     * @param orderMessage The message containing order details
     */
    public void sendOrderMessage(Object orderMessage) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_ACCEPTED_EXCHANGE, RabbitMQConfig.ORDER_ACCEPTED_ROUTING_KEY, orderMessage);    
    }
}
