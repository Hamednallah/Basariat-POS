package com.basariatpos.service.exception;

public class BankNameNotFoundException extends Exception { // Checked exception
    public BankNameNotFoundException(int bankNameId) {
        super("Bank name with ID " + bankNameId + " not found.");
    }

    public BankNameNotFoundException(String message) {
        super(message);
    }

    public BankNameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
