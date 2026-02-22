package com.inventory.order_service.config;

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

    //stock update section constants
    public static final String QUEUE_NAME = "stock.updates.queue";
    public static final String EXCHANGE_NAME = "warehouse.exchange";
    public static final String ROUTING_KEY = "warehouse.stock.update";

    //order accepted section constants
    public static final String ORDER_ACCEPTED_QUEUE = "order.accepted.queue";
    public static final String ORDER_ACCEPTED_EXCHANGE = "order.accepted.exchange";
    public static final String ORDER_ACCEPTED_ROUTING_KEY = "order.accepted";

    //order routed section constants
    public static final String ORDER_ROUTED_EXCHANGE = "order.routed.exchange";
    public static final String ORDER_ROUTED_ROUTING_KEY_WAREHOUSE1 = "warehouse.1.routed";
    public static final String ORDER_ROUTED_ROUTING_KEY_WAREHOUSE2 = "warehouse.2.routed";
    public static final String ORDER_ROUTED_ROUTING_KEY_WAREHOUSE3 = "warehouse.3.routed";
    public static final String WAREHOUSE_1_QUEUE = "order.routed.warehouse.1";
    public static final String WAREHOUSE_2_QUEUE = "order.routed.warehouse.2";
    public static final String WAREHOUSE_3_QUEUE = "order.routed.warehouse.3";

    //status update section constants
    public static final String STATUS_UPDATE_EXCHANGE = "status.update.exchange";
    public static final String STATUS_UPDATE_ROUTING_KEY = "status.update";
    public static final String STATUS_UPDATE_QUEUE = "status.update.queue";

    // retailer notification exchange
    public static final String RETAILER_STATUS_EXCHANGE = "retailer.status.exchange";


    /* stock update section where order-service will listen to stock updates from warehouses
    and put the stock update in the cache */
    @Bean
    public Queue stockUpdatesQueue() {
        return new Queue(QUEUE_NAME, true);  // true = durable (survives restart)
    }
    @Bean
    public TopicExchange warehouseExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    @Bean
    public Binding binding(Queue stockUpdatesQueue, TopicExchange warehouseExchange) {
        return BindingBuilder
                .bind(stockUpdatesQueue)
                .to(warehouseExchange)
                .with(ROUTING_KEY);
    }


    /* order accepted section where order-service will listen to order accepted from retailers
    and put the order accepted in the cache */
    @Bean
    public Queue orderAcceptedQueue() {
        return new Queue(ORDER_ACCEPTED_QUEUE, true);
    }

    @Bean
    public TopicExchange orderAcceptedExchange() {
        return new TopicExchange(ORDER_ACCEPTED_EXCHANGE);
    }

    @Bean
    public Binding orderAcceptedBinding(Queue orderAcceptedQueue, TopicExchange orderAcceptedExchange) {
        return BindingBuilder
                .bind(orderAcceptedQueue)
                .to(orderAcceptedExchange)
                .with(ORDER_ACCEPTED_ROUTING_KEY);
    }

    /* order routed section where order-service will listen to order routed from retailers
    and put the order routed in the specific warehouse queue having the order */

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_ROUTED_EXCHANGE);
    }
    @Bean
    public Queue warehouse1Queue() {
        return new Queue(WAREHOUSE_1_QUEUE, true);
    }
    @Bean
    public Queue warehouse2Queue() {
        return new Queue(WAREHOUSE_2_QUEUE, true);
    }
    @Bean
    public Queue warehouse3Queue() {
        return new Queue(WAREHOUSE_3_QUEUE, true);
    }


    @Bean
    public Binding binding1(Queue warehouse1Queue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(warehouse1Queue)
                .to(orderExchange)
                .with(ORDER_ROUTED_ROUTING_KEY_WAREHOUSE1);
    }

    @Bean
    public Binding binding2(Queue warehouse2Queue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(warehouse2Queue)
                .to(orderExchange)
                .with(ORDER_ROUTED_ROUTING_KEY_WAREHOUSE2);
    }

    @Bean
    public Binding binding3(Queue warehouse3Queue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(warehouse3Queue)
                .to(orderExchange)
                .with(ORDER_ROUTED_ROUTING_KEY_WAREHOUSE3);
    }


    /* status update section where order-service will listen to status updates from warehouses
    and put the status update in the cache */
    @Bean
    public Queue statusUpdateQueue() {
        return new Queue(STATUS_UPDATE_QUEUE, true);
    }
    @Bean
    public TopicExchange statusUpdateExchange() {
        return new TopicExchange(STATUS_UPDATE_EXCHANGE);
    }
    @Bean
    public Binding statusUpdateBinding(Queue statusUpdateQueue, TopicExchange statusUpdateExchange) {
        return BindingBuilder
                .bind(statusUpdateQueue)
                .to(statusUpdateExchange)
                .with(STATUS_UPDATE_ROUTING_KEY);
    }

    @Bean
    public TopicExchange retailerStatusExchange() {
        return new TopicExchange(RETAILER_STATUS_EXCHANGE);
    }


    /*Convert messages from JSON to Java objects*/
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}