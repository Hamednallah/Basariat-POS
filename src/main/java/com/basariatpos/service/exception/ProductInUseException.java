package com.basariatpos.service.exception;

public class ProductInUseException extends ProductServiceException {
    public ProductInUseException(String message) {
        super(message);
    }

    public ProductInUseException(int productId) {
        super("Product with ID " + productId + " is currently in use (e.g., in inventory or sales orders) and cannot be deleted.");
    }
}
