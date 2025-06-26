package com.basariatpos.service.exception;

import java.util.Collections;
import java.util.List;

public class ExpenseValidationException extends ExpenseException {
    private final List<String> errors;

    public ExpenseValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    public ExpenseValidationException(List<String> errors) {
        super(formatErrors(errors));
        this.errors = Collections.unmodifiableList(errors);
    }

    public ExpenseValidationException(String singleError) {
        super(singleError);
        this.errors = Collections.singletonList(singleError);
    }

    public List<String> getErrors() {
        return errors;
    }

    private static String formatErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Expense data validation failed.";
        }
        if (errors.size() == 1) {
            return errors.get(0);
        }
        return "Expense data validation failed: " + String.join("; ", errors);
    }
}
