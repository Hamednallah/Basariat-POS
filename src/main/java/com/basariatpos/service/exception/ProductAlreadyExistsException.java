package com.basariatpos.service.exception;

public class ProductAlreadyExistsException extends ProductServiceException {
    public ProductAlreadyExistsException(String message) {
        super(message);
    }

    public ProductAlreadyExistsException(String productCode) {
        super("Product with code '" + productCode + "' already exists.");
    }
}
