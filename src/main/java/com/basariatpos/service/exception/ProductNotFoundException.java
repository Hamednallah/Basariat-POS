package com.basariatpos.service.exception;

public class ProductNotFoundException extends ProductServiceException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(int productId) {
        super("Product with ID " + productId + " not found.");
    }

    public ProductNotFoundException(String productCode) {
        super("Product with code '" + productCode + "' not found.");
    }
}
