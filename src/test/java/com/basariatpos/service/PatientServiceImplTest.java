package com.basariatpos.service;

import com.basariatpos.model.PatientDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.PatientRepository;
import com.basariatpos.service.exception.PatientAlreadyExistsException;
import com.basariatpos.service.exception.PatientNotFoundException;
import com.basariatpos.service.exception.PatientValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List; // For ValidationException
import java.util.ArrayList; // For ValidationException


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock private PatientRepository mockPatientRepository;
    @Mock private ApplicationSettingsService mockAppSettingsService;
    @Mock private UserSessionService mockUserSessionService;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientDTO testPatientDtoInput;
    private UserDTO mockCurrentUser;

    @BeforeEach
    void setUp() {
        testPatientDtoInput = new PatientDTO("Valid Name", "0911223344", "Some Address", true);

        mockCurrentUser = new UserDTO(1, "testuser", "Test User", "Admin");
        when(mockUserSessionService.getCurrentUser()).thenReturn(mockCurrentUser); // Assume user is logged in by default for most tests

        // Default setting for patient ID prefix
        when(mockAppSettingsService.getSettingValue("default.patient.id_prefix", "PAT-")).thenReturn("PAT-");
    }

    @Test
    void constructor_nullPatientRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PatientServiceImpl(null, mockAppSettingsService, mockUserSessionService));
    }
    @Test
    void constructor_nullAppSettingsService_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PatientServiceImpl(mockPatientRepository, null, mockUserSessionService));
    }
    @Test
    void constructor_nullUserSessionService_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PatientServiceImpl(mockPatientRepository, mockAppSettingsService, null));
    }

    // --- createPatient Tests ---
    @Test
    void createPatient_validData_returnsPatientWithSystemIdAndUserId() throws Exception {
        when(mockPatientRepository.findByPhoneNumber(testPatientDtoInput.getPhoneNumber())).thenReturn(Optional.empty());
        when(mockPatientRepository.systemIdExists(anyString())).thenReturn(false); // Assume generated ID is unique
        // Mock the save operation to set IDs and timestamps as the repository would
        when(mockPatientRepository.save(any(PatientDTO.class))).thenAnswer(invocation -> {
            PatientDTO dto = invocation.getArgument(0);
            dto.setPatientId(100); // Simulate DB assigning an ID
            // dto.setSystemPatientId(dto.getSystemPatientId()); // Already set by service before save
            dto.setCreatedAt(OffsetDateTime.now());
            dto.setUpdatedAt(OffsetDateTime.now());
            return dto;
        });

        PatientDTO result = patientService.createPatient(testPatientDtoInput);

        assertNotNull(result);
        assertEquals(100, result.getPatientId());
        assertNotNull(result.getSystemPatientId());
        assertTrue(result.getSystemPatientId().startsWith("PAT-"));
        assertEquals(mockCurrentUser.getUserId(), result.getCreatedByUserId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(mockPatientRepository).save(any(PatientDTO.class));
    }

    @Test
    void createPatient_phoneNumberExists_throwsPatientAlreadyExistsException() {
        when(mockPatientRepository.findByPhoneNumber(testPatientDtoInput.getPhoneNumber()))
            .thenReturn(Optional.of(new PatientDTO(1, "SYS001", "Existing", testPatientDtoInput.getPhoneNumber(), null, false, null, null, null)));

        assertThrows(PatientAlreadyExistsException.class, () -> {
            patientService.createPatient(testPatientDtoInput);
        });
    }

    @Test
    void createPatient_invalidFullName_throwsPatientValidationException() {
        testPatientDtoInput.setFullName(""); // Invalid
        PatientValidationException ex = assertThrows(PatientValidationException.class, () -> {
            patientService.createPatient(testPatientDtoInput);
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Full name is required")));
    }

    @Test
    void createPatient_systemIdGenerationCollision_retriesAndSucceeds() throws Exception {
        when(mockPatientRepository.findByPhoneNumber(testPatientDtoInput.getPhoneNumber())).thenReturn(Optional.empty());
        // First generated ID exists, second is unique
        when(mockPatientRepository.systemIdExists(anyString()))
            .thenReturn(true)  // First attempt collision
            .thenReturn(false); // Second attempt unique

        when(mockPatientRepository.save(any(PatientDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientDTO result = patientService.createPatient(testPatientDtoInput);

        assertNotNull(result.getSystemPatientId());
        verify(mockPatientRepository, times(2)).systemIdExists(anyString()); // Verify it checked twice
        verify(mockPatientRepository).save(result);
    }


    // --- updatePatient Tests ---
    @Test
    void updatePatient_validData_returnsUpdatedPatient() throws Exception {
        PatientDTO existingPatient = new PatientDTO(1, "PAT-001", "Old Name", "0911223344", "Old Address", false, 1, OffsetDateTime.now(), OffsetDateTime.now());
        when(mockPatientRepository.findById(1)).thenReturn(Optional.of(existingPatient));

        PatientDTO updatedDto = new PatientDTO(1, "PAT-001", "New Name", "0955667788", "New Address", true, 1, existingPatient.getCreatedAt(), null);

        // Assume new phone number doesn't exist for another patient
        when(mockPatientRepository.findByPhoneNumber("0955667788")).thenReturn(Optional.empty());
        when(mockPatientRepository.save(any(PatientDTO.class))).thenReturn(updatedDto); // Repo save returns the DTO passed to it

        PatientDTO result = patientService.updatePatient(updatedDto);

        assertEquals("New Name", result.getFullName());
        assertEquals("0955667788", result.getPhoneNumber());
        assertTrue(result.isWhatsappOptIn());
        verify(mockPatientRepository).save(updatedDto);
    }

    @Test
    void updatePatient_patientNotFound_throwsPatientNotFoundException() {
        PatientDTO nonExistentPatient = new PatientDTO(99, "PAT-999", "Non Existent", "000", null, false, null, null, null);
        when(mockPatientRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> {
            patientService.updatePatient(nonExistentPatient);
        });
    }

    // --- updateWhatsAppConsent Tests ---
    @Test
    void updateWhatsAppConsent_patientExists_updatesConsent() throws Exception {
        PatientDTO patient = new PatientDTO(1, "PAT-001", "Consent Test", "123", null, false, 1, OffsetDateTime.now(), OffsetDateTime.now());
        when(mockPatientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(mockPatientRepository.save(any(PatientDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        patientService.updateWhatsAppConsent(1, true);

        ArgumentCaptor<PatientDTO> captor = ArgumentCaptor.forClass(PatientDTO.class);
        verify(mockPatientRepository).save(captor.capture());
        assertTrue(captor.getValue().isWhatsappOptIn());
    }

    // Add more tests for other service methods: getPatientById, getPatientBySystemId, getPatientByPhoneNumber, searchPatientsByName, searchPatientsByNameOrPhone, getAllPatients
    // And more edge cases for create/update (e.g., phone number conflict during update).
}
