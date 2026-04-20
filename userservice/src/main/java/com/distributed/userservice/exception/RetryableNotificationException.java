package com.distributed.userservice.exception;

public class RetryableNotificationException extends OrderNotificationProcessingException {

    public RetryableNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
