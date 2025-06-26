package com.basariatpos.repository;

import com.basariatpos.model.ExpenseDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository {

    /**
     * Saves an expense record to the database (insert or update).
     * If expenseDto.getExpenseId() is 0 or not set, it's an insert.
     * Otherwise, it's an update.
     *
     * @param expenseDto The expense data to save.
     * @return The saved ExpenseDTO, updated with information from the database (like generated ID, timestamps).
     * @throws RepositoryException if there is an issue during the database operation.
     */
    ExpenseDTO save(ExpenseDTO expenseDto) throws RepositoryException;

    /**
     * Finds a specific expense by its unique ID.
     *
     * @param expenseId The ID of the expense.
     * @return An Optional containing the ExpenseDTO if found, or an empty Optional if not.
     * @throws RepositoryException if there is an issue during the database operation.
     */
    Optional<ExpenseDTO> findById(int expenseId) throws RepositoryException;

    /**
     * Finds all expenses within a given date range, optionally filtered by expense category.
     *
     * @param fromDate The start date of the range (inclusive).
     * @param toDate The end date of the range (inclusive).
     * @param categoryIdFilter Optional. The ID of the expense category to filter by.
     *                         If null, expenses from all categories are returned.
     * @return A list of ExpenseDTOs matching the criteria.
     * @throws RepositoryException if there is an issue during the database operation.
     */
    List<ExpenseDTO> findAllFiltered(LocalDate fromDate, LocalDate toDate, Integer categoryIdFilter) throws RepositoryException;
}
