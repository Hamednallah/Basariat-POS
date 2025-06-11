package com.basariatpos.service.exception;

public class PatientAlreadyExistsException extends PatientServiceException {
    public PatientAlreadyExistsException(String message) {
        super(message);
    }

    public PatientAlreadyExistsException(String fieldName, String fieldValue) {
        super("Patient already exists with " + fieldName + ": " + fieldValue);
    }
}
