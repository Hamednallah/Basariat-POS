package com.basariatpos.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;

class AppLoggerTest {

    @Test
    void getLogger_byClass_should_return_non_null_logger() {
        // Act
        Logger logger = AppLogger.getLogger(AppLoggerTest.class);

        // Assert
        assertNotNull(logger, "Logger instance should not be null.");
    }

    @Test
    void getLogger_byClass_should_return_logger_with_correct_name() {
        // Arrange
        String expectedName = AppLoggerTest.class.getName();

        // Act
        Logger logger = AppLogger.getLogger(AppLoggerTest.class);

        // Assert
        assertEquals(expectedName, logger.getName(), "Logger name should match the class name.");
    }

    @Test
    void getLogger_byName_should_return_non_null_logger() {
        // Arrange
        String customLoggerName = "com.basariatpos.CustomLogger";

        // Act
        Logger logger = AppLogger.getLogger(customLoggerName);

        // Assert
        assertNotNull(logger, "Logger instance should not be null when fetched by name.");
    }

    @Test
    void getLogger_byName_should_return_logger_with_correct_name() {
        // Arrange
        String customLoggerName = "com.basariatpos.AnotherCustomLogger";

        // Act
        Logger logger = AppLogger.getLogger(customLoggerName);

        // Assert
        assertEquals(customLoggerName, logger.getName(), "Logger name should match the custom name provided.");
    }

    // Test for the private constructor for coverage, if desired.
    @Test
    void constructor_should_be_private() throws Exception {
        // Similar to DBManagerTest, this is a conceptual check.
        // A common approach is to use reflection to ensure it's private and cannot be instantiated.
        var constructor = AppLogger.class.getDeclaredConstructor();
        assertFalse(constructor.canAccess(null), "Constructor should not be accessible for utility class.");

        // Attempting to make it accessible and create an instance to ensure it's private by design
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            // If it's a true utility class, instantiation might still be simple.
            // The key is that it's not part of the public API.
        } catch (Exception e) {
            // Expected if the constructor throws an exception (e.g. AssertionError)
            // For a simple private constructor, no exception might be thrown here by newInstance() itself.
        }
        // The primary check is that it's declared private.
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
    }
}
