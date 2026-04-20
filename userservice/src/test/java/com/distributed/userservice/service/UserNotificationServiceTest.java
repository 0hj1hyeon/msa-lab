package com.distributed.userservice.service;

import com.distributed.userservice.domain.UserNotification;
import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.dto.UserNotificationDto;
import com.distributed.userservice.exception.NonRetryableNotificationException;
import com.distributed.userservice.exception.RetryableNotificationException;
import com.distributed.userservice.repository.UserRepository;
import com.distributed.userservice.repository.UserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private UserRepository userRepository;

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

        when(userRepository.findByUserId("user-123")).thenReturn(Optional.of(new com.distributed.userservice.domain.User()));

        userNotificationService.saveOrderCreatedNotification(event);

        ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
        verify(userNotificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("user-123");
        assertThat(captor.getValue().getOrderId()).isEqualTo("order-1");
        assertThat(captor.getValue().isRead()).isFalse();
    }

    @Test
    void saveOrderCreatedNotification_throwsNonRetryableExceptionWhenUserIdIsBlank() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId(" ")
                .productId("product-1")
                .build();

        assertThatThrownBy(() -> userNotificationService.saveOrderCreatedNotification(event))
                .isInstanceOf(NonRetryableNotificationException.class)
                .hasMessage("알림 이벤트에 userId가 없습니다.");
    }

    @Test
    void saveOrderCreatedNotification_throwsNonRetryableExceptionWhenUserDoesNotExist() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("missing-user")
                .productId("product-1")
                .build();

        when(userRepository.findByUserId("missing-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userNotificationService.saveOrderCreatedNotification(event))
                .isInstanceOf(NonRetryableNotificationException.class)
                .hasMessage("알림 이벤트에 해당하는 사용자가 없습니다.");
    }

    @Test
    void saveOrderCreatedNotification_throwsRetryableExceptionWhenRepositoryFails() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .build();

        when(userRepository.findByUserId("user-123")).thenReturn(Optional.of(new com.distributed.userservice.domain.User()));
        doThrow(new DataAccessResourceFailureException("db unavailable"))
                .when(userNotificationRepository)
                .save(org.mockito.ArgumentMatchers.any(UserNotification.class));

        assertThatThrownBy(() -> userNotificationService.saveOrderCreatedNotification(event))
                .isInstanceOf(RetryableNotificationException.class)
                .hasMessage("알림 저장 중 일시적인 DB 오류가 발생했습니다.");
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
