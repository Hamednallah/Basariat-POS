package com.basariatpos.repository;

import com.basariatpos.model.ShiftDTO;
import java.math.BigDecimal;
import java.util.Optional;

public interface ShiftRepository {

    /**
     * Finds a shift by its ID.
     * @param shiftId The ID of the shift.
     * @return An Optional containing the ShiftDTO if found, otherwise empty.
     */
    Optional<ShiftDTO> findById(int shiftId);

    /**
     * Finds the currently active or paused shift for a given user.
     * A user should ideally have at most one shift that is not 'Ended'.
     * @param userId The ID of the user.
     * @return An Optional containing the ShiftDTO if an active, paused, or interrupted shift is found, otherwise empty.
     */
    Optional<ShiftDTO> findIncompleteShiftByUserId(int userId);

    /**
     * Starts a new shift for the given user with the specified opening float.
     * This typically involves calling a database procedure.
     * @param userId The ID of the user starting the shift.
     * @param openingFloat The opening cash float amount.
     * @return The ID of the newly created shift.
     * @throws org.jooq.exception.DataAccessException if the database procedure call fails or returns an error.
     */
    int startShift(int userId, BigDecimal openingFloat);

    /**
     * Pauses an active shift.
     * This typically involves calling a database procedure.
     * @param shiftId The ID of the shift to pause.
     * @param userId The ID of the user pausing the shift (for validation/audit).
     * @throws org.jooq.exception.DataAccessException if the database procedure call fails.
     */
    void pauseShift(int shiftId, int userId);

    /**
     * Resumes a paused or interrupted shift.
     * This typically involves calling a database procedure.
     * @param shiftId The ID of the shift to resume.
     * @param userId The ID of the user resuming the shift (for validation/audit).
     * @throws org.jooq.exception.DataAccessException if the database procedure call fails.
     */
    void resumeShift(int shiftId, int userId);

    /**
     * Ends an active, paused, or interrupted shift.
     * This involves calling a database procedure.
     * @param shiftId The ID of the shift to end.
     * @param endedByUserId The ID of the user ending the shift.
     * @param closingCashCounted The actual cash counted at the end of the shift.
     * @param notes Optional notes for the shift closing.
     * @throws org.jooq.exception.DataAccessException if the database procedure call fails.
     */
    void endShift(int shiftId, int endedByUserId, BigDecimal closingCashCounted, String notes);
}
