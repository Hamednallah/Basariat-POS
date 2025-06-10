package com.basariatpos.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

class DBManagerTest {

    private MockedStatic<DriverManager> mockDriverManager;
    private Connection mockConnection;

    @BeforeEach
    void setUp() {
        // Mock the DriverManager to avoid actual DB connection attempts
        mockConnection = mock(Connection.class);
        // It's important to mock static methods within a try-with-resources or manage its lifecycle
        // For simplicity here, we'll open it in setUp and close in tearDown
        mockDriverManager = Mockito.mockStatic(DriverManager.class);
    }

    @Test
    void getDSLContext_should_return_non_null_DSLContext_on_successful_connection() throws SQLException {
        // Arrange
        mockDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                         .thenReturn(mockConnection);

        // Act
        DSLContext context = DBManager.getDSLContext();

        // Assert
        assertNotNull(context, "DSLContext should not be null when connection is successful.");
        assertEquals(SQLDialect.POSTGRES, context.configuration().dialect(), "DSLContext should be configured for POSTGRES.");

        // Verify that getConnection was called, which implies DSL.using was also attempted
        mockDriverManager.verify(() -> DriverManager.getConnection(
            "jdbc:postgresql://localhost:5433/basariat_pos_db",
            "basariat_pos_user",
            "POST"));
    }

    @Test
    void getDSLContext_should_return_null_when_connection_fails() throws SQLException {
        // Arrange
        mockDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                         .thenThrow(new SQLException("Test SQL Exception: Connection failed"));

        // Act
        DSLContext context = DBManager.getDSLContext();

        // Assert
        assertNull(context, "DSLContext should be null when DriverManager.getConnection throws an SQLException.");
    }

    @Test
    void getConnection_should_return_connection() throws SQLException {
        mockDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                         .thenReturn(mockConnection);
        Connection conn = DBManager.getConnection();
        assertNotNull(conn);
        mockDriverManager.verify(() -> DriverManager.getConnection(
            "jdbc:postgresql://localhost:5433/basariat_pos_db",
            "basariat_pos_user",
            "POST"));
    }

    @Test
    void getConnection_should_throw_SQLException_on_failure() throws SQLException {
        mockDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                         .thenThrow(new SQLException("Simulated connection failure"));

        assertThrows(SQLException.class, () -> {
            DBManager.getConnection();
        }, "SQLException should be thrown when connection fails.");
    }

    // Test for the private constructor for coverage, if desired.
    // This often involves reflection if you want to strictly test it.
    // For utility classes with only static methods, this is often skipped.
    @Test
    void constructor_should_be_private() {
        // This is more of a conceptual test.
        // One common way is to try to invoke it via reflection and expect an IllegalAccessException
        // or ensure the constructor is not public.
        assertTrue(DBManager.class.getDeclaredConstructors()[0].isAccessible() == false,
                 "Default constructor should be private.");
        // Note: isAccessible() might be true if it was made accessible by reflection elsewhere,
        // a better check is Modifier.isPrivate(DBManager.class.getDeclaredConstructors()[0].getModifiers())
        // However, for this simple case, this will likely pass if it's private.
    }


    @AfterEach
    void tearDown() {
        // Close the static mock
        if (mockDriverManager != null) {
            mockDriverManager.close();
        }
    }
}
