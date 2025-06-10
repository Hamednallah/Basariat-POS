package com.basariatpos.service;

import com.basariatpos.model.CenterProfileDTO;
import com.basariatpos.repository.CenterProfileRepository;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CenterProfileServiceTest {

    @Mock
    private CenterProfileRepository mockRepository;

    @InjectMocks
    private CenterProfileService centerProfileService;

    private CenterProfileDTO validDto;
    private CenterProfileDTO invalidDto;

    @BeforeEach
    void setUp() {
        validDto = new CenterProfileDTO(
                "Test Center", "123 Main St", null, "Testville", "Testland",
                "12345", "555-1234", null, "test@example.com",
                "www.example.com", "/path/to/logo.png", "TAX123", "$",
                "USD", "Thanks for shopping!"
        );

        invalidDto = new CenterProfileDTO(); // Empty DTO, will fail validation
    }

    @Test
    void constructor_should_throw_exception_if_repository_is_null() {
        assertThrows(IllegalArgumentException.class, () -> new CenterProfileService(null));
    }

    // --- saveProfile Tests ---
    @Test
    void saveProfile_should_call_repository_save_for_valid_dto() throws Exception {
        // Act
        boolean result = centerProfileService.saveProfile(validDto);

        // Assert
        assertTrue(result, "saveProfile should return true for valid DTO.");
        verify(mockRepository).save(validDto);
    }

    @Test
    void saveProfile_should_throw_ProfileValidationException_for_invalid_dto() {
        // Act & Assert
        ProfileValidationException ex = assertThrows(ProfileValidationException.class, () -> {
            centerProfileService.saveProfile(invalidDto);
        });
        assertTrue(ex.getValidationErrors().size() > 0);
        verify(mockRepository, never()).save(any(CenterProfileDTO.class));
    }

    @Test
    void saveProfile_should_throw_IllegalArgumentException_for_null_dto() {
        assertThrows(IllegalArgumentException.class, () -> {
            centerProfileService.saveProfile(null);
        });
        verify(mockRepository, never()).save(any(CenterProfileDTO.class));
    }

    @Test
    void saveProfile_should_throw_ProfileServiceException_on_repository_dataAccessException() {
        // Arrange
        doThrow(new DataAccessException("DB error")).when(mockRepository).save(validDto);

        // Act & Assert
        ProfileServiceException ex = assertThrows(ProfileServiceException.class, () -> {
            centerProfileService.saveProfile(validDto);
        });
        assertEquals("Failed to save profile due to a database error.", ex.getMessage());
        assertTrue(ex.getCause() instanceof DataAccessException);
    }

    @Test
    void saveProfile_should_throw_ProfileServiceException_on_repository_unexpectedException() {
        // Arrange
        doThrow(new RuntimeException("Unexpected repo error")).when(mockRepository).save(validDto);

        // Act & Assert
        ProfileServiceException ex = assertThrows(ProfileServiceException.class, () -> {
            centerProfileService.saveProfile(validDto);
        });
        assertEquals("An unexpected error occurred while saving the profile.", ex.getMessage());
        assertTrue(ex.getCause() instanceof RuntimeException);
    }


    // --- getCenterProfile Tests ---
    @Test
    void getCenterProfile_should_return_dto_from_repository() throws Exception {
        // Arrange
        when(mockRepository.getProfile()).thenReturn(Optional.of(validDto));

        // Act
        Optional<CenterProfileDTO> resultOpt = centerProfileService.getCenterProfile();

        // Assert
        assertTrue(resultOpt.isPresent(), "Result Optional should not be empty.");
        assertEquals(validDto, resultOpt.get(), "Returned DTO should match the one from repository.");
        verify(mockRepository).getProfile();
    }

    @Test
    void getCenterProfile_should_return_empty_optional_if_repository_returns_empty() throws Exception {
        // Arrange
        when(mockRepository.getProfile()).thenReturn(Optional.empty());

        // Act
        Optional<CenterProfileDTO> resultOpt = centerProfileService.getCenterProfile();

        // Assert
        assertFalse(resultOpt.isPresent(), "Result Optional should be empty.");
        verify(mockRepository).getProfile();
    }

    @Test
    void getCenterProfile_should_throw_ProfileServiceException_on_repository_dataAccessException() {
        // Arrange
        when(mockRepository.getProfile()).thenThrow(new DataAccessException("DB error"));

        // Act & Assert
        ProfileServiceException ex = assertThrows(ProfileServiceException.class, () -> {
            centerProfileService.getCenterProfile();
        });
        assertEquals("Failed to retrieve profile due to a database error.", ex.getMessage());
    }

    // --- isProfileConfigured Tests ---
    @Test
    void isProfileConfigured_should_return_true_if_repository_exists_returns_true() throws Exception {
        // Arrange
        when(mockRepository.exists()).thenReturn(true);

        // Act
        boolean result = centerProfileService.isProfileConfigured();

        // Assert
        assertTrue(result, "isProfileConfigured should return true.");
        verify(mockRepository).exists();
    }

    @Test
    void isProfileConfigured_should_return_false_if_repository_exists_returns_false() throws Exception {
        // Arrange
        when(mockRepository.exists()).thenReturn(false);

        // Act
        boolean result = centerProfileService.isProfileConfigured();

        // Assert
        assertFalse(result, "isProfileConfigured should return false.");
        verify(mockRepository).exists();
    }

    @Test
    void isProfileConfigured_should_throw_ProfileServiceException_on_repository_dataAccessException() {
        // Arrange
        when(mockRepository.exists()).thenThrow(new DataAccessException("DB error"));

        // Act & Assert
        ProfileServiceException ex = assertThrows(ProfileServiceException.class, () -> {
            centerProfileService.isProfileConfigured();
        });
        assertEquals("Failed to check profile configuration due to a database error.", ex.getMessage());
    }
}
