package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.exception.NonRetryableNotificationException;
import com.distributed.userservice.exception.RetryableNotificationException;
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

    @Mock
    private OrderCompensationEventPublisher orderCompensationEventPublisher;

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

        RetryableNotificationException exception =
                new RetryableNotificationException("temporary failure", new RuntimeException("db down"));
        doThrow(exception).when(userNotificationService).saveOrderCreatedNotification(event);
        when(orderNotificationFailureHandler.determineFailureAction(message, exception))
                .thenReturn(OrderNotificationFailureHandler.FailureAction.RETRY);

        assertThatThrownBy(() -> orderNotificationEventListener.handle(event, message))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);

        verify(orderNotificationFailureHandler, never()).publishToDlq(eq(event), any(RetryableNotificationException.class));
        verify(orderCompensationEventPublisher, never()).publishNotificationCompensation(eq(event), any(RetryableNotificationException.class));
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

        RetryableNotificationException exception =
                new RetryableNotificationException("permanent failure", new RuntimeException("db down"));
        doThrow(exception).when(userNotificationService).saveOrderCreatedNotification(event);
        when(orderNotificationFailureHandler.determineFailureAction(message, exception))
                .thenReturn(OrderNotificationFailureHandler.FailureAction.MOVE_TO_DLQ);

        orderNotificationEventListener.handle(event, message);

        verify(orderNotificationFailureHandler).publishToDlq(eq(event), any(RetryableNotificationException.class));
        verify(orderCompensationEventPublisher).publishNotificationCompensation(eq(event), any(RetryableNotificationException.class));
    }

    @Test
    void handle_publishesToDlqImmediatelyForNonRetryableException() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .qty(2)
                .unitPrice(1000)
                .totalPrice(2000)
                .build();

        Message message = MessageBuilder.withBody(new byte[0]).build();

        NonRetryableNotificationException exception =
                new NonRetryableNotificationException("user not found");
        doThrow(exception).when(userNotificationService).saveOrderCreatedNotification(event);
        when(orderNotificationFailureHandler.determineFailureAction(message, exception))
                .thenReturn(OrderNotificationFailureHandler.FailureAction.MOVE_TO_DLQ);

        orderNotificationEventListener.handle(event, message);

        verify(orderNotificationFailureHandler).publishToDlq(eq(event), any(NonRetryableNotificationException.class));
        verify(orderCompensationEventPublisher).publishNotificationCompensation(eq(event), any(NonRetryableNotificationException.class));
    }
}
