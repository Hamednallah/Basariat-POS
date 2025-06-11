package com.basariatpos.repository;

import com.basariatpos.model.ApplicationSettingDTO;
import java.util.List;
import java.util.Optional;

public interface ApplicationSettingsRepository {

    /**
     * Finds an application setting by its key.
     * @param key The unique key of the setting.
     * @return An Optional containing the ApplicationSettingDTO if found, otherwise empty.
     */
    Optional<ApplicationSettingDTO> findByKey(String key);

    /**
     * Retrieves all application settings.
     * @return A list of all ApplicationSettingDTOs.
     */
    List<ApplicationSettingDTO> findAll();

    /**
     * Saves (inserts or updates) an application setting.
     * If a setting with the given key exists, it's updated. Otherwise, it's inserted.
     * @param settingDto The ApplicationSettingDTO to save.
     * @return The saved ApplicationSettingDTO.
     */
    ApplicationSettingDTO save(ApplicationSettingDTO settingDto);

    /**
     * Deletes an application setting by its key.
     * Generally, settings are not deleted but their values are changed.
     * This method is provided for completeness if direct deletion is ever needed.
     * @param key The key of the setting to delete.
     * @return true if deletion was successful, false otherwise.
     */
    // boolean deleteByKey(String key); // Optional: if hard delete is ever needed
}
