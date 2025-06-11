package com.basariatpos.service;

import com.basariatpos.model.OpticalDiagnosticDTO;
import com.basariatpos.service.exception.DiagnosticNotFoundException;
import com.basariatpos.service.exception.DiagnosticServiceException;
import com.basariatpos.service.exception.DiagnosticValidationException;
// Assuming ValidationException is now DiagnosticValidationException or a shared one

import java.util.List;
import java.util.Optional;

public interface OpticalDiagnosticService {

    /**
     * Records a new optical diagnostic for a patient.
     * @param diagnosticDto DTO containing diagnostic details. `patientId` and `diagnosticDate` are mandatory.
     *                      `createdByUserId` will be set from session.
     * @return The saved OpticalDiagnosticDTO, updated with generated ID and createdAt timestamp.
     * @throws DiagnosticValidationException if input data is invalid.
     * @throws PatientNotFoundException if the patientId in DTO does not correspond to an existing patient (handled by DB foreign key, but service can check).
     * @throws DiagnosticServiceException for other service or repository level errors.
     */
    OpticalDiagnosticDTO recordDiagnostic(OpticalDiagnosticDTO diagnosticDto)
        throws DiagnosticValidationException, DiagnosticServiceException; // Add PatientNotFoundException if service checks it

    /**
     * Updates an existing optical diagnostic record.
     * @param diagnosticDto DTO containing updated details. `diagnosticId` must be valid.
     * @return The updated OpticalDiagnosticDTO.
     * @throws DiagnosticValidationException if input data is invalid.
     * @throws DiagnosticNotFoundException if the diagnostic record with the given ID is not found.
     * @throws DiagnosticServiceException for other service or repository level errors.
     */
    OpticalDiagnosticDTO updateDiagnostic(OpticalDiagnosticDTO diagnosticDto)
        throws DiagnosticValidationException, DiagnosticNotFoundException, DiagnosticServiceException;

    /**
     * Retrieves all diagnostic records for a specific patient, ordered by date descending.
     * @param patientId The ID of the patient.
     * @return List of OpticalDiagnosticDTOs.
     * @throws DiagnosticServiceException if an error occurs.
     */
    List<OpticalDiagnosticDTO> getDiagnosticsForPatient(int patientId) throws DiagnosticServiceException;

    /**
     * Retrieves a specific diagnostic record by its ID.
     * @param diagnosticId The ID of the diagnostic record.
     * @return Optional of OpticalDiagnosticDTO.
     * @throws DiagnosticServiceException if an error occurs.
     */
    Optional<OpticalDiagnosticDTO> getDiagnosticById(int diagnosticId) throws DiagnosticServiceException;

    /**
     * Deletes a specific diagnostic record by its ID. (Optional based on business rules)
     * Consider implications: if a diagnostic is linked to sales orders, deletion might be restricted.
     * @param diagnosticId The ID of the diagnostic record to delete.
     * @throws DiagnosticNotFoundException if the record is not found.
     * @throws DiagnosticServiceException (or a more specific e.g., DiagnosticInUseException) if deletion is not permitted.
     */
    void deleteDiagnostic(int diagnosticId) throws DiagnosticNotFoundException, DiagnosticServiceException; // Add DiagnosticInUseException if applicable
}
