package com.basariatpos.service.exception;

import java.util.Collections;
import java.util.List;

public class SalesOrderValidationException extends Exception { // Or RuntimeException depending on handling strategy
    private final List<String> errors;

    public SalesOrderValidationException(String message) {
        super(message);
        this.errors = Collections.singletonList(message);
    }

    public SalesOrderValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = Collections.unmodifiableList(errors);
    }

    public SalesOrderValidationException(String message, List<String> errors) {
        super(message);
        this.errors = Collections.unmodifiableList(errors);
    }

    public SalesOrderValidationException(String message, Throwable cause, List<String> errors) {
        super(message, cause);
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
