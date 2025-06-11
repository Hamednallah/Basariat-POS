package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.PatientsRecord;
import com.basariatpos.model.PatientDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom; // For more robust unique ID generation

import static com.basariatpos.db.generated.Tables.PATIENTS;

public class PatientRepositoryImpl implements PatientRepository {

    private static final Logger logger = AppLogger.getLogger(PatientRepositoryImpl.class);
    private static final String DEFAULT_PATIENT_ID_PREFIX = "PAT-"; // Fallback prefix

    @Override
    public Optional<PatientDTO> findById(int id) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            PatientsRecord record = dsl.selectFrom(PATIENTS)
                                       .where(PATIENTS.PATIENT_ID.eq(id))
                                       .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding patient by ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<PatientDTO> findBySystemPatientId(String systemId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            PatientsRecord record = dsl.selectFrom(PATIENTS)
                                       .where(PATIENTS.SYSTEM_PATIENT_ID.eq(systemId))
                                       .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding patient by system ID '{}': {}", systemId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<PatientDTO> findByPhoneNumber(String phoneNumber) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            PatientsRecord record = dsl.selectFrom(PATIENTS)
                                       .where(PATIENTS.PHONE_NUMBER.eq(phoneNumber))
                                       .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding patient by phone number '{}': {}", phoneNumber, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<PatientDTO> searchByName(String nameQuery) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return dsl.selectFrom(PATIENTS)
                      .where(DSL.lower(PATIENTS.FULL_NAME).like("%" + nameQuery.toLowerCase() + "%"))
                      .orderBy(PATIENTS.FULL_NAME.asc())
                      .fetch()
                      .map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error searching patients by name query '{}': {}", nameQuery, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<PatientDTO> searchByNameOrPhone(String query) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            String likeQuery = "%" + query.toLowerCase() + "%";
            return dsl.selectFrom(PATIENTS)
                      .where(DSL.lower(PATIENTS.FULL_NAME).like(likeQuery)
                             .or(PATIENTS.PHONE_NUMBER.like(likeQuery))) // Phone number might not need lower if stored consistently
                      .orderBy(PATIENTS.FULL_NAME.asc())
                      .fetch()
                      .map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error searching patients by name or phone query '{}': {}", query, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }


    @Override
    public List<PatientDTO> findAll() {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return dsl.selectFrom(PATIENTS)
                      .orderBy(PATIENTS.PATIENT_ID.desc()) // Example ordering
                      .fetch()
                      .map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding all patients: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public PatientDTO save(PatientDTO patientDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            PatientsRecord record;
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

            if (patientDto.getPatientId() > 0) { // Update
                record = dsl.fetchOne(PATIENTS, PATIENTS.PATIENT_ID.eq(patientDto.getPatientId()));
                if (record == null) {
                    throw new DataAccessException("Patient with ID " + patientDto.getPatientId() + " not found for update.");
                }
                record.setUpdatedAt(now);
            } else { // Insert
                record = dsl.newRecord(PATIENTS);
                record.setCreatedAt(now);
                record.setUpdatedAt(now); // Typically updated_at is same as created_at on insert
                if (patientDto.getSystemPatientId() == null || patientDto.getSystemPatientId().trim().isEmpty()) {
                    // ID prefix should come from settings via service layer, passing it to repo or generating in service.
                    // For now, using a default here if service doesn't provide it through DTO.
                    record.setSystemPatientId(generateSystemIdWithDsl(dsl, DEFAULT_PATIENT_ID_PREFIX));
                } else {
                     record.setSystemPatientId(patientDto.getSystemPatientId()); // Use if provided
                }
            }
            mapDtoToRecord(patientDto, record); // Map other fields
            record.store();

            // Update DTO with generated values (ID and potentially SystemPatientId if generated here)
            patientDto.setPatientId(record.getPatientId());
            patientDto.setSystemPatientId(record.getSystemPatientId()); // Ensure DTO has the final system ID
            patientDto.setCreatedAt(record.getCreatedAt());
            patientDto.setUpdatedAt(record.getUpdatedAt());

            logger.info("Patient '{}' (ID: {}, SystemID: {}) saved successfully.",
                        patientDto.getFullName(), patientDto.getPatientId(), patientDto.getSystemPatientId());
            return patientDto;
        } catch (DataAccessException e) {
            logger.error("Error saving patient '{}': {}", patientDto.getFullName(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public boolean systemIdExists(String systemId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return dsl.fetchExists(
                dsl.selectOne()
                   .from(PATIENTS)
                   .where(PATIENTS.SYSTEM_PATIENT_ID.eq(systemId))
            );
        } catch (DataAccessException e) {
            logger.error("Error checking system patient ID existence for '{}': {}", systemId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    // Internal helper, now expects DSLContext
    private String generateSystemIdWithDsl(DSLContext dsl, String prefix) {
        // A more robust implementation might involve a sequence or a more complex random part.
        // Using timestamp + random for higher chance of uniqueness before DB check.
        // Max length for system_patient_id is VARCHAR(50). Prefix "PAT-" is 4 chars.
        // Timestamp (e.g., 13 digits) + random (e.g. 4-6 digits) = ~20-25 chars. Well within limit.
        int attempts = 0;
        while (attempts < 10) { // Limit attempts to avoid infinite loop in unlikely scenario
            long timestamp = System.currentTimeMillis();
            int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 10000); // e.g. 4-digit random
            String generatedId = prefix + timestamp + "-" + randomSuffix;
            if (generatedId.length() > 50) { // Truncate if too long (should not happen with this scheme)
                generatedId = generatedId.substring(0, 50);
            }

            // Check existence using the passed DSLContext within the current transaction
            boolean exists = dsl.fetchExists(
                dsl.selectOne().from(PATIENTS).where(PATIENTS.SYSTEM_PATIENT_ID.eq(generatedId))
            );
            if (!exists) {
                return generatedId;
            }
            attempts++;
        }
        logger.error("Failed to generate a unique system patient ID after {} attempts with prefix '{}'.", attempts, prefix);
        throw new DataAccessException("Could not generate unique system patient ID.");
    }


    private PatientDTO mapRecordToDto(PatientsRecord record) {
        if (record == null) return null;
        return new PatientDTO(
                record.getPatientId(),
                record.getSystemPatientId(),
                record.getFullName(),
                record.getPhoneNumber(),
                record.getAddress(),
                record.getWhatsappOptIn(),
                record.getCreatedByUserId(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private void mapDtoToRecord(PatientDTO dto, PatientsRecord record) {
        // Do not map patientId for new records if it's auto-generated
        // systemPatientId is handled during insert logic if not provided
        record.setFullName(dto.getFullName());
        record.setPhoneNumber(dto.getPhoneNumber());
        record.setAddress(dto.getAddress());
        record.setWhatsappOptIn(dto.isWhatsappOptIn());
        if (dto.getCreatedByUserId() != null) { // Only set if provided, esp. for updates
            record.setCreatedByUserId(dto.getCreatedByUserId());
        }
        // createdAt and updatedAt are handled by save logic
    }

    private void closeContext(DSLContext dslContext) {
        if (dslContext != null) {
            try {
                dslContext.close();
            } catch (Exception e) {
                logger.warn("Failed to close DSLContext.", e);
            }
        }
    }
}
