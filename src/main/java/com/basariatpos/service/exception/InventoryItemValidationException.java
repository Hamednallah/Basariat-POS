package com.basariatpos.service.exception;

import java.util.Collections;
import java.util.List;

public class InventoryItemValidationException extends InventoryItemServiceException {
    private final List<String> errors;

    public InventoryItemValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public InventoryItemValidationException(List<String> errors) {
        super("Inventory item data validation failed. Errors: " + (errors != null ? String.join(", ", errors) : "None"));
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public List<String> getErrors() {
        return errors;
    }
}
