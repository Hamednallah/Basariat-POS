package com.basariatpos.service.exception;

import java.util.Collections;
import java.util.List;

public class PaymentValidationException extends Exception { // Consider RuntimeException
    private final List<String> errors;

    public PaymentValidationException(String message) {
        super(message);
        this.errors = Collections.singletonList(message);
    }

    public PaymentValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = Collections.unmodifiableList(errors);
    }

    public PaymentValidationException(String message, List<String> errors) {
        super(message);
        this.errors = Collections.unmodifiableList(errors);
    }

    public PaymentValidationException(String message, Throwable cause, List<String> errors) {
        super(message, cause);
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
