package com.basariatpos.service.exception;

import java.util.Collections;
import java.util.List;

public class PatientValidationException extends PatientServiceException {
    private final List<String> errors;

    public PatientValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public PatientValidationException(List<String> errors) {
        super("Patient data validation failed. Errors: " + (errors != null ? String.join(", ", errors) : "None"));
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public List<String> getErrors() {
        return errors;
    }
}
