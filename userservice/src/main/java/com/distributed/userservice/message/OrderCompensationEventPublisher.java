package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCompensationRequestedEvent;
import com.distributed.userservice.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCompensationEventPublisher {

    private static final String NOTIFICATION_STEP = "NOTIFICATION";

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public OrderCompensationEventPublisher(RabbitTemplate rabbitTemplate,
                                           @Value("${app.rabbitmq.order-created.exchange}") String exchange,
                                           @Value("${app.rabbitmq.order-compensation.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishNotificationCompensation(OrderCreatedEvent event, Exception exception) {
        OrderCompensationRequestedEvent compensationEvent = OrderCompensationRequestedEvent.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .reason(exception.getMessage())
                .failedStep(NOTIFICATION_STEP)
                .failureType(exception.getClass().getSimpleName())
                .build();

        rabbitTemplate.convertAndSend(exchange, routingKey, compensationEvent);
        log.info("Saga 보상 이벤트 발행 완료. orderId={}, failedStep={}",
                compensationEvent.getOrderId(),
                compensationEvent.getFailedStep());
    }
}
