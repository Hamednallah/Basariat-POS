package com.basariatpos.service.exception;

public class DiagnosticServiceException extends RuntimeException {
    public DiagnosticServiceException(String message) {
        super(message);
    }

    public DiagnosticServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
