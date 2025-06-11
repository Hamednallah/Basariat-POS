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
     * @return An Optional containing the ShiftDTO if an active or paused shift is found, otherwise empty.
     */
    Optional<ShiftDTO> findActiveOrPausedShiftByUserId(int userId);

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
     * Resumes a paused shift.
     * This typically involves calling a database procedure.
     * @param shiftId The ID of the shift to resume.
     * @param userId The ID of the user resuming the shift (for validation/audit).
     * @throws org.jooq.exception.DataAccessException if the database procedure call fails.
     */
    void resumeShift(int shiftId, int userId);

    /**
     * Ends an active or paused shift.
     * This will be implemented in a later step.
     * @param shiftId The ID of the shift to end.
     * @param userId The ID of the user ending the shift.
     * @param closingFloat The closing cash float amount.
     * @param notes Optional notes for the shift closing.
     * @throws org.jooq.exception.DataAccessException if the database procedure call fails.
     */
    // void endShift(int shiftId, int userId, BigDecimal closingFloat, String notes);
}
