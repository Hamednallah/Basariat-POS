package com.basariatpos.service.exception;

public class WhatsAppNotificationException extends RuntimeException {
    public WhatsAppNotificationException(String message) {
        super(message);
    }

    public WhatsAppNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
