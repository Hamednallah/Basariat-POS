package com.basariatpos.service;

import com.basariatpos.model.OpticalDiagnosticDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.OpticalDiagnosticRepository;
import com.basariatpos.service.exception.DiagnosticNotFoundException;
import com.basariatpos.service.exception.DiagnosticValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpticalDiagnosticServiceImplTest {

    @Mock
    private OpticalDiagnosticRepository mockDiagnosticRepository;
    @Mock
    private UserSessionService mockUserSessionService;

    @InjectMocks
    private OpticalDiagnosticServiceImpl diagnosticService;

    private OpticalDiagnosticDTO testDto;
    private UserDTO mockCurrentUser;

    @BeforeEach
    void setUp() {
        testDto = new OpticalDiagnosticDTO();
        testDto.setDiagnosticId(1);
        testDto.setPatientId(10);
        testDto.setDiagnosticDate(LocalDate.now());
        testDto.setOdSphDist(new BigDecimal("-1.00"));

        mockCurrentUser = new UserDTO(5, "opticuser", "Optic User", "Optometrist");
    }

    @Test
    void constructor_nullRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new OpticalDiagnosticServiceImpl(null, mockUserSessionService));
    }

    @Test
    void constructor_nullUserSessionService_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new OpticalDiagnosticServiceImpl(mockDiagnosticRepository, null));
    }

    // --- recordDiagnostic ---
    @Test
    void recordDiagnostic_validDtoAndUserInSession_savesAndReturnsDto() throws Exception {
        OpticalDiagnosticDTO newDto = new OpticalDiagnosticDTO();
        newDto.setPatientId(10);
        newDto.setDiagnosticDate(LocalDate.now());
        newDto.setOdSphDist(BigDecimal.ZERO); // Valid SPH

        when(mockUserSessionService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(mockDiagnosticRepository.save(any(OpticalDiagnosticDTO.class))).thenAnswer(inv -> {
            OpticalDiagnosticDTO arg = inv.getArgument(0);
            arg.setDiagnosticId(101);
            arg.setCreatedAt(OffsetDateTime.now());
            return arg;
        });

        OpticalDiagnosticDTO result = diagnosticService.recordDiagnostic(newDto);

        assertNotNull(result);
        assertEquals(101, result.getDiagnosticId());
        assertEquals(mockCurrentUser.getUserId(), result.getCreatedByUserId());
        assertNotNull(result.getCreatedAt());
        verify(mockDiagnosticRepository).save(newDto);
    }

    @Test
    void recordDiagnostic_missingPatientId_throwsDiagnosticValidationException() {
        OpticalDiagnosticDTO invalidDto = new OpticalDiagnosticDTO();
        invalidDto.setDiagnosticDate(LocalDate.now());

        DiagnosticValidationException ex = assertThrows(DiagnosticValidationException.class, () -> {
            diagnosticService.recordDiagnostic(invalidDto);
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Patient ID is required")));
    }

    @Test
    void recordDiagnostic_odCylPresentButAxisMissing_throwsDiagnosticValidationException() {
        OpticalDiagnosticDTO invalidDto = new OpticalDiagnosticDTO();
        invalidDto.setPatientId(1);
        invalidDto.setDiagnosticDate(LocalDate.now());
        invalidDto.setOdCylDist(new BigDecimal("-0.50"));
        invalidDto.setOdAxisDist(null); // Axis missing

        DiagnosticValidationException ex = assertThrows(DiagnosticValidationException.class, () -> {
            diagnosticService.recordDiagnostic(invalidDto);
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("OD Distance Axis is required if Cylinder is present")));
    }

    // --- updateDiagnostic ---
    @Test
    void updateDiagnostic_validDtoAndExists_updatesAndReturnsDto() throws Exception {
        when(mockDiagnosticRepository.findById(testDto.getDiagnosticId())).thenReturn(Optional.of(testDto));
        when(mockDiagnosticRepository.save(any(OpticalDiagnosticDTO.class))).thenReturn(testDto);

        testDto.setRemarks("Updated remarks");
        OpticalDiagnosticDTO result = diagnosticService.updateDiagnostic(testDto);

        assertEquals("Updated remarks", result.getRemarks());
        verify(mockDiagnosticRepository).save(testDto);
    }

    @Test
    void updateDiagnostic_dtoNotExists_throwsDiagnosticNotFoundException() {
        when(mockDiagnosticRepository.findById(testDto.getDiagnosticId())).thenReturn(Optional.empty());
        assertThrows(DiagnosticNotFoundException.class, () -> {
            diagnosticService.updateDiagnostic(testDto);
        });
    }

    // --- deleteDiagnostic ---
    @Test
    void deleteDiagnostic_exists_callsRepositoryDelete() throws Exception {
        when(mockDiagnosticRepository.findById(1)).thenReturn(Optional.of(testDto));
        doNothing().when(mockDiagnosticRepository).deleteById(1);

        diagnosticService.deleteDiagnostic(1);
        verify(mockDiagnosticRepository).deleteById(1);
    }

    // --- getDiagnosticsForPatient ---
    @Test
    void getDiagnosticsForPatient_validPatientId_returnsList() {
        List<OpticalDiagnosticDTO> list = new ArrayList<>();
        list.add(testDto);
        when(mockDiagnosticRepository.findByPatientId(10)).thenReturn(list);

        List<OpticalDiagnosticDTO> result = diagnosticService.getDiagnosticsForPatient(10);
        assertEquals(1, result.size());
        verify(mockDiagnosticRepository).findByPatientId(10);
    }
}
