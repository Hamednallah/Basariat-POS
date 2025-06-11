package com.basariatpos.service;

import com.basariatpos.model.ShiftDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.ShiftRepository;
import com.basariatpos.repository.UserRepository;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceImplTest {

    @Mock private ShiftRepository mockShiftRepository;
    @Mock private UserRepository mockUserRepository;

    @InjectMocks
    private ShiftServiceImpl shiftService;

    private UserDTO testUser;
    private ShiftDTO testShift;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO(1, "testuser", "Test User", "Cashier");
        testShift = new ShiftDTO(101, 1, "testuser", OffsetDateTime.now(), null, "Active", new BigDecimal("100.00"));
    }

    @Test
    void constructor_nullShiftRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ShiftServiceImpl(null, mockUserRepository));
    }

    @Test
    void constructor_nullUserRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ShiftServiceImpl(mockShiftRepository, null));
    }

    // --- getActiveOrPausedShiftForUser ---
    @Test
    void getActiveOrPausedShiftForUser_shiftExists_returnsDto() throws ShiftException {
        when(mockShiftRepository.findActiveOrPausedShiftByUserId(1)).thenReturn(Optional.of(testShift));
        Optional<ShiftDTO> result = shiftService.getActiveOrPausedShiftForUser(1);
        assertTrue(result.isPresent());
        assertEquals(testShift.getShiftId(), result.get().getShiftId());
    }

    // --- startNewShift ---
    @Test
    void startNewShift_validInput_returnsNewShiftDto() throws Exception {
        BigDecimal openingFloat = new BigDecimal("150.00");
        int newShiftId = 102;

        when(mockShiftRepository.findActiveOrPausedShiftByUserId(1)).thenReturn(Optional.empty()); // No existing shift
        when(mockShiftRepository.startShift(1, openingFloat)).thenReturn(newShiftId);

        // Prepare DTO that findById would return after shift start
        ShiftDTO newShiftDtoFromRepo = new ShiftDTO(newShiftId, 1, "testuser", OffsetDateTime.now(), null, "Active", openingFloat);
        when(mockShiftRepository.findById(newShiftId)).thenReturn(Optional.of(newShiftDtoFromRepo));

        ShiftDTO result = shiftService.startNewShift(1, openingFloat);

        assertNotNull(result);
        assertEquals(newShiftId, result.getShiftId());
        assertEquals("Active", result.getStatus());
        assertEquals("testuser", result.getStartedByUsername()); // Check if username was populated
        verify(mockShiftRepository).startShift(1, openingFloat);
        verify(mockShiftRepository).findById(newShiftId);
    }

    @Test
    void startNewShift_userAlreadyHasActiveShift_throwsShiftOperationException() {
        when(mockShiftRepository.findActiveOrPausedShiftByUserId(1)).thenReturn(Optional.of(testShift)); // User has active shift

        assertThrows(ShiftOperationException.class, () -> {
            shiftService.startNewShift(1, new BigDecimal("100.00"));
        });
    }

    @Test
    void startNewShift_invalidOpeningFloat_throwsValidationException() {
        assertThrows(ValidationException.class, () -> {
            shiftService.startNewShift(1, new BigDecimal("-50.00"));
        });
    }

    @Test
    void startNewShift_repoStartShiftFails_throwsShiftOperationException() {
        when(mockShiftRepository.findActiveOrPausedShiftByUserId(1)).thenReturn(Optional.empty());
        when(mockShiftRepository.startShift(1, new BigDecimal("100.00"))).thenThrow(new DataAccessException("DB error starting shift"));

        assertThrows(ShiftOperationException.class, () -> {
            shiftService.startNewShift(1, new BigDecimal("100.00"));
        });
    }


    // --- pauseActiveShift ---
    @Test
    void pauseActiveShift_shiftIsActive_pausesSuccessfully() throws Exception {
        when(mockShiftRepository.findById(101)).thenReturn(Optional.of(testShift)); // testShift is "Active"
        doNothing().when(mockShiftRepository).pauseShift(101, 1);

        shiftService.pauseActiveShift(101, 1);
        verify(mockShiftRepository).pauseShift(101, 1);
    }

    @Test
    void pauseActiveShift_shiftNotActive_throwsShiftOperationException() {
        testShift.setStatus("Paused");
        when(mockShiftRepository.findById(101)).thenReturn(Optional.of(testShift));
        assertThrows(ShiftOperationException.class, () -> {
            shiftService.pauseActiveShift(101, 1);
        });
    }

    @Test
    void pauseActiveShift_userNotAuthorized_throwsShiftOperationException() {
        when(mockShiftRepository.findById(101)).thenReturn(Optional.of(testShift)); // Shift belongs to user 1
        assertThrows(ShiftOperationException.class, () -> {
            shiftService.pauseActiveShift(101, 2); // User 2 trying to pause
        });
    }


    // --- resumePausedShift ---
    @Test
    void resumePausedShift_shiftIsPaused_resumesSuccessfully() throws Exception {
        testShift.setStatus("Paused");
        ShiftDTO resumedShiftDto = new ShiftDTO(testShift.getShiftId(), testShift.getUserId(), testShift.getStartedByUsername(),
                                                testShift.getStartTime(), null, "Active", testShift.getOpeningFloat());

        when(mockShiftRepository.findById(101)).thenReturn(Optional.of(testShift), Optional.of(resumedShiftDto)); // First call for check, second after resume
        doNothing().when(mockShiftRepository).resumeShift(101, 1);
        // when(mockUserRepository.findById(1)).thenReturn(Optional.of(testUser)); // For username population in DTO

        ShiftDTO result = shiftService.resumePausedShift(101, 1);

        assertNotNull(result);
        assertEquals("Active", result.getStatus());
        verify(mockShiftRepository).resumeShift(101, 1);
    }

    // Add more tests for resumeShift failure scenarios (not paused, user mismatch, etc.)
    // Add tests for exception wrapping from repository layer.
}
