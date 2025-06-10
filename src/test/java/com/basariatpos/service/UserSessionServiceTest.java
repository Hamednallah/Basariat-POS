package com.basariatpos.service;

import com.basariatpos.model.ShiftDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private SessionRepository mockSessionRepository;

    @InjectMocks
    private UserSessionService userSessionService;

    private UserDTO testUser;
    private ShiftDTO testShift;

    @BeforeEach
    void setUp() {
        // Test data
        testUser = new UserDTO(1, "testuser", "Test User Full Name", "Admin");
        testShift = new ShiftDTO(101, testUser.getUserId(), OffsetDateTime.now());

        // It's good practice to reset mocks if they are reused across tests,
        // though with @InjectMocks a new UserSessionService is typically created per test run by MockitoExtension.
        // reset(mockSessionRepository); // Usually not needed with MockitoExtension per test method
    }

    @Test
    void constructor_should_throw_illegalArgumentException_when_repository_is_null() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UserSessionService(null);
        });
    }

    @Test
    void setCurrentUser_should_update_currentUser_and_call_repository() {
        // Act
        userSessionService.setCurrentUser(testUser);

        // Assert
        assertEquals(testUser, userSessionService.getCurrentUser(), "Current user should be set.");
        assertTrue(userSessionService.isUserLoggedIn(), "User should be marked as logged in.");
        verify(mockSessionRepository).setDatabaseUserContext(testUser.getUserId());
    }

    @Test
    void setCurrentUser_with_null_should_clear_currentUser_and_call_repository_with_null() {
        // Arrange: ensure a user is initially set, to test clearing
        userSessionService.setCurrentUser(testUser);
        assertEquals(testUser, userSessionService.getCurrentUser()); // Pre-condition

        // Act
        userSessionService.setCurrentUser(null);

        // Assert
        assertNull(userSessionService.getCurrentUser(), "Current user should be cleared.");
        assertFalse(userSessionService.isUserLoggedIn(), "User should be marked as not logged in.");
        verify(mockSessionRepository).setDatabaseUserContext(null);
    }

    @Test
    void clearCurrentUser_should_clear_currentUser_and_call_repository_with_null() {
        // Arrange: ensure a user is initially set
        userSessionService.setCurrentUser(testUser);
        assertEquals(testUser, userSessionService.getCurrentUser()); // Pre-condition

        // Act
        userSessionService.clearCurrentUser();

        // Assert
        assertNull(userSessionService.getCurrentUser(), "Current user should be cleared via clearCurrentUser.");
        assertFalse(userSessionService.isUserLoggedIn(), "User should be marked as not logged in after clearCurrentUser.");
        verify(mockSessionRepository).setDatabaseUserContext(null); // Verifies it was called by clearCurrentUser
    }


    @Test
    void setActiveShift_should_update_activeShift_and_call_repository() {
        // Act
        userSessionService.setActiveShift(testShift);

        // Assert
        assertEquals(testShift, userSessionService.getActiveShift(), "Active shift should be set.");
        assertTrue(userSessionService.isShiftActive(), "Shift should be marked as active.");
        verify(mockSessionRepository).setDatabaseShiftContext(testShift.getShiftId());
    }

    @Test
    void setActiveShift_with_null_should_clear_activeShift_and_call_repository_with_null() {
        // Arrange: ensure a shift is initially set
        userSessionService.setActiveShift(testShift);
        assertEquals(testShift, userSessionService.getActiveShift()); // Pre-condition

        // Act
        userSessionService.setActiveShift(null);

        // Assert
        assertNull(userSessionService.getActiveShift(), "Active shift should be cleared.");
        assertFalse(userSessionService.isShiftActive(), "Shift should be marked as not active.");
        verify(mockSessionRepository).setDatabaseShiftContext(null);
    }

    @Test
    void clearActiveShift_should_clear_activeShift_and_call_repository_with_null() {
        // Arrange: ensure a shift is initially set
        userSessionService.setActiveShift(testShift);
        assertEquals(testShift, userSessionService.getActiveShift()); // Pre-condition

        // Act
        userSessionService.clearActiveShift();

        // Assert
        assertNull(userSessionService.getActiveShift(), "Active shift should be cleared via clearActiveShift.");
        assertFalse(userSessionService.isShiftActive(), "Shift should be marked as not active after clearActiveShift.");
        verify(mockSessionRepository).setDatabaseShiftContext(null); // Verifies it was called by clearActiveShift
    }

    @Test
    void isUserLoggedIn_should_return_false_initially() {
        assertFalse(userSessionService.isUserLoggedIn(), "User should not be logged in initially.");
    }

    @Test
    void isShiftActive_should_return_false_initially() {
        assertFalse(userSessionService.isShiftActive(), "Shift should not be active initially.");
    }

    @Test
    void setCurrentUser_should_handle_repository_exception_gracefully() {
        // Arrange
        doThrow(new RuntimeException("Database unavailable")).when(mockSessionRepository).setDatabaseUserContext(testUser.getUserId());

        // Act & Assert
        // Depending on policy, the application might choose to let the exception propagate
        // or catch it and log, potentially affecting the currentUser state.
        // Current UserSessionService logs the error and user is still set.
        userSessionService.setCurrentUser(testUser);

        assertEquals(testUser, userSessionService.getCurrentUser(), "User should still be set in service even if DB context fails.");
        assertTrue(userSessionService.isUserLoggedIn());
        // Log verification would be useful here if a testable logging framework is in place.
    }

    @Test
    void setActiveShift_should_handle_repository_exception_gracefully() {
        // Arrange
        doThrow(new RuntimeException("Database unavailable")).when(mockSessionRepository).setDatabaseShiftContext(testShift.getShiftId());

        // Act & Assert
        userSessionService.setActiveShift(testShift);

        assertEquals(testShift, userSessionService.getActiveShift(), "Shift should still be set in service even if DB context fails.");
        assertTrue(userSessionService.isShiftActive());
    }
}
