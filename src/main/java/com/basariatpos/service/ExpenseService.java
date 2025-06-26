package com.basariatpos.service;

import com.basariatpos.model.ExpenseDTO;
import com.basariatpos.service.exception.ExpenseValidationException;
import com.basariatpos.service.exception.NoActiveShiftException;
import com.basariatpos.service.exception.ExpenseException; // General exception
import com.basariatpos.service.exception.ExpenseNotFoundException; // For getById
// Assuming these specific exceptions exist or will be created:
import com.basariatpos.service.exception.CategoryNotFoundException;
import com.basariatpos.service.exception.BankNameNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseService {

    /**
     * Records a new expense.
     * Validates the expense data, checks for an active shift if it's a cash expense,
     * and ensures category and bank name (if applicable) exist.
     *
     * @param expenseDto The DTO containing details of the expense to be recorded.
     * @return The recorded ExpenseDTO, updated with generated ID and timestamps.
     * @throws ExpenseValidationException if expense data is invalid.
     * @throws NoActiveShiftException if the expense is paid by 'Cash' and no shift is active for the current user.
     * @throws CategoryNotFoundException if the specified expenseCategoryId does not correspond to an existing category.
     * @throws BankNameNotFoundException if paymentMethod requires a bank and the specified bankNameId does not exist.
     * @throws ExpenseException for other general errors during expense recording.
     */
    ExpenseDTO recordExpense(ExpenseDTO expenseDto)
        throws ExpenseValidationException, NoActiveShiftException, CategoryNotFoundException, BankNameNotFoundException, ExpenseException;

    /**
     * Finds expenses within a specified date range, optionally filtered by an expense category.
     *
     * @param fromDate The start date of the range (inclusive).
     * @param toDate The end date of the range (inclusive).
     * @param categoryIdFilter Optional. The ID of the expense category to filter by.
     *                         If null, expenses from all categories are returned.
     * @return A list of ExpenseDTOs matching the criteria.
     * @throws ExpenseException for errors during data retrieval.
     */
    List<ExpenseDTO> findExpenses(LocalDate fromDate, LocalDate toDate, Integer categoryIdFilter)
        throws ExpenseException;

    /**
     * Retrieves a specific expense by its ID.
     *
     * @param expenseId The ID of the expense to retrieve.
     * @return An Optional containing the ExpenseDTO if found, otherwise empty.
     * @throws ExpenseNotFoundException if no expense is found for the given ID (optional, could also return empty Optional).
     * @throws ExpenseException for other errors during data retrieval.
     */
    Optional<ExpenseDTO> getExpenseById(int expenseId)
        throws ExpenseNotFoundException, ExpenseException;

    // Update and delete methods for expenses are typically not provided or are highly restricted
    // unless specific business requirements allow for modification/deletion of recorded expenses.
    // void updateExpense(ExpenseDTO expenseDto) throws ExpenseValidationException, ExpenseNotFoundException, ...;
    // void deleteExpense(int expenseId) throws ExpenseNotFoundException, ...;
}
