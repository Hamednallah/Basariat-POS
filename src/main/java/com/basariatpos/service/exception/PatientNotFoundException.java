package com.basariatpos.service.exception;

public class PatientNotFoundException extends PatientServiceException {
    public PatientNotFoundException(String message) {
        super(message);
    }

    public PatientNotFoundException(int patientId) {
        super("Patient with ID " + patientId + " not found.");
    }
     public PatientNotFoundException(String identifier, boolean isSystemId) {
        super("Patient with " + (isSystemId ? "system ID" : "identifier") + " '" + identifier + "' not found.");
    }
}
