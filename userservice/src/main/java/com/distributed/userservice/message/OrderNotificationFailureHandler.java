package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OrderNotificationFailureHandler {

    private static final String X_DEATH = "x-death";

    private final RabbitTemplate rabbitTemplate;
    private final String notificationQueueName;
    private final String deadLetterExchange;
    private final String deadLetterRoutingKey;
    private final int maxRetryCount;

    public OrderNotificationFailureHandler(RabbitTemplate rabbitTemplate,
                                           @Value("${app.rabbitmq.order-created.notification-queue}") String notificationQueueName,
                                           @Value("${app.rabbitmq.order-created.notification-dlx}") String deadLetterExchange,
                                           @Value("${app.rabbitmq.order-created.notification-dlq-routing-key}") String deadLetterRoutingKey,
                                           @Value("${app.rabbitmq.order-created.notification-max-retry-count}") int maxRetryCount) {
        this.rabbitTemplate = rabbitTemplate;
        this.notificationQueueName = notificationQueueName;
        this.deadLetterExchange = deadLetterExchange;
        this.deadLetterRoutingKey = deadLetterRoutingKey;
        this.maxRetryCount = maxRetryCount;
    }

    public boolean shouldMoveToDlq(Message message) {
        return getNotificationDeathCount(message) >= maxRetryCount;
    }

    public void publishToDlq(OrderCreatedEvent event, Exception exception) {
        rabbitTemplate.convertAndSend(deadLetterExchange, deadLetterRoutingKey, event, outgoingMessage -> {
            outgoingMessage.getMessageProperties().setHeader("x-error-message", exception.getMessage());
            return outgoingMessage;
        });

        log.error("알림 처리 실패 메시지를 DLQ로 이동했습니다. orderId={}, reason={}",
                event.getOrderId(),
                exception.getMessage());
    }

    long getNotificationDeathCount(Message message) {
        Object xDeathHeader = message.getMessageProperties().getHeaders().get(X_DEATH);

        if (!(xDeathHeader instanceof List<?> xDeathEntries)) {
            return 0L;
        }

        return xDeathEntries.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .filter(entry -> notificationQueueName.equals(entry.get("queue")))
                .map(entry -> entry.get("count"))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToLong(Number::longValue)
                .findFirst()
                .orElse(0L);
    }
}
