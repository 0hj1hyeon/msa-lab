package com.distributed.userservice.service;

import com.distributed.userservice.domain.UserNotification;
import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.dto.UserNotificationDto;
import com.distributed.userservice.repository.UserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;

    public UserNotificationService(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    public void saveOrderCreatedNotification(OrderCreatedEvent event) {
        UserNotification userNotification = new UserNotification();
        userNotification.setUserId(event.getUserId());
        userNotification.setOrderId(event.getOrderId());
        userNotification.setTitle("주문이 생성되었습니다.");
        userNotification.setMessage(String.format("상품 %s 주문이 정상적으로 생성되었습니다.", event.getProductId()));
        userNotification.setRead(false);
        userNotification.setCreatedAt(LocalDateTime.now());

        userNotificationRepository.save(userNotification);
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
}
