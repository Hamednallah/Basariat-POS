package com.basariatpos.service.exception;

public class SalesOrderNotFoundException extends Exception { // Or RuntimeException
    public SalesOrderNotFoundException(String message) {
        super(message);
    }

    public SalesOrderNotFoundException(int salesOrderId) {
        super("Sales Order with ID " + salesOrderId + " not found.");
    }

    public SalesOrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
