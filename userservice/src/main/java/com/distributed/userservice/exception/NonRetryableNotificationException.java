package com.distributed.userservice.exception;

public class NonRetryableNotificationException extends OrderNotificationProcessingException {

    public NonRetryableNotificationException(String message) {
        super(message);
    }
}
