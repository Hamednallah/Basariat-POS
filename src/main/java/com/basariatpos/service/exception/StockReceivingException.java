package com.basariatpos.service.exception;

public class StockReceivingException extends PurchaseOrderException {
    public StockReceivingException(String message) {
        super(message);
    }

    public StockReceivingException(String message, Throwable cause) {
        super(message, cause);
    }
}
