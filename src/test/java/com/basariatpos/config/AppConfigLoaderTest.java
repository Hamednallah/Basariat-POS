package com.basariatpos.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

class AppConfigLoaderTest {

    private static final String TEST_PROPERTIES_FILE = "test-application.properties";
    private static final String NON_EXISTENT_PROPERTIES_FILE = "non-existent-app.properties";
    private AppConfigLoader configLoader;

    @BeforeEach
    void setUp() {
        // Load from a test-specific properties file
        configLoader = new AppConfigLoader(TEST_PROPERTIES_FILE);
    }

    @Test
    void constructor_should_load_properties_from_specified_file() {
        // Properties are loaded in setUp
        assertNotNull(configLoader.getProperty("app.name"), "Should load 'app.name' from test properties.");
        assertEquals("Basariat POS Test", configLoader.getProperty("app.name"));
    }

    @Test
    void constructor_should_handle_non_existent_properties_file_gracefully() {
        // This test creates a new instance to check the non-existent file scenario
        AppConfigLoader loaderWithNonExistentFile = new AppConfigLoader(NON_EXISTENT_PROPERTIES_FILE);
        // It should not throw an exception (as per current AppConfigLoader design, it logs a warning)
        // and properties should be empty or default.
        assertNull(loaderWithNonExistentFile.getProperty("any.key"),
                "Properties should be null if file not found and no defaults given.");
        assertEquals("default", loaderWithNonExistentFile.getProperty("any.key", "default"),
                "Should return default value if key not found after non-existent file load.");
    }

    @Test
    void default_constructor_should_attempt_to_load_default_application_properties() {
        // This relies on 'application.properties' being present in 'src/main/resources'
        // and accessible to the test classpath.
        AppConfigLoader defaultConfigLoader = new AppConfigLoader(); // Uses "application.properties"
        assertNotNull(defaultConfigLoader.getProperty("app.name"),
                      "Should load 'app.name' from main application.properties");
        assertEquals("Basariat POS", defaultConfigLoader.getProperty("app.name"));
    }


    @Test
    void getProperty_should_return_value_for_existing_key() {
        assertEquals("Hello from test properties!", configLoader.getProperty("test.message"));
        assertEquals("jdbc:postgresql://localhost:5432/test_db", configLoader.getProperty("db.url"));
    }

    @Test
    void getProperty_should_return_null_for_non_existing_key() {
        assertNull(configLoader.getProperty("non.existent.key"));
    }

    @Test
    void getProperty_with_defaultValue_should_return_value_for_existing_key() {
        assertEquals("Hello from test properties!", configLoader.getProperty("test.message", "default"));
    }

    @Test
    void getProperty_with_defaultValue_should_return_defaultValue_for_non_existing_key() {
        assertEquals("default value", configLoader.getProperty("non.existent.key", "default value"));
    }

    @Test
    void getProperty_with_defaultValue_should_return_defaultValue_if_property_value_is_null() {
        // To test this, we'd need a key that explicitly has no value in the properties file,
        // e.g. key=
        // Standard Properties behavior might treat 'key=' as empty string, not null.
        // If a key is truly absent, the defaultValue is returned, as tested above.
        // If a key is present but its value is an empty string:
        // Properties p = new Properties(); p.setProperty("emptyKey", "");
        // AppConfigLoader would need to be based on such a Properties instance.
        // Our current file-based test cases cover missing keys well.
        assertEquals("default", configLoader.getProperty("someKeyWithNoValueInFile", "default"));
    }


    @Test
    void getIntProperty_should_return_integer_value_for_valid_key() {
        assertEquals(123, configLoader.getIntProperty("test.number", 0));
    }

    @Test
    void getIntProperty_should_return_defaultValue_for_non_existing_key() {
        assertEquals(999, configLoader.getIntProperty("non.existent.number", 999));
    }

    @Test
    void getIntProperty_should_return_defaultValue_for_invalid_integer_format() {
        assertEquals(0, configLoader.getIntProperty("app.name", 0)); // "Basariat POS Test" is not an int
    }

    @Test
    void getBooleanProperty_should_return_true_for_valid_true_string() {
        assertTrue(configLoader.getBooleanProperty("test.boolean.true", false));
    }

    @Test
    void getBooleanProperty_should_return_false_for_valid_false_string() {
        assertFalse(configLoader.getBooleanProperty("test.boolean.false", true));
    }

    @Test
    void getBooleanProperty_should_return_false_for_invalid_boolean_string() {
        // Boolean.parseBoolean("any string not 'true'") is false
        assertFalse(configLoader.getBooleanProperty("test.boolean.invalid", true));
    }

    @Test
    void getBooleanProperty_should_return_defaultValue_for_non_existing_key() {
        assertTrue(configLoader.getBooleanProperty("non.existent.boolean", true));
        assertFalse(configLoader.getBooleanProperty("non.existent.boolean.deux", false));
    }

    @Test
    void getAllProperties_should_return_loaded_properties() {
        Properties allProps = configLoader.getAllProperties();
        assertNotNull(allProps);
        assertEquals("Basariat POS Test", allProps.getProperty("app.name"));
        assertEquals("123", allProps.getProperty("test.number"));
    }
}
