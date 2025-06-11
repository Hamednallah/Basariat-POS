package com.basariatpos.service;

import com.basariatpos.model.ShiftDTO;
import java.math.BigDecimal;
import java.util.Optional;

// Custom Exceptions for ShiftService
class ShiftException extends RuntimeException {
    public ShiftException(String message) { super(message); }
    public ShiftException(String message, Throwable cause) { super(message, cause); }
}

class ShiftNotFoundException extends ShiftException {
    public ShiftNotFoundException(int shiftId) { super("Shift with ID " + shiftId + " not found."); }
    public ShiftNotFoundException(String message) { super(message); }
}

class ShiftOperationException extends ShiftException {
    public ShiftOperationException(String message) { super(message); }
    public ShiftOperationException(String message, Throwable cause) { super(message, cause); }
}


public interface ShiftService {

    /**
     * Retrieves the currently active or paused shift for a specific user.
     * @param userId The ID of the user.
     * @return An Optional containing the ShiftDTO if found, otherwise empty.
     * @throws ShiftException if a service-level error occurs.
     */
    Optional<ShiftDTO> getActiveOrPausedShiftForUser(int userId) throws ShiftException;

    /**
     * Starts a new shift for the specified user.
     * @param userId The ID of the user starting the shift.
     * @param openingFloat The opening cash float for the shift.
     * @return The created ShiftDTO with all details including ID, start time, and status.
     * @throws ShiftOperationException if a user already has an active/paused shift or other business rule violation.
     * @throws ValidationException if openingFloat is invalid.
     * @throws ShiftException if a service-level error occurs.
     */
    ShiftDTO startNewShift(int userId, BigDecimal openingFloat) throws ShiftOperationException, ValidationException, ShiftException;

    /**
     * Pauses the currently active shift for a user.
     * @param shiftId The ID of the shift to pause.
     * @param userId The ID of the user performing the action (for validation).
     * @throws ShiftNotFoundException if the shift is not found or not active for the user.
     * @throws ShiftOperationException if the shift cannot be paused (e.g., not active).
     * @throws ShiftException if a service-level error occurs.
     */
    void pauseActiveShift(int shiftId, int userId) throws ShiftNotFoundException, ShiftOperationException, ShiftException;

    /**
     * Resumes a previously paused shift for a user.
     * @param shiftId The ID of the shift to resume.
     * @param userId The ID of the user performing the action.
     * @return The resumed ShiftDTO with updated status and potentially resume time if tracked.
     * @throws ShiftNotFoundException if the shift is not found or not paused for the user.
     * @throws ShiftOperationException if the shift cannot be resumed (e.g., already active or ended).
     * @throws ShiftException if a service-level error occurs.
     */
    ShiftDTO resumePausedShift(int shiftId, int userId) throws ShiftNotFoundException, ShiftOperationException, ShiftException;

    // endShift method will be added in a later task for Sprint 1
    // void endShift(int shiftId, int userId, BigDecimal closingFloat, String notes) throws ShiftNotFoundException, ShiftOperationException, ShiftException;
}
