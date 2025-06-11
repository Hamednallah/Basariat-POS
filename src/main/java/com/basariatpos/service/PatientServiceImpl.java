package com.basariatpos.service;

import com.basariatpos.model.PatientDTO;
import com.basariatpos.model.UserDTO; // For createdByUserId
import com.basariatpos.repository.PatientRepository;
import com.basariatpos.service.exception.PatientAlreadyExistsException;
import com.basariatpos.service.exception.PatientNotFoundException;
import com.basariatpos.service.exception.PatientServiceException;
import com.basariatpos.service.exception.PatientValidationException;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class PatientServiceImpl implements PatientService {

    private static final Logger logger = AppLogger.getLogger(PatientServiceImpl.class);
    private final PatientRepository patientRepository;
    private final ApplicationSettingsService applicationSettingsService; // For patient ID prefix
    private final UserSessionService userSessionService; // For createdByUserId

    public PatientServiceImpl(PatientRepository patientRepository,
                              ApplicationSettingsService applicationSettingsService,
                              UserSessionService userSessionService) {
        if (patientRepository == null) {
            throw new IllegalArgumentException("PatientRepository cannot be null.");
        }
        if (applicationSettingsService == null) {
            throw new IllegalArgumentException("ApplicationSettingsService cannot be null.");
        }
        if (userSessionService == null) {
            throw new IllegalArgumentException("UserSessionService cannot be null.");
        }
        this.patientRepository = patientRepository;
        this.applicationSettingsService = applicationSettingsService;
        this.userSessionService = userSessionService;
    }

    @Override
    public PatientDTO createPatient(PatientDTO patientDto)
        throws PatientValidationException, PatientAlreadyExistsException, PatientServiceException {

        validatePatientDto(patientDto, true);

        try {
            // Check for existing patient by phone number
            if (patientRepository.findByPhoneNumber(patientDto.getPhoneNumber()).isPresent()) {
                throw new PatientAlreadyExistsException("phoneNumber", patientDto.getPhoneNumber());
            }

            // Generate System Patient ID
            String prefix = applicationSettingsService.getSettingValue("default.patient.id_prefix", "PAT-");
            patientDto.setSystemPatientId(generateUniqueSystemId(prefix));

            // Set createdByUserId
            UserDTO currentUser = userSessionService.getCurrentUser();
            if (currentUser != null) {
                patientDto.setCreatedByUserId(currentUser.getUserId());
            } else {
                // Handle cases where no user is in session (e.g., system process, initial import)
                // For now, we'll allow null, but a specific system user ID might be better.
                logger.warn("Creating patient without a logged-in user in session. createdByUserId will be null.");
            }

            // Timestamps (createdAt, updatedAt) will be set by repository during save.
            return patientRepository.save(patientDto);
        } catch (PatientAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating patient with phone number '{}': {}", patientDto.getPhoneNumber(), e.getMessage(), e);
            throw new PatientServiceException("Could not create patient.", e);
        }
    }

    @Override
    public PatientDTO updatePatient(PatientDTO patientDto)
        throws PatientValidationException, PatientNotFoundException, PatientServiceException {

        if (patientDto.getPatientId() <= 0) {
            throw new PatientValidationException("Patient ID must be valid for update.", List.of("Invalid Patient ID."));
        }
        validatePatientDto(patientDto, false); // false for update (don't validate phone for existence against itself)

        try {
            // Ensure patient exists before attempting update
            patientRepository.findById(patientDto.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(patientDto.getPatientId()));

            // Check if phone number is being changed to one that already exists for *another* patient
            Optional<PatientDTO> patientByPhone = patientRepository.findByPhoneNumber(patientDto.getPhoneNumber());
            if (patientByPhone.isPresent() && patientByPhone.get().getPatientId() != patientDto.getPatientId()) {
                throw new PatientAlreadyExistsException("phoneNumber", patientDto.getPhoneNumber());
            }

            // updatedAt timestamp will be set by repository
            return patientRepository.save(patientDto);
        } catch (PatientNotFoundException | PatientAlreadyExistsException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Error updating patient ID '{}': {}", patientDto.getPatientId(), e.getMessage(), e);
            throw new PatientServiceException("Could not update patient.", e);
        }
    }

    @Override
    public void updateWhatsAppConsent(int patientId, boolean hasConsented)
        throws PatientNotFoundException, PatientServiceException {
        try {
            PatientDTO patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

            patient.setWhatsappOptIn(hasConsented);
            patientRepository.save(patient); // This will also update the updatedAt timestamp
            logger.info("WhatsApp consent for patient ID {} updated to {}.", patientId, hasConsented);
        } catch (PatientNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Error updating WhatsApp consent for patient ID '{}': {}", patientId, e.getMessage(), e);
            throw new PatientServiceException("Could not update WhatsApp consent.", e);
        }
    }

    @Override
    public Optional<PatientDTO> getPatientById(int patientId) {
        try {
            return patientRepository.findById(patientId);
        } catch (Exception e) {
            logger.error("Error retrieving patient by ID {}: {}", patientId, e.getMessage(), e);
            throw new PatientServiceException("Error retrieving patient by ID.", e);
        }
    }

    @Override
    public Optional<PatientDTO> getPatientBySystemId(String systemId) {
         try {
            return patientRepository.findBySystemPatientId(systemId);
        } catch (Exception e) {
            logger.error("Error retrieving patient by system ID '{}': {}", systemId, e.getMessage(), e);
            throw new PatientServiceException("Error retrieving patient by system ID.", e);
        }
    }

    @Override
    public Optional<PatientDTO> getPatientByPhoneNumber(String phoneNumber) {
        try {
            return patientRepository.findByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            logger.error("Error retrieving patient by phone number '{}': {}", phoneNumber, e.getMessage(), e);
            throw new PatientServiceException("Error retrieving patient by phone number.", e);
        }
    }

    @Override
    public List<PatientDTO> searchPatientsByName(String nameQuery) {
        try {
            return patientRepository.searchByName(nameQuery);
        } catch (Exception e) {
            logger.error("Error searching patients by name query '{}': {}", nameQuery, e.getMessage(), e);
            throw new PatientServiceException("Error searching patients by name.", e);
        }
    }

    @Override
    public List<PatientDTO> searchPatientsByNameOrPhone(String query) {
        try {
            return patientRepository.searchByNameOrPhone(query);
        } catch (Exception e) {
            logger.error("Error searching patients by name/phone query '{}': {}", query, e.getMessage(), e);
            throw new PatientServiceException("Error searching patients by name or phone.", e);
        }
    }

    @Override
    public List<PatientDTO> getAllPatients() {
         try {
            return patientRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all patients: {}", e.getMessage(), e);
            throw new PatientServiceException("Error retrieving all patients.", e);
        }
    }

    // --- Helper Methods ---
    private void validatePatientDto(PatientDTO dto, boolean isCreate) throws PatientValidationException {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("Patient data cannot be null.");
            throw new PatientValidationException("Patient data is null.", errors);
        }
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            errors.add("Full name is required."); // TODO: Use i18n keys from MessageProvider
        }
        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
            errors.add("Phone number is required.");
        } else if (!dto.getPhoneNumber().matches("\\+?[0-9\\s\\(\\)-]+")) { // Basic phone format validation
            errors.add("Invalid phone number format.");
        }
        // Add other validations (e.g., address length) as needed.

        if (!errors.isEmpty()) {
            throw new PatientValidationException("Patient data validation failed.", errors);
        }
    }

    private String generateUniqueSystemId(String prefix) {
        // Re-implementing here, as repository's generateSystemIdWithDsl needs DSLContext
        // This service layer method will use repository's systemIdExists for check.
        int attempts = 0;
        while (attempts < 10) {
            long timestamp = System.currentTimeMillis();
            int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            String generatedId = prefix + timestamp + "-" + randomSuffix;
            if (generatedId.length() > 50) {
                generatedId = generatedId.substring(0, 50);
            }
            if (!patientRepository.systemIdExists(generatedId)) {
                return generatedId;
            }
            attempts++;
        }
        logger.error("Failed to generate a unique system patient ID after {} attempts with prefix '{}'.", attempts, prefix);
        throw new PatientServiceException("Could not generate unique system patient ID.");
    }
}
