package com.basariatpos.service;

import com.basariatpos.model.ApplicationSettingDTO;
import java.util.List;
import java.util.Optional;

// Custom Exception for ApplicationSettingsService
class SettingException extends RuntimeException {
    public SettingException(String message) { super(message); }
    public SettingException(String message, Throwable cause) { super(message, cause); }
}

class SettingNotFoundException extends SettingException {
    public SettingNotFoundException(String key) { super("Setting with key '" + key + "' not found.");}
}

// Reusing ValidationException from UserService.java or common package, or define if needed:
// class ValidationException extends SettingException { ... }


public interface ApplicationSettingsService {

    /**
     * Retrieves an application setting by its key.
     * @param key The unique key of the setting.
     * @return An Optional containing the ApplicationSettingDTO if found.
     * @throws SettingException if a service-level error occurs.
     */
    Optional<ApplicationSettingDTO> getApplicationSetting(String key) throws SettingException;

    /**
     * Retrieves the value of an application setting by its key.
     * If the setting is not found, returns the provided default value.
     * @param key The unique key of the setting.
     * @param defaultValue The value to return if the setting is not found.
     * @return The setting's value or the defaultValue.
     * @throws SettingException if a service-level error occurs during lookup (other than not found).
     */
    String getSettingValue(String key, String defaultValue) throws SettingException;

    /**
     * Retrieves an integer value of an application setting by its key.
     * If not found or not a valid integer, returns default.
     * @param key The unique key of the setting.
     * @param defaultValue The value to return if not found or invalid.
     * @return The integer setting's value or default.
     * @throws SettingException if a service-level error occurs.
     */
    int getIntSettingValue(String key, int defaultValue) throws SettingException;

    /**
     * Retrieves a boolean value of an application setting by its key.
     * "true" (case-insensitive) is true. If not found or other value, returns default.
     * @param key The unique key of the setting.
     * @param defaultValue The value to return if not found or not "true".
     * @return The boolean setting's value or default.
     * @throws SettingException if a service-level error occurs.
     */
    boolean getBooleanSettingValue(String key, boolean defaultValue) throws SettingException;


    /**
     * Retrieves all application settings.
     * @return A list of ApplicationSettingDTOs.
     * @throws SettingException if a service-level error occurs.
     */
    List<ApplicationSettingDTO> getAllApplicationSettings() throws SettingException;

    /**
     * Updates the value of an existing application setting.
     * @param key The key of the setting to update.
     * @param newValue The new value for the setting.
     * @throws SettingNotFoundException if the setting key does not exist.
     * @throws ValidationException if the new value is invalid.
     * @throws SettingException if a service-level error occurs.
     */
    void updateSettingValue(String key, String newValue) throws SettingNotFoundException, ValidationException, SettingException;

    /**
     * Saves an application setting (creates if not exists, updates if exists).
     * This is a more general save method than updateSettingValue if creating new settings is allowed from service.
     * For this task, focus is on editing existing ones, so updateSettingValue is primary.
     * @param settingDto The DTO of the setting to save.
     * @return The saved DTO.
     * @throws ValidationException if setting data is invalid.
     * @throws SettingException if a service-level error occurs.
     */
    ApplicationSettingDTO saveApplicationSetting(ApplicationSettingDTO settingDto) throws ValidationException, SettingException;
}
