package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.service.UserNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderNotificationEventListenerTest {

    @Mock
    private UserNotificationService userNotificationService;

    @Mock
    private OrderNotificationFailureHandler orderNotificationFailureHandler;

    @InjectMocks
    private OrderNotificationEventListener orderNotificationEventListener;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void handle_savesNotification() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .qty(2)
                .unitPrice(1000)
                .totalPrice(2000)
                .build();

        Message message = MessageBuilder.withBody(new byte[0]).build();

        orderNotificationEventListener.handle(event, message);

        verify(userNotificationService).saveOrderCreatedNotification(event);
    }

    @Test
    void handle_throwsToMoveMessageToRetryQueue() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .qty(2)
                .unitPrice(1000)
                .totalPrice(2000)
                .build();

        Message message = MessageBuilder.withBody(new byte[0]).build();

        doThrow(new RuntimeException("temporary failure")).when(userNotificationService).saveOrderCreatedNotification(event);
        when(orderNotificationFailureHandler.shouldMoveToDlq(message)).thenReturn(false);

        assertThatThrownBy(() -> orderNotificationEventListener.handle(event, message))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);

        verify(orderNotificationFailureHandler, never()).publishToDlq(eq(event), any(RuntimeException.class));
    }

    @Test
    void handle_publishesToDlqWhenRetryThresholdExceeded() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .qty(2)
                .unitPrice(1000)
                .totalPrice(2000)
                .build();

        Message message = MessageBuilder.withBody(new byte[0]).build();

        doThrow(new RuntimeException("permanent failure")).when(userNotificationService).saveOrderCreatedNotification(event);
        when(orderNotificationFailureHandler.shouldMoveToDlq(message)).thenReturn(true);

        orderNotificationEventListener.handle(event, message);

        verify(orderNotificationFailureHandler).publishToDlq(eq(event), any(RuntimeException.class));
    }
}
