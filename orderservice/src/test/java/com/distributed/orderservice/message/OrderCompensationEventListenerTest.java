package com.distributed.orderservice.message;

import com.distributed.orderservice.dto.OrderCompensationRequestedEvent;
import com.distributed.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCompensationEventListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderCompensationEventListener orderCompensationEventListener;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void handle_compensatesOrder() {
        OrderCompensationRequestedEvent event = OrderCompensationRequestedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .reason("notification failed")
                .failedStep("NOTIFICATION")
                .failureType("RetryableNotificationException")
                .build();

        orderCompensationEventListener.handle(event);

        verify(orderService).compensateOrder(event);
    }
}
