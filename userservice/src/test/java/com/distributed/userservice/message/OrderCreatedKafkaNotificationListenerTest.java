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

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCreatedKafkaNotificationListenerTest {

    @Mock
    private UserNotificationService userNotificationService;

    @InjectMocks
    private OrderCreatedKafkaNotificationListener orderCreatedKafkaNotificationListener;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void handle_savesNotificationFromKafkaEvent() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .qty(2)
                .unitPrice(1000)
                .totalPrice(2000)
                .build();

        orderCreatedKafkaNotificationListener.handle(event);

        verify(userNotificationService).saveOrderCreatedNotification(event);
    }
}
