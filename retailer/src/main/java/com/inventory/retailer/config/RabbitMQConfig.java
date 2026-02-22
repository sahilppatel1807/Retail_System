package com.inventory.retailer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "retailer.status.exchange";

    @Value("${retailer.id}")
    private String retailerId;

    @Bean
    public Queue statusQueue() {
        return new Queue("retailer.status." + retailerId, true);
    }

    @Bean
    public TopicExchange statusExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding statusBinding(Queue statusQueue, TopicExchange statusExchange) {
        return BindingBuilder.bind(statusQueue).to(statusExchange).with("retailer." + retailerId + ".#");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
