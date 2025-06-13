package com.basariatpos.service.exception;

public class InventoryItemServiceException extends RuntimeException {
    public InventoryItemServiceException(String message) {
        super(message);
    }

    public InventoryItemServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
