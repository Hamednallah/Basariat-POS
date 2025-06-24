package com.basariatpos.service.exception;

public class PermissionDeniedException extends Exception { // Or RuntimeException
    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(String action, String reason) {
        super("Permission denied to perform action: " + action + ". Reason: " + reason);
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
