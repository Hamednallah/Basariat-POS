package com.basariatpos.service.exception;

public class PurchaseOrderException extends RuntimeException {
    public PurchaseOrderException(String message) {
        super(message);
    }

    public PurchaseOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
