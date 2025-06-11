package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.ExpensecategoriesRecord;
import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.basariatpos.db.generated.Tables.EXPENSECATEGORIES;
import static com.basariatpos.db.generated.Tables.EXPENSES; // For isCategoryInUse check

public class ExpenseCategoryRepositoryImpl implements ExpenseCategoryRepository {

    private static final Logger logger = AppLogger.getLogger(ExpenseCategoryRepositoryImpl.class);
    // Define the special category name that might have restrictions
    public static final String LOSS_ON_ABANDONED_ORDERS_EN = "Loss on Abandoned Orders";
    public static final String LOSS_ON_ABANDONED_ORDERS_AR = "خسارة الطلبات الملغاة";


    @Override
    public Optional<ExpenseCategoryDTO> findById(int id) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            ExpensecategoriesRecord record = dsl.selectFrom(EXPENSECATEGORIES)
                                                .where(EXPENSECATEGORIES.EXPENSE_CATEGORY_ID.eq(id))
                                                .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding expense category by ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<ExpenseCategoryDTO> findByNameEn(String nameEn) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            ExpensecategoriesRecord record = dsl.selectFrom(EXPENSECATEGORIES)
                                                .where(DSL.lower(EXPENSECATEGORIES.CATEGORY_NAME_EN).eq(nameEn.toLowerCase()))
                                                .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding expense category by English name '{}': {}", nameEn, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<ExpenseCategoryDTO> findByNameAr(String nameAr) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            ExpensecategoriesRecord record = dsl.selectFrom(EXPENSECATEGORIES)
                                                .where(EXPENSECATEGORIES.CATEGORY_NAME_AR.eq(nameAr))
                                                .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding expense category by Arabic name '{}': {}", nameAr, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<ExpenseCategoryDTO> findAll(boolean includeInactive) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            Condition condition = DSL.trueCondition();
            if (!includeInactive) {
                condition = condition.and(EXPENSECATEGORIES.IS_ACTIVE.isTrue());
            }
            return dsl.selectFrom(EXPENSECATEGORIES)
                      .where(condition)
                      .orderBy(EXPENSECATEGORIES.CATEGORY_NAME_EN.asc())
                      .fetch()
                      .map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding all expense categories (includeInactive={}): {}", includeInactive, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public ExpenseCategoryDTO save(ExpenseCategoryDTO categoryDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            ExpensecategoriesRecord record;
            if (categoryDto.getExpenseCategoryId() > 0) { // Update
                record = dsl.fetchOne(EXPENSECATEGORIES, EXPENSECATEGORIES.EXPENSE_CATEGORY_ID.eq(categoryDto.getExpenseCategoryId()));
                if (record == null) {
                    throw new DataAccessException("Expense category with ID " + categoryDto.getExpenseCategoryId() + " not found for update.");
                }
            } else { // Insert
                record = dsl.newRecord(EXPENSECATEGORIES);
            }
            mapDtoToRecord(categoryDto, record);
            record.store();

            if (categoryDto.getExpenseCategoryId() <= 0) {
                categoryDto.setExpenseCategoryId(record.getExpenseCategoryId());
            }
            logger.info("Expense category '{}' (ID: {}) saved successfully.", categoryDto.getCategoryNameEn(), categoryDto.getExpenseCategoryId());
            return categoryDto;
        } catch (DataAccessException e) {
            logger.error("Error saving expense category '{}': {}", categoryDto.getCategoryNameEn(), e.getMessage(), e);
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

            int updatedRows = dsl.update(EXPENSECATEGORIES)
                                 .set(EXPENSECATEGORIES.IS_ACTIVE, isActive)
                                 .where(EXPENSECATEGORIES.EXPENSE_CATEGORY_ID.eq(id))
                                 .execute();
            if (updatedRows == 0) {
                throw new DataAccessException("Expense category with ID " + id + " not found for status update.");
            }
            logger.info("Active status for expense category ID {} set to {}.", id, isActive);
        } catch (DataAccessException e) {
            logger.error("Error setting active status for expense category ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public boolean isCategoryInUse(int id) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            return dsl.fetchExists(
                dsl.selectOne()
                   .from(EXPENSES)
                   .where(EXPENSES.EXPENSE_CATEGORY_ID.eq(id))
            );
        } catch (DataAccessException e) {
            logger.error("Error checking if expense category ID {} is in use: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    private ExpenseCategoryDTO mapRecordToDto(ExpensecategoriesRecord record) {
        if (record == null) return null;
        return new ExpenseCategoryDTO(
                record.getExpenseCategoryId(),
                record.getCategoryNameEn(),
                record.getCategoryNameAr(),
                record.getIsActive()
        );
    }

    private void mapDtoToRecord(ExpenseCategoryDTO dto, ExpensecategoriesRecord record) {
        record.setCategoryNameEn(dto.getCategoryNameEn());
        record.setCategoryNameAr(dto.getCategoryNameAr());
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
