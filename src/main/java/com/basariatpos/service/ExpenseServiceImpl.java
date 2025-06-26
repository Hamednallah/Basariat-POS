package com.basariatpos.service;

import com.basariatpos.model.ExpenseDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.ExpenseRepository;
import com.basariatpos.service.exception.*; // Import all custom exceptions
import com.basariatpos.util.AppLogger;

import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpenseServiceImpl implements ExpenseService {

    private static final Logger logger = AppLogger.getLogger(ExpenseServiceImpl.class);

    private final ExpenseRepository expenseRepository;
    private final UserSessionService userSessionService;
    private final ExpenseCategoryService expenseCategoryService;
    private final BankNameService bankNameService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                              UserSessionService userSessionService,
                              ExpenseCategoryService expenseCategoryService,
                              BankNameService bankNameService) {
        this.expenseRepository = expenseRepository;
        this.userSessionService = userSessionService;
        this.expenseCategoryService = expenseCategoryService;
        this.bankNameService = bankNameService;
    }

    @Override
    public ExpenseDTO recordExpense(ExpenseDTO expenseDto)
            throws ExpenseValidationException, NoActiveShiftException, CategoryNotFoundException, BankNameNotFoundException, ExpenseException {

        logger.info("Attempting to record expense: {}", expenseDto.getDescription());
        UserDTO currentUser = userSessionService.getCurrentUser();
        if (currentUser == null) {
            // This case should ideally be prevented by UI or earlier checks if an operation requires auth
            throw new ExpenseException("No authenticated user found. Cannot record expense.");
        }

        validateExpenseDto(expenseDto);

        // Set system-managed fields
        expenseDto.setCreatedByUserId(currentUser.getUserId());

        String paymentMethod = expenseDto.getPaymentMethod();
        if ("Cash".equalsIgnoreCase(paymentMethod)) {
            if (!userSessionService.isShiftActive() || userSessionService.getActiveShift() == null) {
                logger.warn("Cash expense recording failed: No active shift for user {}.", currentUser.getUsername());
                throw new NoActiveShiftException("Cannot record cash expense: No active shift for the current user.");
            }
            expenseDto.setShiftId(userSessionService.getActiveShift().getShiftId());
            expenseDto.setBankNameId(null); // Ensure bank details are null for cash
            expenseDto.setTransactionIdRef(null);
        } else if (isBankRelatedPayment(paymentMethod)) {
            if (expenseDto.getBankNameId() == null || expenseDto.getBankNameId() <= 0) {
                throw new ExpenseValidationException("Bank name is required for payment method: " + paymentMethod,
                                                     List.of("Bank name is required for " + paymentMethod + "."));
            }
            // Validate bank name existence
            bankNameService.getBankNameById(expenseDto.getBankNameId())
                .orElseThrow(() -> new BankNameNotFoundException(expenseDto.getBankNameId()));

            if (expenseDto.getTransactionIdRef() == null || expenseDto.getTransactionIdRef().trim().isEmpty()) {
                throw new ExpenseValidationException("Transaction ID/Reference is required for payment method: " + paymentMethod,
                                                     List.of("Transaction ID/Reference is required for " + paymentMethod + "."));
            }
            expenseDto.setShiftId(null); // Bank transactions are not tied to a cash shift
        } else {
            // For other payment methods that are not cash and not explicitly bank-related,
            // ensure bank details and shift ID are null.
            expenseDto.setBankNameId(null);
            expenseDto.setTransactionIdRef(null);
            expenseDto.setShiftId(null);
        }

        try {
            return expenseRepository.save(expenseDto);
        } catch (RepositoryException e) {
            logger.error("Repository error while recording expense: {}", e.getMessage(), e);
            throw new ExpenseException("Failed to record expense due to a data access error.", e);
        }
    }

    private void validateExpenseDto(ExpenseDTO dto) throws ExpenseValidationException, CategoryNotFoundException {
        List<String> errors = new ArrayList<>();
        if (dto.getExpenseDate() == null) {
            errors.add("Expense date is required.");
        }
        if (dto.getExpenseCategoryId() <= 0) {
            errors.add("Expense category is required.");
        } else {
            // Validate category existence
            expenseCategoryService.getExpenseCategoryById(dto.getExpenseCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(dto.getExpenseCategoryId()));
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            errors.add("Description is required.");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Amount must be positive.");
        }
        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().trim().isEmpty()) {
            errors.add("Payment method is required.");
        }

        if (!errors.isEmpty()) {
            throw new ExpenseValidationException(errors);
        }
    }

    private boolean isBankRelatedPayment(String paymentMethod) {
        if (paymentMethod == null) return false;
        String methodUpper = paymentMethod.toUpperCase();
        return methodUpper.contains("BANK") || methodUpper.contains("CARD") || methodUpper.contains("CHEQUE");
    }

    @Override
    public List<ExpenseDTO> findExpenses(LocalDate fromDate, LocalDate toDate, Integer categoryIdFilter) throws ExpenseException {
        logger.debug("Finding expenses from {} to {}, category filter: {}", fromDate, toDate, categoryIdFilter);
        try {
            return expenseRepository.findAllFiltered(fromDate, toDate, categoryIdFilter);
        } catch (RepositoryException e) {
            logger.error("Repository error while finding expenses: {}", e.getMessage(), e);
            throw new ExpenseException("Failed to find expenses due to a data access error.", e);
        }
    }

    @Override
    public Optional<ExpenseDTO> getExpenseById(int expenseId) throws ExpenseNotFoundException, ExpenseException {
        logger.debug("Getting expense by ID: {}", expenseId);
        try {
            // The repository findById might return empty Optional, which is fine.
            // An explicit ExpenseNotFoundException could be thrown here if strict "must exist" is needed by callers.
            // For now, mirroring repository's Optional return.
            return expenseRepository.findById(expenseId);
        } catch (RepositoryException e) {
            logger.error("Repository error while getting expense by ID {}: {}", expenseId, e.getMessage(), e);
            throw new ExpenseException("Failed to get expense by ID due to a data access error.", e);
        }
    }
}
