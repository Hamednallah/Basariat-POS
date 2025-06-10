package com.basariatpos.service;

import com.basariatpos.model.CenterProfileDTO;
import com.basariatpos.repository.CenterProfileRepository;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;
import org.jooq.exception.DataAccessException; // Assuming repository might throw this

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CenterProfileService {

    private static final Logger logger = AppLogger.getLogger(CenterProfileService.class);
    private final CenterProfileRepository centerProfileRepository;

    public CenterProfileService(CenterProfileRepository centerProfileRepository) {
        if (centerProfileRepository == null) {
            throw new IllegalArgumentException("CenterProfileRepository cannot be null.");
        }
        this.centerProfileRepository = centerProfileRepository;
    }

    /**
     * Saves the center profile after validation.
     *
     * @param profileDto The DTO containing profile information.
     * @return true if save was successful, false otherwise (e.g., validation failure).
     * @throws ProfileServiceException if a critical error occurs during saving (e.g., database issue).
     */
    public boolean saveProfile(CenterProfileDTO profileDto) throws ProfileServiceException {
        if (profileDto == null) {
            throw new IllegalArgumentException("CenterProfileDTO cannot be null for saving.");
        }

        List<String> validationErrors = validateProfile(profileDto);
        if (!validationErrors.isEmpty()) {
            String errorMsg = "Validation failed: " + String.join(", ", validationErrors);
            logger.warn("Center profile validation failed for center '{}': {}", profileDto.getCenterName(), errorMsg);
            // It's often better to throw a specific validation exception here
            // that the controller can catch and interpret.
            // For now, returning false as per original thought, but an exception is cleaner.
             throw new ProfileValidationException("Validation failed", validationErrors);
            // return false;
        }

        try {
            centerProfileRepository.save(profileDto);
            logger.info("Center profile saved successfully for center: {}", profileDto.getCenterName());
            return true;
        } catch (DataAccessException e) {
            logger.error("Database error while saving center profile for center '{}': {}", profileDto.getCenterName(), e.getMessage(), e);
            throw new ProfileServiceException("Failed to save profile due to a database error.", e);
        } catch (Exception e) {
            logger.error("Unexpected error while saving center profile for center '{}': {}", profileDto.getCenterName(), e.getMessage(), e);
            throw new ProfileServiceException("An unexpected error occurred while saving the profile.", e);
        }
    }

    /**
     * Retrieves the center profile.
     *
     * @return An {@link Optional} containing the {@link CenterProfileDTO}, or empty if not configured.
     * @throws ProfileServiceException if a critical error occurs during retrieval.
     */
    public Optional<CenterProfileDTO> getCenterProfile() throws ProfileServiceException {
        try {
            return centerProfileRepository.getProfile();
        } catch (DataAccessException e) {
            logger.error("Database error while retrieving center profile: {}", e.getMessage(), e);
            throw new ProfileServiceException("Failed to retrieve profile due to a database error.", e);
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving center profile: {}", e.getMessage(), e);
            throw new ProfileServiceException("An unexpected error occurred while retrieving the profile.", e);
        }
    }

    /**
     * Checks if the center profile is configured.
     *
     * @return true if the profile exists, false otherwise.
     * @throws ProfileServiceException if a critical error occurs during the check.
     */
    public boolean isProfileConfigured() throws ProfileServiceException {
        try {
            return centerProfileRepository.exists();
        } catch (DataAccessException e) {
            logger.error("Database error while checking if profile is configured: {}", e.getMessage(), e);
            throw new ProfileServiceException("Failed to check profile configuration due to a database error.", e);
        } catch (Exception e) {
            logger.error("Unexpected error while checking if profile is configured: {}", e.getMessage(), e);
            throw new ProfileServiceException("An unexpected error occurred while checking profile configuration.", e);
        }
    }

    private List<String> validateProfile(CenterProfileDTO profileDto) {
        List<String> errors = new ArrayList<>();

        if (isBlank(profileDto.getCenterName())) {
            errors.add("Center Name is required."); // Later use i18n keys: MessageProvider.getString("validation.centerprofile.centerName.required")
        }
        if (isBlank(profileDto.getPhonePrimary())) {
            errors.add("Primary Phone is required.");
        }
        if (isBlank(profileDto.getCurrencySymbol())) {
            errors.add("Currency Symbol is required.");
        }
        if (isBlank(profileDto.getCurrencyCode())) {
            errors.add("Currency Code is required.");
        }
        // Add more specific validations as needed (e.g., email format, phone format, lengths)
        // Example:
        // if (!isBlank(profileDto.getEmailAddress()) && !isValidEmail(profileDto.getEmailAddress())) {
        //     errors.add("Invalid email format.");
        // }
        return errors;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // private boolean isValidEmail(String email) { /* Basic regex or library validation */ return true; }
}

// Custom Exceptions for the service layer

class ProfileServiceException extends Exception {
    public ProfileServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

class ProfileValidationException extends ProfileServiceException {
    private final List<String> validationErrors;
    public ProfileValidationException(String message, List<String> validationErrors) {
        super(message, null); // No underlying cause, just validation errors
        this.validationErrors = validationErrors;
    }
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
