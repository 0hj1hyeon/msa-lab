package com.distributed.orderservice.service;

import com.distributed.orderservice.dto.OrderCreatedEvent;
import com.distributed.orderservice.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate,
                               @Value("${app.rabbitmq.order-created.exchange}") String exchange,
                               @Value("${app.rabbitmq.order-created.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishOrderCreated(OrderDto orderDto) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderDto.getOrderId())
                .userId(orderDto.getUserId())
                .productId(orderDto.getProductId())
                .qty(orderDto.getQty())
                .unitPrice(orderDto.getUnitPrice())
                .totalPrice(orderDto.getTotalPrice())
                .build();

        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.info("RabbitMQ 주문 생성 이벤트 발행 완료. orderId={}, userId={}", event.getOrderId(), event.getUserId());
    }
}
