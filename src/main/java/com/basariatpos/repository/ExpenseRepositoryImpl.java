package com.basariatpos.repository;

import com.basariatpos.config.DBManager; // Assuming DBManager for DSLContext
import com.basariatpos.model.ExpenseDTO;
import com.basariatpos.db.generated.tables.records.ExpensesRecord; // jOOQ generated record

import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.*; // jOOQ generated tables

public class ExpenseRepositoryImpl implements ExpenseRepository {

    private static final Logger log = LoggerFactory.getLogger(ExpenseRepositoryImpl.class);
    private final DSLContext dsl;

    public ExpenseRepositoryImpl() {
        // Default constructor, assumes DBManager will provide context or uses a default context
        this.dsl = DBManager.getDSLContext(); // Or inject if preferred
    }

    // Constructor for explicit DSLContext injection (useful for testing)
    public ExpenseRepositoryImpl(DSLContext dslContext) {
        this.dsl = dslContext;
    }

    private ExpenseDTO mapRecordToExpenseDTO(Record r) {
        if (r == null) return null;
        ExpenseDTO dto = new ExpenseDTO();
        ExpensesRecord expenseRecord = r.into(EXPENSES); // Assuming EXPENSES is the jOOQ table

        dto.setExpenseId(expenseRecord.getExpenseId());
        dto.setExpenseDate(expenseRecord.getExpenseDate());
        dto.setExpenseCategoryId(expenseRecord.getExpenseCategoryId());
        dto.setDescription(expenseRecord.getDescription());
        dto.setAmount(expenseRecord.getAmount());
        dto.setPaymentMethod(expenseRecord.getPaymentMethod());
        dto.setBankNameId(expenseRecord.getBankNameId());
        dto.setTransactionIdRef(expenseRecord.getTransactionIdRef());
        dto.setCreatedByUserId(expenseRecord.getCreatedByUserId());
        dto.setShiftId(expenseRecord.getShiftId());
        dto.setCreatedAt(expenseRecord.getCreatedAt());
        dto.setUpdatedAt(expenseRecord.getUpdatedAt());

        // Populate display names from joined tables (check if fields exist in record)
        if (r.field(EXPENSECATEGORIES.CATEGORY_NAME_EN) != null) {
            dto.setCategoryNameEnDisplay(r.get(EXPENSECATEGORIES.CATEGORY_NAME_EN));
        }
        if (r.field(EXPENSECATEGORIES.CATEGORY_NAME_AR) != null) {
            dto.setCategoryNameArDisplay(r.get(EXPENSECATEGORIES.CATEGORY_NAME_AR));
        }
        if (r.field(USERS.FULL_NAME) != null) { // Assuming FULL_NAME for user display
            dto.setCreatedByNameDisplay(r.get(USERS.FULL_NAME));
        }
        if (r.field(BANKNAMES.BANK_NAME_EN) != null) {
            dto.setBankNameDisplayEn(r.get(BANKNAMES.BANK_NAME_EN));
        }
        if (r.field(BANKNAMES.BANK_NAME_AR) != null) {
            dto.setBankNameDisplayAr(r.get(BANKNAMES.BANK_NAME_AR));
        }
        return dto;
    }

