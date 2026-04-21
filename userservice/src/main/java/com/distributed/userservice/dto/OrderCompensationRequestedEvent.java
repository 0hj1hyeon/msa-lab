package com.distributed.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompensationRequestedEvent {
    private String orderId;
    private String userId;
    private String reason;
    private String failedStep;
    private String failureType;
}
