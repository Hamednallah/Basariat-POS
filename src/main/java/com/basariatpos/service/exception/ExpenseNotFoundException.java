package com.basariatpos.service.exception;

public class ExpenseNotFoundException extends ExpenseException {
    public ExpenseNotFoundException(int expenseId) {
        super("Expense with ID " + expenseId + " not found.");
    }

    public ExpenseNotFoundException(String message) {
        super(message);
    }

    public ExpenseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
