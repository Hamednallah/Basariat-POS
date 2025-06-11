package com.basariatpos.service;

import com.basariatpos.model.ExpenseDTO; // Assuming this DTO will be created later
import com.basariatpos.service.exception.NoActiveShiftException;
// import java.util.List;
// import java.util.Optional;

/**
 * Service interface for managing expenses.
 * Methods will be defined in later sprints.
 */
public interface ExpenseService {

    /**
     * Records a cash expense made from the till.
     * This is a placeholder method signature.
     *
     * @param expenseDto The DTO containing details of the expense.
     * @return The created ExpenseDTO, possibly updated with an ID.
     * @throws NoActiveShiftException if no shift is active for the current user.
     * @throws ValidationException if expense data is invalid.
     * @throws CategoryNotFoundException if the specified expense category is not found.
     * @throws ExpenseException for other expense related errors.
     */
    // ExpenseDTO recordCashExpenseFromTill(ExpenseDTO expenseDto)
    //    throws NoActiveShiftException, ValidationException, CategoryNotFoundException, ExpenseException;

    // Other methods like:
    // ExpenseDTO recordGeneralExpense(ExpenseDTO expenseDto); // For expenses not directly from till cash
    // Optional<ExpenseDTO> getExpenseById(int expenseId);
    // List<ExpenseDTO> findExpensesByCriteria(/* criteria, date ranges, category, user */);
    // void updateExpense(ExpenseDTO expenseDto);
    // void deleteExpense(int expenseId);
}

// Placeholder for custom exceptions related to Expense if needed later
// class ExpenseException extends RuntimeException {
//     public ExpenseException(String message) { super(message); }
//     public ExpenseException(String message, Throwable cause) { super(message, cause); }
// }
