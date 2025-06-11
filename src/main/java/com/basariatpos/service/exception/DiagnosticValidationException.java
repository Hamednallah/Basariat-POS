package com.basariatpos.service.exception;

import java.util.Collections;
import java.util.List;

public class DiagnosticValidationException extends DiagnosticServiceException {
    private final List<String> errors;

    public DiagnosticValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public DiagnosticValidationException(List<String> errors) {
        super("Optical diagnostic data validation failed. Errors: " + (errors != null ? String.join(", ", errors) : "None"));
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public List<String> getErrors() {
        return errors;
    }
}
