package com.basariatpos.service.exception;

public class CategoryNotFoundException extends Exception { // Checked exception
    public CategoryNotFoundException(int categoryId) {
        super("Expense category with ID " + categoryId + " not found.");
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
