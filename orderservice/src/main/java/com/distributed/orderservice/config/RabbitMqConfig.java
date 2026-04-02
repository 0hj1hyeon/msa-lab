package com.distributed.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue orderCreatedQueue(@Value("${app.rabbitmq.order-created.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange orderExchange(@Value("${app.rabbitmq.order-created.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue,
                                       DirectExchange orderExchange,
                                       @Value("${app.rabbitmq.order-created.routing-key}") String routingKey) {
        return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
