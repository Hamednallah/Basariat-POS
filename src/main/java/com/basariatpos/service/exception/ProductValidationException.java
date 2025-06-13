package com.basariatpos.service.exception;

import java.util.Collections;
import java.util.List;

public class ProductValidationException extends ProductServiceException {
    private final List<String> errors;

    public ProductValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public ProductValidationException(List<String> errors) {
        super("Product data validation failed. Errors: " + (errors != null ? String.join(", ", errors) : "None"));
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public List<String> getErrors() {
        return errors;
    }
}
