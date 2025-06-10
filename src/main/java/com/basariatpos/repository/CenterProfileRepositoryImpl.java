package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.CenterprofileRecord;
import com.basariatpos.model.CenterProfileDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

import java.util.Optional;

import static com.basariatpos.db.generated.Tables.CENTERPROFILE;

public class CenterProfileRepositoryImpl implements CenterProfileRepository {

    private static final Logger logger = AppLogger.getLogger(CenterProfileRepositoryImpl.class);
    private static final int SINGLE_PROFILE_ID = 1; // As per design, only one profile record

    @Override
    public void save(CenterProfileDTO profileDto) {
        if (profileDto == null) {
            throw new IllegalArgumentException("CenterProfileDTO cannot be null.");
        }
        DSLContext dslContext = null;
        try {
            dslContext = DBManager.getDSLContext();
            if (dslContext == null) {
                logger.error("Cannot save profile: DSLContext is null.");
                throw new DataAccessException("Database not available");
            }

            CenterprofileRecord record = dslContext.fetchOne(CENTERPROFILE, CENTERPROFILE.PROFILE_ID.eq(SINGLE_PROFILE_ID));

            if (record == null) { // Insert new record
                record = dslContext.newRecord(CENTERPROFILE);
                record.setProfileId(SINGLE_PROFILE_ID); // Explicitly set the ID
                mapDtoToRecord(profileDto, record);
                record.store(); // Executes INSERT
                logger.info("Inserted new center profile with ID: {}", SINGLE_PROFILE_ID);
            } else { // Update existing record
                mapDtoToRecord(profileDto, record);
                record.store(); // Executes UPDATE
                logger.info("Updated center profile with ID: {}", SINGLE_PROFILE_ID);
            }
        } catch (DataAccessException e) {
            logger.error("Error saving center profile: {}", e.getMessage(), e);
            throw e; // Re-throw to be handled by service layer or caller
        } finally {
            closeContext(dslContext);
        }
    }

    @Override
    public Optional<CenterProfileDTO> getProfile() {
        DSLContext dslContext = null;
        try {
            dslContext = DBManager.getDSLContext();
            if (dslContext == null) {
                logger.error("Cannot get profile: DSLContext is null.");
                 // Or throw an exception, depending on how unavailable DB should be handled by callers
                return Optional.empty();
            }

            CenterprofileRecord record = dslContext.selectFrom(CENTERPROFILE)
                                                 .where(CENTERPROFILE.PROFILE_ID.eq(SINGLE_PROFILE_ID))
                                                 .fetchOne();

            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error retrieving center profile: {}", e.getMessage(), e);
            throw e; // Re-throw
        } finally {
            closeContext(dslContext);
        }
    }

    @Override
    public boolean exists() {
        DSLContext dslContext = null;
        try {
            dslContext = DBManager.getDSLContext();
             if (dslContext == null) {
                logger.error("Cannot check profile existence: DSLContext is null.");
                return false; // Or throw
            }
            return dslContext.fetchExists(
                    dslContext.selectOne()
                              .from(CENTERPROFILE)
                              .where(CENTERPROFILE.PROFILE_ID.eq(SINGLE_PROFILE_ID))
            );
        } catch (DataAccessException e) {
            logger.error("Error checking if center profile exists: {}", e.getMessage(), e);
            throw e; // Re-throw
        } finally {
            closeContext(dslContext);
        }
    }

    private void mapDtoToRecord(CenterProfileDTO dto, CenterprofileRecord record) {
        record.setCenterName(dto.getCenterName());
        record.setAddressLine1(dto.getAddressLine1());
        record.setAddressLine2(dto.getAddressLine2());
        record.setCity(dto.getCity());
        record.setCountry(dto.getCountry());
        record.setPostalCode(dto.getPostalCode());
        record.setPhonePrimary(dto.getPhonePrimary());
        record.setPhoneSecondary(dto.getPhoneSecondary());
        record.setEmailAddress(dto.getEmailAddress());
        record.setWebsite(dto.getWebsite());
        record.setLogoImagePath(dto.getLogoImagePath());
        record.setTaxIdentifier(dto.getTaxIdentifier());
        record.setCurrencySymbol(dto.getCurrencySymbol());
        record.setCurrencyCode(dto.getCurrencyCode());
        record.setReceiptFooterMessage(dto.getReceiptFooterMessage());
        // profile_id is set prior to this mapping for inserts
        // last_updated and created_at are handled by DB triggers/defaults
    }

    private CenterProfileDTO mapRecordToDto(CenterprofileRecord record) {
        CenterProfileDTO dto = new CenterProfileDTO();
        dto.setCenterName(record.getCenterName());
        dto.setAddressLine1(record.getAddressLine1());
        dto.setAddressLine2(record.getAddressLine2());
        dto.setCity(record.getCity());
        dto.setCountry(record.getCountry());
        dto.setPostalCode(record.getPostalCode());
        dto.setPhonePrimary(record.getPhonePrimary());
        dto.setPhoneSecondary(record.getPhoneSecondary());
        dto.setEmailAddress(record.getEmailAddress());
        dto.setWebsite(record.getWebsite());
        dto.setLogoImagePath(record.getLogoImagePath());
        dto.setTaxIdentifier(record.getTaxIdentifier());
        dto.setCurrencySymbol(record.getCurrencySymbol());
        dto.setCurrencyCode(record.getCurrencyCode());
        dto.setReceiptFooterMessage(record.getReceiptFooterMessage());
        return dto;
    }

    private void closeContext(DSLContext dslContext) {
        if (dslContext != null) {
            try {
                // Assuming DBManager.getDSLContext() provides a context that wraps a single connection
                // which should be closed after use.
                dslContext.close();
            } catch (Exception e) {
                logger.warn("Failed to close DSLContext.", e);
            }
        }
    }
}
