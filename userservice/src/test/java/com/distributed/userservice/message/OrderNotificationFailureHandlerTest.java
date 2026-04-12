package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderNotificationFailureHandlerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private OrderNotificationFailureHandler orderNotificationFailureHandler;

    @BeforeEach
    void setUp() {
        orderNotificationFailureHandler = new OrderNotificationFailureHandler(
                rabbitTemplate,
                "order.notification.queue",
                "order.notification.dlx",
                "order.notification.dlq",
                3
        );
    }

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void shouldMoveToDlq_returnsFalseWhenRetryCountIsBelowThreshold() {
        Message message = MessageBuilder.withBody(new byte[0])
                .setHeader("x-death", List.of(Map.of("queue", "order.notification.queue", "count", 2L)))
                .build();

        assertThat(orderNotificationFailureHandler.shouldMoveToDlq(message)).isFalse();
    }

    @Test
    void shouldMoveToDlq_returnsTrueWhenRetryCountReachedThreshold() {
        Message message = MessageBuilder.withBody(new byte[0])
                .setHeader("x-death", List.of(Map.of("queue", "order.notification.queue", "count", 3L)))
                .build();

        assertThat(orderNotificationFailureHandler.shouldMoveToDlq(message)).isTrue();
    }

    @Test
    void publishToDlq_sendsEventToDeadLetterExchange() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .qty(2)
                .unitPrice(1000)
                .totalPrice(2000)
                .build();

        orderNotificationFailureHandler.publishToDlq(event, new RuntimeException("notification failed"));

        verify(rabbitTemplate).convertAndSend(
                eq("order.notification.dlx"),
                eq("order.notification.dlq"),
                eq(event),
                any(MessagePostProcessor.class)
        );
    }
}
