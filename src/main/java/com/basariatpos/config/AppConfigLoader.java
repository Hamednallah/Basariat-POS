package com.basariatpos.config;

import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfigLoader {

    private static final Logger logger = AppLogger.getLogger(AppConfigLoader.class);
    private static final String DEFAULT_PROPERTIES_FILE = "application.properties";
    private final Properties properties;

    /**
     * Loads application configuration from the default properties file (application.properties)
     * found in the classpath.
     */
    public AppConfigLoader() {
        this(DEFAULT_PROPERTIES_FILE);
    }

    /**
     * Loads application configuration from a specified properties file.
     *
     * @param propertiesFileName The name of the properties file to load from the classpath.
     */
    public AppConfigLoader(String propertiesFileName) {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (input == null) {
                // Log a warning instead of an error to allow the application to proceed
                // with default values or if the file is optional for some configurations.
                logger.warn("Unable to find '{}' in the classpath. Using default or empty configuration.", propertiesFileName);
                // Optionally, throw an exception if the file is critical:
                // throw new FileNotFoundException("Property file '" + propertiesFileName + "' not found in the classpath");
                return;
            }
            properties.load(input);
            logger.info("Successfully loaded configuration from '{}'", propertiesFileName);
        } catch (IOException ex) {
            logger.error("IOException while trying to load configuration from '{}'. Error: {}", propertiesFileName, ex.getMessage(), ex);
            // Depending on the application's needs, you might re-throw this as a runtime exception
            // or allow the application to continue with empty/default properties.
        }
    }

    /**
     * Retrieves a property value by its key.
     *
     * @param key The key of the property.
     * @return The property value, or null if the key is not found.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Retrieves a property value by its key, returning a default value if the key is not found.
     *
     * @param key          The key of the property.
     * @param defaultValue The value to return if the key is not found or the property is null.
     * @return The property value, or the defaultValue if not found.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Retrieves an integer property value by its key.
     *
     * @param key          The key of the property.
     * @param defaultValue The value to return if the key is not found or the property is not a valid integer.
     * @return The integer property value, or the defaultValue.
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Property '{}' with value '{}' is not a valid integer. Returning default value '{}'.", key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Retrieves a boolean property value by its key.
     * "true" (case-insensitive) is considered true, anything else is false.
     *
     * @param key          The key of the property.
     * @param defaultValue The value to return if the key is not found.
     * @return The boolean property value, or the defaultValue.
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Returns the loaded Properties object.
     * Useful if other parts of the application need direct access to all properties.
     * @return The Properties object.
     */
    public Properties getAllProperties() {
        return properties;
    }
}
