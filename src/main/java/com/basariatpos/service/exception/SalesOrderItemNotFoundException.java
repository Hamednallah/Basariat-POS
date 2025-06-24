package com.basariatpos.service.exception;

public class SalesOrderItemNotFoundException extends Exception { // Or RuntimeException
    public SalesOrderItemNotFoundException(String message) {
        super(message);
    }

    public SalesOrderItemNotFoundException(int soItemId) {
        super("Sales Order Item with ID " + soItemId + " not found.");
    }

    public SalesOrderItemNotFoundException(int salesOrderId, int soItemId) {
        super("Sales Order Item with ID " + soItemId + " not found in Sales Order ID " + salesOrderId + ".");
    }

    public SalesOrderItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
