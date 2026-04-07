package com.distributed.userservice.service;

import com.distributed.userservice.domain.UserNotification;
import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.dto.UserNotificationDto;
import com.distributed.userservice.repository.UserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @InjectMocks
    private UserNotificationService userNotificationService;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void saveOrderCreatedNotification_savesNotification() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .qty(2)
                .unitPrice(1000)
                .totalPrice(2000)
                .build();

        userNotificationService.saveOrderCreatedNotification(event);

        ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
        verify(userNotificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("user-123");
        assertThat(captor.getValue().getOrderId()).isEqualTo("order-1");
        assertThat(captor.getValue().isRead()).isFalse();
    }

    @Test
    void getNotifications_returnsNotificationDtos() {
        UserNotification userNotification = new UserNotification();
        userNotification.setUserId("user-123");
        userNotification.setOrderId("order-1");
        userNotification.setTitle("주문이 생성되었습니다.");
        userNotification.setMessage("상품 product-1 주문이 정상적으로 생성되었습니다.");
        userNotification.setRead(false);
        userNotification.setCreatedAt(LocalDateTime.of(2026, 4, 7, 10, 0));

        when(userNotificationRepository.findByUserIdOrderByCreatedAtDesc("user-123"))
                .thenReturn(List.of(userNotification));

        List<UserNotificationDto> result = userNotificationService.getNotifications("user-123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo("order-1");
        assertThat(result.get(0).getTitle()).isEqualTo("주문이 생성되었습니다.");
    }
}
