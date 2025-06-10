package com.basariatpos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility wrapper for obtaining SLF4J Logger instances.
 * This promotes consistency in logger instantiation across the application.
 */
public final class AppLogger {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AppLogger() {
        // Utility class
    }

    /**
     * Gets an SLF4J Logger instance for the specified class.
     *
     * @param clazz The class for which the logger is to be created.
     * @return An {@link Logger} instance.
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Gets an SLF4J Logger instance for the specified name.
     * This can be useful for creating loggers not directly tied to a class,
     * e.g., for specific functional areas.
     *
     * @param name The custom name for the logger.
     * @return An {@link Logger} instance.
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }
}
