package com.basariatpos.repository;

import com.basariatpos.model.ProductCategoryDTO;
import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository {

    /**
     * Finds a product category by its ID.
     * @param id The ID of the product category.
     * @return An Optional containing the ProductCategoryDTO if found, otherwise empty.
     */
    Optional<ProductCategoryDTO> findById(int id);

    /**
     * Finds a product category by its English name (case-insensitive).
     * @param nameEn The English name to search for.
     * @return An Optional containing the ProductCategoryDTO if found, otherwise empty.
     */
    Optional<ProductCategoryDTO> findByNameEn(String nameEn);

    /**
     * Finds a product category by its Arabic name.
     * @param nameAr The Arabic name to search for.
     * @return An Optional containing the ProductCategoryDTO if found, otherwise empty.
     */
    Optional<ProductCategoryDTO> findByNameAr(String nameAr);

    /**
     * Retrieves all product categories.
     * @return A list of ProductCategoryDTOs.
     */
    List<ProductCategoryDTO> findAll();

    /**
     * Saves (inserts or updates) a product category.
     * If categoryDto.getCategoryId() is 0 or less, it's treated as an insert.
     * Otherwise, it's an update.
     * @param categoryDto The ProductCategoryDTO to save.
     * @return The saved ProductCategoryDTO, updated with a generated ID if it was an insert.
     */
    ProductCategoryDTO save(ProductCategoryDTO categoryDto);

    /**
     * Deletes a product category by its ID.
     * This is a hard delete. Check isCategoryInUse before calling.
     * @param id The ID of the product category to delete.
     */
    void deleteById(int id);

    /**
     * Checks if the product category is currently linked to any entries in the Products table.
     * This is used to prevent deletion of categories that are in use.
     * @param id The ID of the product category to check.
     * @return true if the category is in use, false otherwise.
     */
    boolean isCategoryInUse(int id);
}
