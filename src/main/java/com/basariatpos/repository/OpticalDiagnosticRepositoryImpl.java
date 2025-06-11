package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.OpticaldiagnosticsRecord;
import com.basariatpos.model.OpticalDiagnosticDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.OPTICALDIAGNOSTICS;

public class OpticalDiagnosticRepositoryImpl implements OpticalDiagnosticRepository {

    private static final Logger logger = AppLogger.getLogger(OpticalDiagnosticRepositoryImpl.class);

    @Override
    public OpticalDiagnosticDTO save(OpticalDiagnosticDTO diagnosticDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            OpticaldiagnosticsRecord record;
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

            if (diagnosticDto.getDiagnosticId() > 0) { // Update
                record = dsl.fetchOne(OPTICALDIAGNOSTICS, OPTICALDIAGNOSTICS.DIAGNOSTIC_ID.eq(diagnosticDto.getDiagnosticId()));
                if (record == null) {
                    throw new DataAccessException("Optical Diagnostic with ID " + diagnosticDto.getDiagnosticId() + " not found for update.");
                }
                // created_at and created_by_user_id are not updated.
            } else { // Insert
                record = dsl.newRecord(OPTICALDIAGNOSTICS);
                record.setCreatedAt(now);
                if (diagnosticDto.getCreatedByUserId() != null) {
                    record.setCreatedByUserId(diagnosticDto.getCreatedByUserId());
                }
            }
            mapDtoToRecord(diagnosticDto, record);
            record.store();

            // Update DTO with generated ID if it was an insert
            if (diagnosticDto.getDiagnosticId() <= 0) {
                diagnosticDto.setDiagnosticId(record.getDiagnosticId());
            }
            // Ensure DTO has created_at if it was an insert and record has it
            if(record.getCreatedAt() != null) diagnosticDto.setCreatedAt(record.getCreatedAt());

            logger.info("Optical Diagnostic for patient ID {} (Diagnostic ID: {}) saved successfully.",
                        diagnosticDto.getPatientId(), diagnosticDto.getDiagnosticId());
            return diagnosticDto;
        } catch (DataAccessException e) {
            logger.error("Error saving optical diagnostic for patient ID {}: {}", diagnosticDto.getPatientId(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<OpticalDiagnosticDTO> findById(int diagnosticId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            OpticaldiagnosticsRecord record = dsl.selectFrom(OPTICALDIAGNOSTICS)
                                                 .where(OPTICALDIAGNOSTICS.DIAGNOSTIC_ID.eq(diagnosticId))
                                                 .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding optical diagnostic by ID {}: {}", diagnosticId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<OpticalDiagnosticDTO> findByPatientId(int patientId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return dsl.selectFrom(OPTICALDIAGNOSTICS)
                      .where(OPTICALDIAGNOSTICS.PATIENT_ID.eq(patientId))
                      .orderBy(OPTICALDIAGNOSTICS.DIAGNOSTIC_DATE.desc(), OPTICALDIAGNOSTICS.CREATED_AT.desc())
                      .fetch()
                      .map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding optical diagnostics for patient ID {}: {}", patientId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void deleteById(int diagnosticId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            int deletedRows = dsl.deleteFrom(OPTICALDIAGNOSTICS)
                                 .where(OPTICALDIAGNOSTICS.DIAGNOSTIC_ID.eq(diagnosticId))
                                 .execute();
            if (deletedRows == 0) {
                logger.warn("No optical diagnostic found with ID {} to delete.", diagnosticId);
                // Depending on desired behavior, could throw an exception here.
                // throw new DataAccessException("Optical Diagnostic with ID " + diagnosticId + " not found for deletion.");
            } else {
                logger.info("Optical diagnostic with ID {} deleted successfully.", diagnosticId);
            }
        } catch (DataAccessException e) {
            logger.error("Error deleting optical diagnostic with ID {}: {}", diagnosticId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    // --- Helper Methods ---
    private OpticalDiagnosticDTO mapRecordToDto(OpticaldiagnosticsRecord record) {
        if (record == null) return null;
        OpticalDiagnosticDTO dto = new OpticalDiagnosticDTO();
        dto.setDiagnosticId(record.getDiagnosticId());
        dto.setPatientId(record.getPatientId());
        dto.setDiagnosticDate(record.getDiagnosticDate());
        dto.setContactLensRx(record.getIsContactLensRx());
        dto.setContactLensDetails(record.getContactLensDetails());
        dto.setOdSphDist(record.getOdSphDist());
        dto.setOdCylDist(record.getOdCylDist());
        dto.setOdAxisDist(record.getOdAxisDist());
        dto.setOsSphDist(record.getOsSphDist());
        dto.setOsCylDist(record.getOsCylDist());
        dto.setOsAxisDist(record.getOsAxisDist());
        dto.setOdAdd(record.getOdAdd());
        dto.setOsAdd(record.getOsAdd());
        dto.setIpd(record.getIpd());
        dto.setRemarks(record.getRemarks());
        dto.setCreatedByUserId(record.getCreatedByUserId());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }

    private void mapDtoToRecord(OpticalDiagnosticDTO dto, OpticaldiagnosticsRecord record) {
        record.setPatientId(dto.getPatientId());
        record.setDiagnosticDate(dto.getDiagnosticDate());
        record.setIsContactLensRx(dto.isContactLensRx());
        record.setContactLensDetails(dto.getContactLensDetails());
        record.setOdSphDist(dto.getOdSphDist());
        record.setOdCylDist(dto.getOdCylDist());
        record.setOdAxisDist(dto.getOdAxisDist());
        record.setOsSphDist(dto.getOsSphDist());
        record.setOsCylDist(dto.getOsCylDist());
        record.setOsAxisDist(dto.getOsAxisDist());
        record.setOdAdd(dto.getOdAdd());
        record.setOsAdd(dto.getOsAdd());
        record.setIpd(dto.getIpd());
        record.setRemarks(dto.getRemarks());
        // created_by_user_id and created_at are set only for new records in the save method.
        // diagnostic_id is auto-increment or set before update.
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
