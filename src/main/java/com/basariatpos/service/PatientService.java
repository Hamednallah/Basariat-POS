package com.basariatpos.service;

import com.basariatpos.model.PatientDTO;
import com.basariatpos.service.exception.PatientAlreadyExistsException;
import com.basariatpos.service.exception.PatientNotFoundException;
import com.basariatpos.service.exception.PatientServiceException;
import com.basariatpos.service.exception.PatientValidationException; // Assuming this is created
// Reusing ValidationException from other service or define common one if more specific needed for patient
// For now, let's assume a PatientValidationException exists or can be created.

import java.util.List;
import java.util.Optional;

public interface PatientService {

    /**
     * Creates a new patient.
     * Validates input, generates a system patient ID, sets createdByUserId.
     * @param patientDto DTO containing patient details (fullName, phoneNumber required).
     * @return The created PatientDTO with assigned IDs and timestamps.
     * @throws PatientValidationException if input data is invalid.
     * @throws PatientAlreadyExistsException if a patient with the same phone number already exists.
     * @throws PatientServiceException for other service or repository level errors.
     */
    PatientDTO createPatient(PatientDTO patientDto)
        throws PatientValidationException, PatientAlreadyExistsException, PatientServiceException;

    /**
     * Updates an existing patient's details.
     * @param patientDto DTO containing updated patient details. patientId must be valid.
     * @return The updated PatientDTO.
     * @throws PatientValidationException if input data is invalid.
     * @throws PatientNotFoundException if the patient with the given ID is not found.
     * @throws PatientServiceException for other service or repository level errors.
     */
    PatientDTO updatePatient(PatientDTO patientDto)
        throws PatientValidationException, PatientNotFoundException, PatientServiceException;

    /**
     * Updates a patient's WhatsApp consent status.
     * @param patientId The ID of the patient.
     * @param hasConsented The new consent status.
     * @throws PatientNotFoundException if the patient is not found.
     * @throws PatientServiceException for other errors.
     */
    void updateWhatsAppConsent(int patientId, boolean hasConsented)
        throws PatientNotFoundException, PatientServiceException;

    /**
     * Retrieves a patient by their primary database ID.
     * @param patientId The patient's primary ID.
     * @return Optional of PatientDTO.
     */
    Optional<PatientDTO> getPatientById(int patientId);

    /**
     * Retrieves a patient by their system-generated ID (e.g., "PAT-00001").
     * @param systemId The patient's system ID.
     * @return Optional of PatientDTO.
     */
    Optional<PatientDTO> getPatientBySystemId(String systemId);

    /**
     * Retrieves a patient by their phone number.
     * @param phoneNumber The patient's phone number.
     * @return Optional of PatientDTO.
     */
    Optional<PatientDTO> getPatientByPhoneNumber(String phoneNumber);


    /**
     * Searches for patients whose full name contains the given query string.
     * @param nameQuery The query string for the name search.
     * @return List of matching PatientDTOs.
     */
    List<PatientDTO> searchPatientsByName(String nameQuery);

    /**
     * Searches for patients whose full name or phone number contains the given query string.
     * @param query The query string for the search.
     * @return List of matching PatientDTOs.
     */
    List<PatientDTO> searchPatientsByNameOrPhone(String query);


    /**
     * Retrieves all patients.
     * @return List of all PatientDTOs.
     */
    List<PatientDTO> getAllPatients();
}
