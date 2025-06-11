package com.basariatpos.service.exception;

public class PatientServiceException extends RuntimeException {
    public PatientServiceException(String message) {
        super(message);
    }

    public PatientServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
