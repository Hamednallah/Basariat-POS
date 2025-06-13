package com.basariatpos.service.exception;

import java.util.Collections;
import java.util.List;

public class PurchaseOrderValidationException extends PurchaseOrderException {
    private final List<String> errors;

    public PurchaseOrderValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public PurchaseOrderValidationException(List<String> errors) {
        super("Purchase order data validation failed. Errors: " + (errors != null ? String.join(", ", errors) : "None"));
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public List<String> getErrors() {
        return errors;
    }
}
