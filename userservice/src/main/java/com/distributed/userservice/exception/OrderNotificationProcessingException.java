package com.distributed.userservice.exception;

public abstract class OrderNotificationProcessingException extends RuntimeException {

    protected OrderNotificationProcessingException(String message) {
        super(message);
    }

    protected OrderNotificationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