    @Override
    public ExpenseDTO save(ExpenseDTO expenseDto) throws RepositoryException {
        log.debug("Saving expense: {}", expenseDto.getDescription());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        try {
            ExpensesRecord record;
            if (expenseDto.getExpenseId() == 0) { // Insert
                record = dsl.newRecord(EXPENSES);
                record.setCreatedByUserId(expenseDto.getCreatedByUserId()); // Must be set by service
                record.setCreatedAt(now);
            } else { // Update
                record = dsl.fetchOne(EXPENSES, EXPENSES.EXPENSE_ID.eq(expenseDto.getExpenseId()));
                if (record == null) {
                    throw new RepositoryException("Expense with ID " + expenseDto.getExpenseId() + " not found for update.");
                }
            }

            record.setExpenseDate(expenseDto.getExpenseDate());
            record.setExpenseCategoryId(expenseDto.getExpenseCategoryId());
            record.setDescription(expenseDto.getDescription());
            record.setAmount(expenseDto.getAmount());
            record.setPaymentMethod(expenseDto.getPaymentMethod());
            record.setBankNameId(expenseDto.getBankNameId()); // Nullable
            record.setTransactionIdRef(expenseDto.getTransactionIdRef()); // Nullable
            record.setShiftId(expenseDto.getShiftId()); // Nullable, set by service for cash
            record.setUpdatedAt(now);

            record.store(); // Handles insert or update

            expenseDto.setExpenseId(record.getExpenseId());
            expenseDto.setCreatedAt(record.getCreatedAt()); // Ensure DTO has these if it was an insert
            expenseDto.setUpdatedAt(record.getUpdatedAt());

            // For display names, it's often better to re-fetch or rely on service to populate.
            // However, if this save is simple and doesn't change related entities, we can assume
            // any display names already on DTO are fine, or clear them if they could be stale.
            // For simplicity, we return the DTO; service layer can re-fetch if needed.

            log.info("Expense {} successfully with ID: {}", (expenseDto.getExpenseId() == record.getExpenseId() && expenseDto.getCreatedAt() != null ? "updated" : "created"), record.getExpenseId());
            return expenseDto; // Return the DTO, now with ID and timestamps

        } catch (DataAccessException e) {
            log.error("Error saving expense '{}': {}", expenseDto.getDescription(), e.getMessage(), e);
            throw new RepositoryException("Failed to save expense: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ExpenseDTO> findById(int expenseId) throws RepositoryException {
        log.debug("Finding expense by ID: {}", expenseId);
        try {
            Record record = dsl.select(EXPENSES.asterisk(),
                                EXPENSECATEGORIES.CATEGORY_NAME_EN, EXPENSECATEGORIES.CATEGORY_NAME_AR,
                                USERS.FULL_NAME, // Assuming user's full name for display
                                BANKNAMES.BANK_NAME_EN, BANKNAMES.BANK_NAME_AR)
                    .from(EXPENSES)
                    .join(EXPENSECATEGORIES).on(EXPENSES.EXPENSE_CATEGORY_ID.eq(EXPENSECATEGORIES.EXPENSE_CATEGORY_ID))
                    .join(USERS).on(EXPENSES.CREATED_BY_USER_ID.eq(USERS.USER_ID))
                    .leftOuterJoin(BANKNAMES).on(EXPENSES.BANK_NAME_ID.eq(BANKNAMES.BANK_NAME_ID))
                    .where(EXPENSES.EXPENSE_ID.eq(expenseId))
                    .fetchOne();

            return Optional.ofNullable(record).map(this::mapRecordToExpenseDTO);
        } catch (DataAccessException e) {
            log.error("Error finding expense by ID {}: {}", expenseId, e.getMessage(), e);
            throw new RepositoryException("Failed to find expense by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExpenseDTO> findAllFiltered(LocalDate fromDate, LocalDate toDate, Integer categoryIdFilter) throws RepositoryException {
        log.debug("Finding expenses from {} to {}, category filter: {}", fromDate, toDate, categoryIdFilter);
        try {
            Condition condition = DSL.trueCondition();
            if (fromDate != null) {
                condition = condition.and(EXPENSES.EXPENSE_DATE.ge(fromDate));
            }
            if (toDate != null) {
                condition = condition.and(EXPENSES.EXPENSE_DATE.le(toDate));
            }
            if (categoryIdFilter != null && categoryIdFilter > 0) {
                condition = condition.and(EXPENSES.EXPENSE_CATEGORY_ID.eq(categoryIdFilter));
            }

            return dsl.select(EXPENSES.asterisk(),
                            EXPENSECATEGORIES.CATEGORY_NAME_EN, EXPENSECATEGORIES.CATEGORY_NAME_AR,
                            USERS.FULL_NAME, // Assuming user's full name for display
                            BANKNAMES.BANK_NAME_EN, BANKNAMES.BANK_NAME_AR)
                    .from(EXPENSES)
                    .join(EXPENSECATEGORIES).on(EXPENSES.EXPENSE_CATEGORY_ID.eq(EXPENSECATEGORIES.EXPENSE_CATEGORY_ID))
                    .join(USERS).on(EXPENSES.CREATED_BY_USER_ID.eq(USERS.USER_ID))
                    .leftOuterJoin(BANKNAMES).on(EXPENSES.BANK_NAME_ID.eq(BANKNAMES.BANK_NAME_ID))
                    .where(condition)
                    .orderBy(EXPENSES.EXPENSE_DATE.desc(), EXPENSES.CREATED_AT.desc())
                    .fetch(this::mapRecordToExpenseDTO);
        } catch (DataAccessException e) {
            log.error("Error finding filtered expenses: {}", e.getMessage(), e);
            throw new RepositoryException("Failed to find filtered expenses: " + e.getMessage(), e);
        }
    }
}
