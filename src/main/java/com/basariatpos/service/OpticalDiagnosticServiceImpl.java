package com.basariatpos.service;

import com.basariatpos.model.OpticalDiagnosticDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.OpticalDiagnosticRepository;
import com.basariatpos.service.exception.DiagnosticNotFoundException;
import com.basariatpos.service.exception.DiagnosticServiceException;
import com.basariatpos.service.exception.DiagnosticValidationException;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OpticalDiagnosticServiceImpl implements OpticalDiagnosticService {

    private static final Logger logger = AppLogger.getLogger(OpticalDiagnosticServiceImpl.class);
    private final OpticalDiagnosticRepository diagnosticRepository;
    private final UserSessionService userSessionService; // For createdByUserId

    public OpticalDiagnosticServiceImpl(OpticalDiagnosticRepository diagnosticRepository, UserSessionService userSessionService) {
        if (diagnosticRepository == null) {
            throw new IllegalArgumentException("OpticalDiagnosticRepository cannot be null.");
        }
        if (userSessionService == null) {
            throw new IllegalArgumentException("UserSessionService cannot be null.");
        }
        this.diagnosticRepository = diagnosticRepository;
        this.userSessionService = userSessionService;
    }

    @Override
    public OpticalDiagnosticDTO recordDiagnostic(OpticalDiagnosticDTO diagnosticDto)
        throws DiagnosticValidationException, DiagnosticServiceException {

        validateDiagnosticDto(diagnosticDto, true); // true for create (patientId must be > 0)

        try {
            UserDTO currentUser = userSessionService.getCurrentUser();
            if (currentUser != null) {
                diagnosticDto.setCreatedByUserId(currentUser.getUserId());
            } else {
                // This case should ideally be prevented by UI/application flow (user must be logged in)
                logger.warn("Recording diagnostic without a logged-in user in session. createdByUserId will be null if not already set.");
                 // diagnosticDto.setCreatedByUserId(null); // Or a system user ID
            }
            // createdAt is set by repository
            return diagnosticRepository.save(diagnosticDto);
        } catch (Exception e) {
            logger.error("Error recording diagnostic for patient ID {}: {}", diagnosticDto.getPatientId(), e.getMessage(), e);
            throw new DiagnosticServiceException("Could not record diagnostic.", e);
        }
    }

    @Override
    public OpticalDiagnosticDTO updateDiagnostic(OpticalDiagnosticDTO diagnosticDto)
        throws DiagnosticValidationException, DiagnosticNotFoundException, DiagnosticServiceException {

        if (diagnosticDto.getDiagnosticId() <= 0) {
            throw new DiagnosticValidationException("Diagnostic ID must be valid for update.", List.of("Invalid Diagnostic ID."));
        }
        validateDiagnosticDto(diagnosticDto, false); // false for update

        try {
            // Ensure diagnostic exists before attempting update
            diagnosticRepository.findById(diagnosticDto.getDiagnosticId())
                .orElseThrow(() -> new DiagnosticNotFoundException(diagnosticDto.getDiagnosticId()));

            // createdByUserId and createdAt should not be changed on update by this method
            // The repository's save method for update should also not touch these.
            return diagnosticRepository.save(diagnosticDto);
        } catch (DiagnosticNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating diagnostic ID '{}': {}", diagnosticDto.getDiagnosticId(), e.getMessage(), e);
            throw new DiagnosticServiceException("Could not update diagnostic.", e);
        }
    }

    @Override
    public List<OpticalDiagnosticDTO> getDiagnosticsForPatient(int patientId) throws DiagnosticServiceException {
        if (patientId <= 0) throw new DiagnosticServiceException("Patient ID must be positive."); // Or ValidationException
        try {
            return diagnosticRepository.findByPatientId(patientId);
        } catch (Exception e) {
            logger.error("Error retrieving diagnostics for patient ID {}: {}", patientId, e.getMessage(), e);
            throw new DiagnosticServiceException("Could not retrieve diagnostics for patient.", e);
        }
    }

    @Override
    public Optional<OpticalDiagnosticDTO> getDiagnosticById(int diagnosticId) throws DiagnosticServiceException {
        if (diagnosticId <= 0) throw new DiagnosticServiceException("Diagnostic ID must be positive.");
        try {
            return diagnosticRepository.findById(diagnosticId);
        } catch (Exception e) {
            logger.error("Error retrieving diagnostic by ID {}: {}", diagnosticId, e.getMessage(), e);
            throw new DiagnosticServiceException("Error retrieving diagnostic by ID.", e);
        }
    }

    @Override
    public void deleteDiagnostic(int diagnosticId) throws DiagnosticNotFoundException, DiagnosticServiceException {
        if (diagnosticId <= 0) throw new DiagnosticServiceException("Diagnostic ID must be positive.");
        try {
            // Ensure diagnostic exists before attempting delete
            diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new DiagnosticNotFoundException(diagnosticId));

            // TODO: Add business rule: Check if diagnostic is linked to any sales orders.
            // If so, throw new DiagnosticInUseException("Diagnostic is linked to sales orders and cannot be deleted.");

            diagnosticRepository.deleteById(diagnosticId);
            logger.info("Optical diagnostic with ID {} deleted successfully.", diagnosticId);
        } catch (DiagnosticNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Error deleting diagnostic ID '{}': {}", diagnosticId, e.getMessage(), e);
            throw new DiagnosticServiceException("Could not delete diagnostic.", e);
        }
    }

    // --- Helper Methods ---
    private void validateDiagnosticDto(OpticalDiagnosticDTO dto, boolean isCreate) throws DiagnosticValidationException {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("Diagnostic data cannot be null.");
            throw new DiagnosticValidationException("Diagnostic data is null.", errors);
        }
        if (dto.getPatientId() <= 0) {
            errors.add("Patient ID is required and must be positive.");
        }
        if (dto.getDiagnosticDate() == null) {
            errors.add("Diagnostic date is required.");
        }

        // Basic validation for prescription values: if one part of an eye's prescription is set, others might be expected.
        // This is a simplified validation. Clinical validation is more complex.
        validateEyePrescription("OD Distance", dto.getOdSphDist(), dto.getOdCylDist(), dto.getOdAxisDist(), errors);
        validateEyePrescription("OS Distance", dto.getOsSphDist(), dto.getOsCylDist(), dto.getOsAxisDist(), errors);

        if (dto.isContactLensRx() && (dto.getContactLensDetails() == null || dto.getContactLensDetails().trim().isEmpty())) {
            errors.add("Contact lens details are required if 'Is Contact Lens Rx' is checked.");
        }
        if (!dto.isContactLensRx() && dto.getContactLensDetails() != null && !dto.getContactLensDetails().trim().isEmpty()) {
            // Potentially clear details if not a CL Rx, or warn. For now, just a note.
            logger.debug("Contact lens details provided but 'Is Contact Lens Rx' is not checked for patient ID {}.", dto.getPatientId());
        }


        // Example: Axis should be present if Cyl is present
        if (dto.getOdCylDist() != null && dto.getOdCylDist().compareTo(BigDecimal.ZERO) != 0 && dto.getOdAxisDist() == null) {
            errors.add("OD Distance Axis is required if Cylinder is present.");
        }
        if (dto.getOsCylDist() != null && dto.getOsCylDist().compareTo(BigDecimal.ZERO) != 0 && dto.getOsAxisDist() == null) {
            errors.add("OS Distance Axis is required if Cylinder is present.");
        }

        // Axis values typically between 0 and 180
        if (dto.getOdAxisDist() != null && (dto.getOdAxisDist() < 0 || dto.getOdAxisDist() > 180)) {
            errors.add("OD Distance Axis must be between 0 and 180.");
        }
        if (dto.getOsAxisDist() != null && (dto.getOsAxisDist() < 0 || dto.getOsAxisDist() > 180)) {
            errors.add("OS Distance Axis must be between 0 and 180.");
        }

        // Sph and Cyl values are usually in steps of 0.25. This is too complex for basic validation here.

        if (!errors.isEmpty()) {
            throw new DiagnosticValidationException("Diagnostic data validation failed.", errors);
        }
    }

    private void validateEyePrescription(String eyeLabel, BigDecimal sph, BigDecimal cyl, Integer axis, List<String> errors) {
        boolean sphPresent = sph != null;
        boolean cylPresent = cyl != null && cyl.compareTo(BigDecimal.ZERO) != 0; // Cyl 0.00 is like no cyl
        boolean axisPresent = axis != null;

        if (cylPresent && !axisPresent) {
            errors.add(eyeLabel + " Axis is required if Cylinder is present.");
        }
        if (axisPresent && !cylPresent) {
            // This is usually allowed, axis might be noted even with plano cylinder.
            // If Cyl is 0 or null, axis is irrelevant but not strictly an error if noted.
        }
        if (!sphPresent && (cylPresent || axisPresent)) {
            // Typically, if Cyl/Axis are present, Sph should also be (even if plano/0.00)
            // errors.add(eyeLabel + " Sphere is required if Cylinder or Axis is present.");
        }
    }
}
