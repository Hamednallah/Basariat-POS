package com.basariatpos.service;

import com.basariatpos.model.ShiftDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.ShiftRepository;
import com.basariatpos.repository.UserRepository; // To get username for DTO
import com.basariatpos.util.AppLogger;
import org.jooq.exception.DataAccessException; // From jOOQ, caught from repository
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList; // For ValidationException
import java.util.List;    // For ValidationException
import java.util.Optional;

public class ShiftServiceImpl implements ShiftService {

    private static final Logger logger = AppLogger.getLogger(ShiftServiceImpl.class);
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository; // To fetch username

    public ShiftServiceImpl(ShiftRepository shiftRepository, UserRepository userRepository) {
        if (shiftRepository == null) {
            throw new IllegalArgumentException("ShiftRepository cannot be null.");
        }
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null.");
        }
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<ShiftDTO> getActiveOrPausedShiftForUser(int userId) throws ShiftException {
        try {
            return shiftRepository.findActiveOrPausedShiftByUserId(userId);
        } catch (Exception e) {
            logger.error("Error getting active/paused shift for user ID {}: {}", userId, e.getMessage(), e);
            throw new ShiftException("Could not retrieve shift status for user.", e);
        }
    }

    @Override
    public ShiftDTO startNewShift(int userId, BigDecimal openingFloat) throws ShiftOperationException, ValidationException, ShiftException {
        if (openingFloat == null || openingFloat.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Opening float must be a non-negative value.", List.of("Invalid opening float."));
        }

        try {
            // Check if user already has an active/paused shift
            Optional<ShiftDTO> existingShift = shiftRepository.findActiveOrPausedShiftByUserId(userId);
            if (existingShift.isPresent()) {
                throw new ShiftOperationException("User ID " + userId + " already has an active or paused shift (ID: " + existingShift.get().getShiftId() + ").");
            }

            int newShiftId = shiftRepository.startShift(userId, openingFloat);

            // Fetch the newly created shift to return full DTO
            Optional<ShiftDTO> newShiftOpt = shiftRepository.findById(newShiftId);
            if (newShiftOpt.isEmpty()) {
                 logger.error("Failed to retrieve newly started shift with ID {} for user {}", newShiftId, userId);
                 throw new ShiftException("Shift started but could not retrieve its details.");
            }
            // Username should be populated by findById if repository joins correctly
            return newShiftOpt.get();

        } catch (DataAccessException e) { // Catch exceptions from DB procedure (e.g., p_user_already_has_active_shift)
             logger.error("Database error starting new shift for user ID {}: {}", userId, e.getMessage(), e);
             // Attempt to map common DB errors to more specific service exceptions
             if (e.getMessage() != null && e.getMessage().toLowerCase().contains("user already has an active shift")) { // Example error text
                 throw new ShiftOperationException("User already has an active shift.", e);
             }
             throw new ShiftOperationException("Could not start new shift due to a database issue.", e);
        } catch (ShiftOperationException | ValidationException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error starting new shift for user ID {}: {}", userId, e.getMessage(), e);
            throw new ShiftException("Unexpected error while starting shift.", e);
        }
    }

    @Override
    public void pauseActiveShift(int shiftId, int userId) throws ShiftNotFoundException, ShiftOperationException, ShiftException {
        try {
            ShiftDTO shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ShiftNotFoundException(shiftId));

            if (shift.getUserId() != userId) {
                throw new ShiftOperationException("User ID " + userId + " is not authorized to pause shift ID " + shiftId + ".");
            }
            if (!"Active".equalsIgnoreCase(shift.getStatus())) {
                throw new ShiftOperationException("Shift ID " + shiftId + " is not active, cannot be paused. Current status: " + shift.getStatus());
            }

            shiftRepository.pauseShift(shiftId, userId);
        } catch (ShiftNotFoundException | ShiftOperationException e) {
            throw e;
        } catch (DataAccessException e){
            logger.error("Database error pausing shift ID {}: {}", shiftId, e.getMessage(), e);
            throw new ShiftOperationException("Could not pause shift due to a database issue.", e);
        }
        catch (Exception e) {
            logger.error("Unexpected error pausing shift ID {}: {}", shiftId, e.getMessage(), e);
            throw new ShiftException("Unexpected error while pausing shift.", e);
        }
    }

    @Override
    public ShiftDTO resumePausedShift(int shiftId, int userId) throws ShiftNotFoundException, ShiftOperationException, ShiftException {
        try {
            ShiftDTO shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ShiftNotFoundException(shiftId));

            if (shift.getUserId() != userId) {
                throw new ShiftOperationException("User ID " + userId + " is not authorized to resume shift ID " + shiftId + ".");
            }
            if (!"Paused".equalsIgnoreCase(shift.getStatus())) {
                throw new ShiftOperationException("Shift ID " + shiftId + " is not paused, cannot be resumed. Current status: " + shift.getStatus());
            }

            shiftRepository.resumeShift(shiftId, userId);
            // Fetch updated shift details
            return shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ShiftException("Shift resumed but could not retrieve updated details."));
        } catch (ShiftNotFoundException | ShiftOperationException e) {
            throw e;
        } catch (DataAccessException e){
            logger.error("Database error resuming shift ID {}: {}", shiftId, e.getMessage(), e);
            throw new ShiftOperationException("Could not resume shift due to a database issue.", e);
        }
        catch (Exception e) {
            logger.error("Unexpected error resuming shift ID {}: {}", shiftId, e.getMessage(), e);
            throw new ShiftException("Unexpected error while resuming shift.", e);
        }
    }
}
