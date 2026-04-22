package com.distributed.orderservice.service;

import com.distributed.orderservice.dto.OrderCreatedEvent;
import com.distributed.orderservice.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final String topic;

    public OrderKafkaProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
                              @Value("${app.kafka.order-created.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(OrderDto orderDto) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderDto.getOrderId())
                .userId(orderDto.getUserId())
                .productId(orderDto.getProductId())
                .qty(orderDto.getQty())
                .unitPrice(orderDto.getUnitPrice())
                .totalPrice(orderDto.getTotalPrice())
                .build();

        kafkaTemplate.send(topic, event.getOrderId(), event);
        log.info("Kafka 주문 생성 이벤트 발행 완료. orderId={}, userId={}", event.getOrderId(), event.getUserId());
    }
}
