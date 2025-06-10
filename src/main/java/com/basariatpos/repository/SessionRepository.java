package com.basariatpos.repository;

/**
 * Repository interface for managing database session context,
 * specifically for setting user and shift information at the database level.
 */
public interface SessionRepository {

    /**
     * Sets the current application user context in the database.
     * This is typically used to inform database triggers or RLS policies
     * about the user performing operations.
     *
     * @param userId The ID of the user to set in the database session context.
     *               Pass null to clear the user context.
     */
    void setDatabaseUserContext(Integer userId);

    /**
     * Sets the current application shift context in the database.
     * This can be used for auditing or context-specific operations within the database.
     *
     * @param shiftId The ID of the active shift to set in the database session context.
     *                Pass null to clear the shift context.
     */
    void setDatabaseShiftContext(Integer shiftId);
}
