package com.basariatpos.service;

import com.basariatpos.model.ShiftDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.SessionRepository;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

/**
 * Manages the current user and active shift session within the application.
 * It also coordinates updating the database session context via SessionRepository.
 */
public class UserSessionService {

    private static final Logger logger = AppLogger.getLogger(UserSessionService.class);

    private UserDTO currentUser;
    private ShiftDTO activeShift;
    private final SessionRepository sessionRepository;

    /**
     * Constructs a UserSessionService with the given SessionRepository.
     *
     * @param sessionRepository The repository to use for setting database session context.
     *                          Cannot be null.
     */
    public UserSessionService(SessionRepository sessionRepository) {
        if (sessionRepository == null) {
            throw new IllegalArgumentException("SessionRepository cannot be null.");
        }
        this.sessionRepository = sessionRepository;
        logger.info("UserSessionService initialized.");
    }

    /**
     * Sets the current logged-in user.
     * Also updates the database session context for the user.
     *
     * @param user The UserDTO representing the logged-in user, or null to clear.
     */
    public void setCurrentUser(UserDTO user) {
        this.currentUser = user;
        Integer userId = (user != null) ? user.getUserId() : null;
        try {
            this.sessionRepository.setDatabaseUserContext(userId);
            if (user != null) {
                logger.info("Current user set to: {} (ID: {})", user.getUsername(), user.getUserId());
            } else {
                logger.info("Current user cleared.");
            }
        } catch (Exception e) {
            logger.error("Failed to set database user context for user ID: {}", userId, e);
            // Depending on policy, might want to clear currentUser if DB context fails
            // Or throw an exception to indicate login/context-setting failure
        }
    }

    public UserDTO getCurrentUser() {
        return currentUser;
    }

    /**
     * Clears the current logged-in user and the database session context.
     */
    public void clearCurrentUser() {
        setCurrentUser(null);
    }

    public boolean isUserLoggedIn() {
        return this.currentUser != null;
    }

    /**
     * Sets the currently active shift.
     * Also updates the database session context for the shift.
     *
     * @param shift The ShiftDTO representing the active shift, or null to clear.
     */
    public void setActiveShift(ShiftDTO shift) {
        this.activeShift = shift;
        Integer shiftId = (shift != null) ? shift.getShiftId() : null;
        try {
            this.sessionRepository.setDatabaseShiftContext(shiftId);
            if (shift != null) {
                logger.info("Active shift set to ID: {}", shift.getShiftId());
            } else {
                logger.info("Active shift cleared.");
            }
        } catch (Exception e) {
            logger.error("Failed to set database shift context for shift ID: {}", shiftId, e);
            // Policy consideration: clear activeShift or throw?
        }
    }

    public ShiftDTO getActiveShift() {
        return activeShift;
    }

    /**
     * Clears the active shift and the database session context for the shift.
     */
    public void clearActiveShift() {
        setActiveShift(null);
    }

    public boolean isShiftActive() {
        return this.activeShift != null;
    }
}
