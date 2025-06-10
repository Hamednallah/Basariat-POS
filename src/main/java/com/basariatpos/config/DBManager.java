package com.basariatpos.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBManager {

    private static final Logger logger = Logger.getLogger(DBManager.class.getName());

    // Hardcoded for now as per requirements, eventually load from AppConfigLoader or environment
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/basariat_pos_db";
    private static final String DB_USER = "basariat_pos_user";
    private static final String DB_PASSWORD = "POST"; // Be careful with hardcoding sensitive data

    /**
     * Private constructor to prevent instantiation.
     * This class provides static methods for database access.
     */
    private DBManager() {
        // Utility class
    }

    /**
     * Establishes and returns a direct JDBC connection to the database.
     * <p>
     * Note: In a production environment, a connection pool (e.g., HikariCP)
     * should be used instead of creating direct connections each time.
     * This method is provided for basic connectivity and jOOQ setup.
     * </p>
     *
     * @return A {@link Connection} to the database.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Ensure the PostgreSQL driver is registered (though often automatic with JDBC 4.0+)
            // Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to establish database connection.", e);
            throw e; // Re-throw to allow caller to handle
        }
        // catch (ClassNotFoundException e) {
        //     logger.log(Level.SEVERE, "PostgreSQL JDBC Driver not found.", e);
        //     throw new SQLException("PostgreSQL JDBC Driver not found.", e);
        // }
    }

    /**
     * Creates and returns a jOOQ DSLContext for interacting with the database.
     * <p>
     * This method uses the {@link #getConnection()} method to obtain a database connection.
     * It configures the DSLContext for PostgreSQL.
     * </p>
     * <p>
     * Future enhancements:
     * - Configure {@link org.jooq.conf.Settings} for more advanced jOOQ features.
     * - Integrate with a connection pool for managing connections efficiently.
     * </p>
     *
     * @return A {@link DSLContext} configured for PostgreSQL.
     *         Returns {@code null} if a connection cannot be established (though typically an exception is preferred).
     */
    public static DSLContext getDSLContext() {
        try {
            Connection connection = getConnection();
            return DSL.using(connection, SQLDialect.POSTGRES);
        } catch (SQLException e) {
            // Logging is already done in getConnection()
            // Depending on application's error handling strategy, could return null or throw a runtime exception
            logger.log(Level.SEVERE, "Could not create DSLContext due to SQL Exception.", e);
            return null; // Or throw new RuntimeException("Failed to initialize DSLContext", e);
        }
    }

    // Optional: A method to close resources if not using try-with-resources with DSLContext
    // public static void close(Connection connection) {
    //     if (connection != null) {
    //         try {
    //             connection.close();
    //         } catch (SQLException e) {
    //             logger.log(Level.WARNING, "Error closing database connection.", e);
    //         }
    //     }
    // }
}
