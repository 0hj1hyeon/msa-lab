package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.exception.RetryableNotificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCompensationEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private OrderCompensationEventPublisher orderCompensationEventPublisher;

    @BeforeEach
    void setUp() {
        orderCompensationEventPublisher = new OrderCompensationEventPublisher(
                rabbitTemplate,
                "order.exchange",
                "order.compensation.requested"
        );
    }

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void publishNotificationCompensation_publishesSagaCompensationEvent() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .qty(2)
                .unitPrice(1000)
                .totalPrice(2000)
                .build();

        RetryableNotificationException exception =
                new RetryableNotificationException("notification failed", new RuntimeException("db down"));

        orderCompensationEventPublisher.publishNotificationCompensation(event, exception);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("order.exchange"), eq("order.compensation.requested"), eventCaptor.capture());

        assertThat(eventCaptor.getValue())
                .hasFieldOrPropertyWithValue("orderId", "order-1")
                .hasFieldOrPropertyWithValue("userId", "user-123")
                .hasFieldOrPropertyWithValue("reason", "notification failed")
                .hasFieldOrPropertyWithValue("failedStep", "NOTIFICATION")
                .hasFieldOrPropertyWithValue("failureType", "RetryableNotificationException");
    }
}
