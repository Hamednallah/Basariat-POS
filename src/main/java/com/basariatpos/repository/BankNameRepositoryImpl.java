package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.BanknamesRecord;
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.basariatpos.db.generated.Tables.BANKNAMES;

public class BankNameRepositoryImpl implements BankNameRepository {

    private static final Logger logger = AppLogger.getLogger(BankNameRepositoryImpl.class);

    @Override
    public Optional<BankNameDTO> findById(int id) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            BanknamesRecord record = dsl.selectFrom(BANKNAMES)
                                        .where(BANKNAMES.BANK_NAME_ID.eq(id))
                                        .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding bank name by ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<BankNameDTO> findByNameEn(String nameEn) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            // Using lower for case-insensitive search, assuming DB collation supports it well
            // or consider ILIKE if using plain SQL. jOOQ's eqIgnoreCase might be better if available/configured.
            BanknamesRecord record = dsl.selectFrom(BANKNAMES)
                                        .where(DSL.lower(BANKNAMES.BANK_NAME_EN).eq(nameEn.toLowerCase()))
                                        .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding bank name by English name '{}': {}", nameEn, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<BankNameDTO> findByNameAr(String nameAr) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            BanknamesRecord record = dsl.selectFrom(BANKNAMES)
                                        .where(BANKNAMES.BANK_NAME_AR.eq(nameAr)) // Arabic comparison often exact
                                        .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding bank name by Arabic name '{}': {}", nameAr, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }


    @Override
    public List<BankNameDTO> findAll(boolean includeInactive) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            Condition condition = DSL.trueCondition();
            if (!includeInactive) {
                condition = condition.and(BANKNAMES.IS_ACTIVE.isTrue());
            }
            return dsl.selectFrom(BANKNAMES)
                      .where(condition)
                      .orderBy(BANKNAMES.BANK_NAME_EN.asc())
                      .fetch()
                      .map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding all bank names (includeInactive={}): {}", includeInactive, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public BankNameDTO save(BankNameDTO bankNameDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            BanknamesRecord record;
            if (bankNameDto.getBankNameId() > 0) { // Update
                record = dsl.fetchOne(BANKNAMES, BANKNAMES.BANK_NAME_ID.eq(bankNameDto.getBankNameId()));
                if (record == null) {
                    throw new DataAccessException("Bank name with ID " + bankNameDto.getBankNameId() + " not found for update.");
                }
            } else { // Insert
                record = dsl.newRecord(BANKNAMES);
            }
            mapDtoToRecord(bankNameDto, record);
            record.store(); // Inserts or updates based on record state

            // Update DTO with generated ID if it was an insert
            if (bankNameDto.getBankNameId() <= 0) {
                bankNameDto.setBankNameId(record.getBankNameId());
            }
            logger.info("Bank name '{}' (ID: {}) saved successfully.", bankNameDto.getBankNameEn(), bankNameDto.getBankNameId());
            return bankNameDto;
        } catch (DataAccessException e) {
            logger.error("Error saving bank name '{}': {}", bankNameDto.getBankNameEn(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void setActiveStatus(int id, boolean isActive) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            int updatedRows = dsl.update(BANKNAMES)
                                 .set(BANKNAMES.IS_ACTIVE, isActive)
                                 .where(BANKNAMES.BANK_NAME_ID.eq(id))
                                 .execute();
            if (updatedRows == 0) {
                throw new DataAccessException("Bank name with ID " + id + " not found for status update.");
            }
            logger.info("Active status for bank name ID {} set to {}.", id, isActive);
        } catch (DataAccessException e) {
            logger.error("Error setting active status for bank name ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    private BankNameDTO mapRecordToDto(BanknamesRecord record) {
        if (record == null) return null;
        return new BankNameDTO(
                record.getBankNameId(),
                record.getBankNameEn(),
                record.getBankNameAr(),
                record.getIsActive()
        );
    }

    private void mapDtoToRecord(BankNameDTO dto, BanknamesRecord record) {
        // bank_name_id is not set here for inserts if it's auto-generated by DB
        // For updates, it's used in the where clause and shouldn't be changed.
        record.setBankNameEn(dto.getBankNameEn());
        record.setBankNameAr(dto.getBankNameAr());
        record.setIsActive(dto.isActive());
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
