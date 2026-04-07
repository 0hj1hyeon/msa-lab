package com.distributed.userservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserNotificationDto {
    private String userId;
    private String orderId;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
