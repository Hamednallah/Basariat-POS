package com.basariatpos.repository;

import com.basariatpos.model.ExpenseCategoryDTO;
import java.util.List;
import java.util.Optional;

public interface ExpenseCategoryRepository {

    /**
     * Finds an expense category by its ID.
     * @param id The ID of the expense category.
     * @return An Optional containing the ExpenseCategoryDTO if found, otherwise empty.
     */
    Optional<ExpenseCategoryDTO> findById(int id);

    /**
     * Finds an expense category by its English name (case-insensitive).
     * @param nameEn The English name to search for.
     * @return An Optional containing the ExpenseCategoryDTO if found, otherwise empty.
     */
    Optional<ExpenseCategoryDTO> findByNameEn(String nameEn);

    /**
     * Finds an expense category by its Arabic name.
     * @param nameAr The Arabic name to search for.
     * @return An Optional containing the ExpenseCategoryDTO if found, otherwise empty.
     */
    Optional<ExpenseCategoryDTO> findByNameAr(String nameAr);

    /**
     * Retrieves all expense categories.
     * @param includeInactive true to include inactive categories, false to retrieve only active ones.
     * @return A list of ExpenseCategoryDTOs.
     */
    List<ExpenseCategoryDTO> findAll(boolean includeInactive);

    /**
     * Saves (inserts or updates) an expense category.
     * If categoryDto.getExpenseCategoryId() is 0 or less, it's treated as an insert.
     * Otherwise, it's an update.
     * @param categoryDto The ExpenseCategoryDTO to save.
     * @return The saved ExpenseCategoryDTO, updated with a generated ID if it was an insert.
     */
    ExpenseCategoryDTO save(ExpenseCategoryDTO categoryDto);

    /**
     * Sets the active status of an expense category.
     * @param id The ID of the expense category.
     * @param isActive true to activate, false to deactivate.
     */
    void setActiveStatus(int id, boolean isActive);

    /**
     * Checks if the expense category is currently linked to any entries in the Expenses table.
     * This is used to prevent deletion or deactivation of categories that are in use.
     * @param id The ID of the expense category to check.
     * @return true if the category is in use, false otherwise.
     */
    boolean isCategoryInUse(int id);
}
