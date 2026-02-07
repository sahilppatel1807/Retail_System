package com.inventory.warehouse_central.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "stock.updates.queue";
    public static final String EXCHANGE_NAME = "warehouse.exchange";
    public static final String ROUTING_KEY = "warehouse.stock.update";

    /**
     * Create the queue where messages will be stored
     */
    @Bean
    public Queue stockUpdatesQueue() {
        return new Queue(QUEUE_NAME, true);  // true = durable (survives restart)
    }

    /**
     * Create the exchange (same as in Warehouse)
     */
    @Bean
    public TopicExchange warehouseExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    /**
     * Bind the queue to the exchange with routing key
     * This tells RabbitMQ: "Messages with routing key 'warehouse.stock.update' 
     * should go to queue 'stock.updates.queue'"
     */
    @Bean
    public Binding binding(Queue stockUpdatesQueue, TopicExchange warehouseExchange) {
        return BindingBuilder
                .bind(stockUpdatesQueue)
                .to(warehouseExchange)
                .with(ROUTING_KEY);
    }

    /**
     * Convert messages from JSON to Java objects
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}