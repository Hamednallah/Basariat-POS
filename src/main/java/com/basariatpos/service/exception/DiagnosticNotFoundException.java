package com.basariatpos.service.exception;

public class DiagnosticNotFoundException extends DiagnosticServiceException {
    public DiagnosticNotFoundException(String message) {
        super(message);
    }

    public DiagnosticNotFoundException(int diagnosticId) {
        super("Optical diagnostic record with ID " + diagnosticId + " not found.");
    }
}
