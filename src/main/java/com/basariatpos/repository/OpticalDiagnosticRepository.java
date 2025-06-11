package com.basariatpos.repository;

import com.basariatpos.model.OpticalDiagnosticDTO;
import java.util.List;
import java.util.Optional;

public interface OpticalDiagnosticRepository {

    /**
     * Saves (inserts or updates) an optical diagnostic record.
     * If diagnosticDto.getDiagnosticId() is 0, it's treated as an insert.
     * Otherwise, it's an update.
     * @param diagnosticDto The DTO to save.
     * @return The saved DTO, updated with a generated ID if it was an insert.
     */
    OpticalDiagnosticDTO save(OpticalDiagnosticDTO diagnosticDto);

    /**
     * Finds an optical diagnostic record by its ID.
     * @param diagnosticId The ID of the diagnostic record.
     * @return An Optional containing the DTO if found.
     */
    Optional<OpticalDiagnosticDTO> findById(int diagnosticId);

    /**
     * Finds all optical diagnostic records for a specific patient,
     * ordered by diagnostic date in descending order (most recent first).
     * @param patientId The ID of the patient.
     * @return A list of DTOs.
     */
    List<OpticalDiagnosticDTO> findByPatientId(int patientId);

    /**
     * Deletes an optical diagnostic record by its ID.
     * (Optional method, consider if hard deletes are allowed or if a soft delete/archive mechanism is preferred).
     * @param diagnosticId The ID of the diagnostic record to delete.
     */
    void deleteById(int diagnosticId); // Or return boolean for success
}
