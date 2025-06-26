package com.basariatpos.service.exception;

public class PaymentException extends Exception { // Consider RuntimeException if unchecked is preferred
    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
