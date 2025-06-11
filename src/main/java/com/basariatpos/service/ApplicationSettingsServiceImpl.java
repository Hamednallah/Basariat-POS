package com.basariatpos.service;

import com.basariatpos.model.ApplicationSettingDTO;
import com.basariatpos.repository.ApplicationSettingsRepository;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.util.ArrayList; // For ValidationException
import java.util.List;
import java.util.Optional;

public class ApplicationSettingsServiceImpl implements ApplicationSettingsService {

    private static final Logger logger = AppLogger.getLogger(ApplicationSettingsServiceImpl.class);
    private final ApplicationSettingsRepository settingsRepository;

    public ApplicationSettingsServiceImpl(ApplicationSettingsRepository settingsRepository) {
        if (settingsRepository == null) {
            throw new IllegalArgumentException("ApplicationSettingsRepository cannot be null.");
        }
        this.settingsRepository = settingsRepository;
    }

    @Override
    public Optional<ApplicationSettingDTO> getApplicationSetting(String key) throws SettingException {
        validateKey(key);
        try {
            return settingsRepository.findByKey(key);
        } catch (Exception e) {
            logger.error("Error getting application setting for key '{}': {}", key, e.getMessage(), e);
            throw new SettingException("Could not retrieve setting.", e);
        }
    }

    @Override
    public String getSettingValue(String key, String defaultValue) throws SettingException {
        validateKey(key);
        try {
            return settingsRepository.findByKey(key)
                                     .map(ApplicationSettingDTO::getSettingValue)
                                     .orElse(defaultValue);
        } catch (Exception e) {
            logger.error("Error getting setting value for key '{}': {}", key, e.getMessage(), e);
            // If only DataAccessException is expected from repo, catch that specifically.
            throw new SettingException("Could not retrieve setting value.", e);
        }
    }

    @Override
    public int getIntSettingValue(String key, int defaultValue) throws SettingException {
        String valueStr = getSettingValue(key, null);
        if (valueStr == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            logger.warn("Setting key '{}' with value '{}' is not a valid integer. Returning default value {}.", key, valueStr, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public boolean getBooleanSettingValue(String key, boolean defaultValue) throws SettingException {
        String valueStr = getSettingValue(key, null);
        if (valueStr == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(valueStr); // "true" (case-insensitive) is true, everything else is false.
    }


    @Override
    public List<ApplicationSettingDTO> getAllApplicationSettings() throws SettingException {
        try {
            return settingsRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting all application settings: {}", e.getMessage(), e);
            throw new SettingException("Could not retrieve all settings.", e);
        }
    }

    @Override
    public void updateSettingValue(String key, String newValue) throws SettingNotFoundException, ValidationException, SettingException {
        validateKey(key);
        validateValue(newValue); // Basic validation for the value itself

        try {
            ApplicationSettingDTO setting = settingsRepository.findByKey(key)
                .orElseThrow(() -> new SettingNotFoundException(key));

            setting.setSettingValue(newValue);
            settingsRepository.save(setting);
            logger.info("Application setting '{}' updated successfully.", key);
        } catch (SettingNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating setting value for key '{}': {}", key, e.getMessage(), e);
            throw new SettingException("Could not update setting value.", e);
        }
    }

    @Override
    public ApplicationSettingDTO saveApplicationSetting(ApplicationSettingDTO settingDto) throws ValidationException, SettingException {
        if (settingDto == null) {
            throw new ValidationException("Setting DTO cannot be null.", new ArrayList<>());
        }
        validateKey(settingDto.getSettingKey());
        validateValue(settingDto.getSettingValue());
        // Description validation if any (e.g., length)

        try {
            return settingsRepository.save(settingDto);
        } catch (Exception e) {
            logger.error("Error saving application setting '{}': {}", settingDto.getSettingKey(), e.getMessage(), e);
            throw new SettingException("Could not save application setting.", e);
        }
    }


    private void validateKey(String key) throws ValidationException {
        if (key == null || key.trim().isEmpty()) {
            // Assuming ValidationException is from com.basariatpos.service package or common
            throw new com.basariatpos.service.ValidationException("Setting key cannot be empty.", List.of("Setting key required."));
        }
        // Add more key validation if needed (e.g. format, allowed characters)
    }

    private void validateValue(String value) throws ValidationException {
         if (value == null) { // Null might be permissible for some settings, meaning "not set"
            return;
        }
        // Example: Max length validation
        if (value.length() > 1024) { // Arbitrary limit
             throw new com.basariatpos.service.ValidationException("Setting value exceeds maximum length.", List.of("Value too long."));
        }
    }
}
