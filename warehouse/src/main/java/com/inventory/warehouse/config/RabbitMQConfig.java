package com.inventory.warehouse.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "warehouse.exchange";
    
    public static final String STATUS_UPDATE_EXCHANGE = "status.update.exchange";
    public static final String STATUS_UPDATE_ROUTING_KEY = "status.update";

    // create the topic exchange for communication between retailer and warehouse
    @Bean
    public TopicExchange warehouseExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public TopicExchange statusUpdateExchange() {
        return new TopicExchange(STATUS_UPDATE_EXCHANGE);
    }

    // configure the message converter to use JSON format for messages
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}