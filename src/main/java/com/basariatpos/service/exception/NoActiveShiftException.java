package com.basariatpos.service.exception;

public class NoActiveShiftException extends RuntimeException {
    public NoActiveShiftException(String message) {
        super(message);
    }

    public NoActiveShiftException(String message, Throwable cause) {
        super(message, cause);
    }
}
