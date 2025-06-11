package com.basariatpos.model;

import java.util.Objects;

public class ApplicationSettingDTO {
    private String settingKey;   // Primary Key
    private String settingValue;
    private String description;  // Optional, can be null

    // Default constructor
    public ApplicationSettingDTO() {}

    // Full constructor
    public ApplicationSettingDTO(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }

    // Getters
    public String getSettingKey() {
        return settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ApplicationSettingDTO{" +
               "settingKey='" + settingKey + '\'' +
               ", settingValue='" + settingValue + '\'' +
               ", description='" + description + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationSettingDTO that = (ApplicationSettingDTO) o;
        return Objects.equals(settingKey, that.settingKey) &&
               Objects.equals(settingValue, that.settingValue) &&
               Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingKey, settingValue, description);
    }
}
