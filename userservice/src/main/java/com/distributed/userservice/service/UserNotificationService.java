package com.distributed.userservice.service;

import com.distributed.userservice.domain.UserNotification;
import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.dto.UserNotificationDto;
import com.distributed.userservice.exception.NonRetryableNotificationException;
import com.distributed.userservice.exception.RetryableNotificationException;
import com.distributed.userservice.repository.UserRepository;
import com.distributed.userservice.repository.UserNotificationRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    public UserNotificationService(UserNotificationRepository userNotificationRepository,
                                   UserRepository userRepository) {
        this.userNotificationRepository = userNotificationRepository;
        this.userRepository = userRepository;
    }

    public void saveOrderCreatedNotification(OrderCreatedEvent event) {
        validateEvent(event);
        validateUserExists(event.getUserId());

        UserNotification userNotification = new UserNotification();
        userNotification.setUserId(event.getUserId());
        userNotification.setOrderId(event.getOrderId());
        userNotification.setTitle("주문이 생성되었습니다.");
        userNotification.setMessage(String.format("상품 %s 주문이 정상적으로 생성되었습니다.", event.getProductId()));
        userNotification.setRead(false);
        userNotification.setCreatedAt(LocalDateTime.now());

        try {
            userNotificationRepository.save(userNotification);
        } catch (DataAccessException exception) {
            throw new RetryableNotificationException("알림 저장 중 일시적인 DB 오류가 발생했습니다.", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<UserNotificationDto> getNotifications(String userId) {
        return userNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private UserNotificationDto toDto(UserNotification userNotification) {
        return UserNotificationDto.builder()
                .userId(userNotification.getUserId())
                .orderId(userNotification.getOrderId())
                .title(userNotification.getTitle())
                .message(userNotification.getMessage())
                .read(userNotification.isRead())
                .createdAt(userNotification.getCreatedAt())
                .build();
    }

    private void validateEvent(OrderCreatedEvent event) {
        if (!StringUtils.hasText(event.getUserId())) {
            throw new NonRetryableNotificationException("알림 이벤트에 userId가 없습니다.");
        }

        if (!StringUtils.hasText(event.getOrderId())) {
            throw new NonRetryableNotificationException("알림 이벤트에 orderId가 없습니다.");
        }

        if (!StringUtils.hasText(event.getProductId())) {
            throw new NonRetryableNotificationException("알림 이벤트에 productId가 없습니다.");
        }
    }

    private void validateUserExists(String userId) {
        if (userRepository.findByUserId(userId).isEmpty()) {
            throw new NonRetryableNotificationException("알림 이벤트에 해당하는 사용자가 없습니다.");
        }
    }
}
