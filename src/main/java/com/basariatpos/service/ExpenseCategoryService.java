package com.basariatpos.service;

import com.basariatpos.model.ExpenseCategoryDTO;
import java.util.List;
import java.util.Optional;

// Custom Exceptions for ExpenseCategoryService
class CategoryException extends RuntimeException {
    public CategoryException(String message) { super(message); }
    public CategoryException(String message, Throwable cause) { super(message, cause); }
}

class CategoryNotFoundException extends CategoryException {
    public CategoryNotFoundException(int id) { super("Expense category with ID " + id + " not found.");}
    public CategoryNotFoundException(String name) { super("Expense category with name '" + name + "' not found.");}
}

class CategoryAlreadyExistsException extends CategoryException {
    public CategoryAlreadyExistsException(String name) { super("Expense category with name '" + name + "' already exists."); }
}

class CategoryInUseException extends CategoryException {
    public CategoryInUseException(int id) { super("Expense category with ID " + id + " is currently in use and cannot be deactivated or deleted.");}
    public CategoryInUseException(String name) { super("Expense category '" + name + "' is currently in use and cannot be deactivated or deleted.");}
}

// Reusing ValidationException from UserService.java or common package
// If not available, define:
// class ValidationException extends CategoryException {
//     private List<String> errors;
//     public ValidationException(String message, List<String> errors) { super(message); this.errors = errors; }
//     public List<String> getErrors() { return errors; }
// }


public interface ExpenseCategoryService {

    Optional<ExpenseCategoryDTO> getExpenseCategoryById(int id) throws CategoryException;

    List<ExpenseCategoryDTO> getAllExpenseCategories(boolean includeInactive) throws CategoryException;

    List<ExpenseCategoryDTO> getActiveExpenseCategories() throws CategoryException;

    /**
     * Saves (creates or updates) an expense category.
     * Validates required fields and checks for uniqueness of English name.
     * Prevents modification of name for special categories like "Loss on Abandoned Orders".
     * @param categoryDto The DTO containing category details.
     * @return The saved ExpenseCategoryDTO, updated with ID if new.
     * @throws ValidationException if category data is invalid.
     * @throws CategoryAlreadyExistsException if the English or Arabic name already exists.
     * @throws CategoryException if a service-level error occurs or a protected category is improperly modified.
     */
    ExpenseCategoryDTO saveExpenseCategory(ExpenseCategoryDTO categoryDto) throws ValidationException, CategoryAlreadyExistsException, CategoryException;

    /**
     * Toggles the active status of an expense category.
     * If active, it becomes inactive. If inactive, it becomes active.
     * Prevents deactivation of special categories or categories currently in use.
     * @param id The ID of the category to toggle.
     * @throws CategoryNotFoundException if the category is not found.
     * @throws CategoryInUseException if the category is in use and cannot be deactivated.
     * @throws CategoryException if a service-level error occurs or a protected category status change is attempted.
     */
    void toggleExpenseCategoryStatus(int id) throws CategoryNotFoundException, CategoryInUseException, CategoryException;

    /**
     * Checks if a category is a special, system-protected category (e.g., "Loss on Abandoned Orders").
     * @param categoryDto The category DTO to check.
     * @return true if it's a protected category, false otherwise.
     */
    boolean isProtectedCategory(ExpenseCategoryDTO categoryDto);
}
