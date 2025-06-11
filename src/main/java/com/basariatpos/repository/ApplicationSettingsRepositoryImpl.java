package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.ApplicationsettingsRecord;
import com.basariatpos.model.ApplicationSettingDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.basariatpos.db.generated.Tables.APPLICATIONSSETTINGS;

public class ApplicationSettingsRepositoryImpl implements ApplicationSettingsRepository {

    private static final Logger logger = AppLogger.getLogger(ApplicationSettingsRepositoryImpl.class);

    @Override
    public Optional<ApplicationSettingDTO> findByKey(String key) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            ApplicationsettingsRecord record = dsl.selectFrom(APPLICATIONSSETTINGS)
                                                  .where(APPLICATIONSSETTINGS.SETTING_KEY.eq(key))
                                                  .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding application setting by key '{}': {}", key, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<ApplicationSettingDTO> findAll() {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            return dsl.selectFrom(APPLICATIONSSETTINGS)
                      .orderBy(APPLICATIONSSETTINGS.SETTING_KEY.asc())
                      .fetch()
                      .map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding all application settings: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public ApplicationSettingDTO save(ApplicationSettingDTO settingDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            // Using jOOQ's ON DUPLICATE KEY UPDATE (PostgreSQL's ON CONFLICT DO UPDATE)
            // This requires setting the primary key field before insert if it's part of the DTO and not auto-generated
            // For ApplicationSettings, setting_key is the PK.

            ApplicationsettingsRecord record = dsl.newRecord(APPLICATIONSSETTINGS);
            mapDtoToRecord(settingDto, record);

            // jOOQ's store() can handle insert or update if PK is set.
            // For explicit ON CONFLICT behavior:
            dsl.insertInto(APPLICATIONSSETTINGS)
               .set(record)
               .onConflict(APPLICATIONSSETTINGS.SETTING_KEY)
               .doUpdate()
               .set(APPLICATIONSSETTINGS.SETTING_VALUE, settingDto.getSettingValue())
               .set(APPLICATIONSSETTINGS.DESCRIPTION, settingDto.getDescription())
               // SETTING_KEY should not be updated in an ON CONFLICT scenario
               .execute();

            logger.info("Application setting '{}' saved successfully.", settingDto.getSettingKey());
            // The DTO is returned as is, as the key doesn't change and value/desc are already in it.
            return settingDto;

        } catch (DataAccessException e) {
            logger.error("Error saving application setting '{}': {}", settingDto.getSettingKey(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    private ApplicationSettingDTO mapRecordToDto(ApplicationsettingsRecord record) {
        if (record == null) return null;
        return new ApplicationSettingDTO(
                record.getSettingKey(),
                record.getSettingValue(),
                record.getDescription()
        );
    }

    private void mapDtoToRecord(ApplicationSettingDTO dto, ApplicationsettingsRecord record) {
        record.setSettingKey(dto.getSettingKey());
        record.setSettingValue(dto.getSettingValue());
        record.setDescription(dto.getDescription());
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
