package com.distributed.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue orderHistoryQueue(@Value("${app.rabbitmq.order-created.history-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue orderNotificationQueue(@Value("${app.rabbitmq.order-created.notification-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue orderLoggingQueue(@Value("${app.rabbitmq.order-created.logging-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange orderExchange(@Value("${app.rabbitmq.order-created.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding orderHistoryBinding(@Qualifier("orderHistoryQueue") Queue orderHistoryQueue,
                                       DirectExchange orderExchange,
                                       @Value("${app.rabbitmq.order-created.routing-key}") String routingKey) {
        return BindingBuilder.bind(orderHistoryQueue).to(orderExchange).with(routingKey);
    }

    @Bean
    public Binding orderNotificationBinding(@Qualifier("orderNotificationQueue") Queue orderNotificationQueue,
                                            DirectExchange orderExchange,
                                            @Value("${app.rabbitmq.order-created.routing-key}") String routingKey) {
        return BindingBuilder.bind(orderNotificationQueue).to(orderExchange).with(routingKey);
    }

    @Bean
    public Binding orderLoggingBinding(@Qualifier("orderLoggingQueue") Queue orderLoggingQueue,
                                       DirectExchange orderExchange,
                                       @Value("${app.rabbitmq.order-created.routing-key}") String routingKey) {
        return BindingBuilder.bind(orderLoggingQueue).to(orderExchange).with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
