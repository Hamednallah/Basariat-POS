package com.basariatpos.service.exception;

public class SalesOrderServiceException extends RuntimeException {
    public SalesOrderServiceException(String message) {
        super(message);
    }

    public SalesOrderServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
