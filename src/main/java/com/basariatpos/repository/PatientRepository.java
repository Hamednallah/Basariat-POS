package com.basariatpos.repository;

import com.basariatpos.model.PatientDTO;
import java.util.List;
import java.util.Optional;

public interface PatientRepository {

    /**
     * Finds a patient by their auto-incremented primary ID.
     * @param id The patient_id.
     * @return Optional of PatientDTO.
     */
    Optional<PatientDTO> findById(int id);

    /**
     * Finds a patient by their system-generated patient ID (e.g., "PAT-00001").
     * @param systemId The system_patient_id.
     * @return Optional of PatientDTO.
     */
    Optional<PatientDTO> findBySystemPatientId(String systemId);

    /**
     * Finds a patient by their primary phone number.
     * Assumes phone numbers should be unique for active patients or across the system.
     * @param phoneNumber The phone number.
     * @return Optional of PatientDTO.
     */
    Optional<PatientDTO> findByPhoneNumber(String phoneNumber);

    /**
     * Searches for patients by a part of their full name (case-insensitive).
     * @param nameQuery The query string to search for in full names.
     * @return List of matching PatientDTOs.
     */
    List<PatientDTO> searchByName(String nameQuery);

    /**
     * Searches for patients by a part of their full name or phone number (case-insensitive).
     * @param query The query string to search for.
     * @return List of matching PatientDTOs.
     */
    List<PatientDTO> searchByNameOrPhone(String query);


    /**
     * Retrieves all patients. Consider pagination for large datasets in a real application.
     * @return List of all PatientDTOs.
     */
    List<PatientDTO> findAll();

    /**
     * Saves (inserts or updates) a patient.
     * If patientDto.getPatientId() is 0, it's an insert. SystemPatientId should be generated if not provided.
     * Otherwise, it's an update. Timestamps (createdAt, updatedAt) are handled.
     * @param patientDto The PatientDTO to save.
     * @return The saved PatientDTO, updated with generated ID/timestamps.
     */
    PatientDTO save(PatientDTO patientDto);

    /**
     * Checks if a system_patient_id already exists in the database.
     * @param systemId The system_patient_id to check.
     * @return true if the ID exists, false otherwise.
     */
    boolean systemIdExists(String systemId);

    /**
     * Deletes a patient by their ID.
     * Note: Consider implications (e.g., soft delete, archiving) in a real system.
     * For now, this implies a hard delete if implemented.
     * @param id The patient_id to delete.
     */
    // void deleteById(int id); // Optional, if hard delete is needed
}
