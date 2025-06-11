package com.basariatpos.service;

import com.basariatpos.model.ProductCategoryDTO;
import java.util.List;
import java.util.Optional;

// Assuming these exceptions are defined (e.g., in a common place or alongside ExpenseCategoryService)
// For example:
// class CategoryException extends RuntimeException { public CategoryException(String m) { super(m); } public CategoryException(String m, Throwable c) { super(m,c); }}
// class CategoryNotFoundException extends CategoryException { public CategoryNotFoundException(int id) { super("Category with ID " + id + " not found."); } }
// class CategoryAlreadyExistsException extends CategoryException { public CategoryAlreadyExistsException(String name) { super("Category '" + name + "' already exists."); } }
// class CategoryInUseException extends CategoryException { public CategoryInUseException(int id) { super("Category ID " + id + " is in use."); } }
// class ValidationException extends CategoryException { public ValidationException(String m, List<String> e) { super(m); } public List<String> getErrors(){ return null;} }


public interface ProductCategoryService {

    /**
     * Retrieves a product category by its ID.
     * @param id The ID of the product category.
     * @return An Optional containing the ProductCategoryDTO if found.
     * @throws CategoryException if a service-level error occurs.
     */
    Optional<ProductCategoryDTO> getProductCategoryById(int id) throws CategoryException;

    /**
     * Retrieves all product categories.
     * @return A list of ProductCategoryDTOs.
     * @throws CategoryException if a service-level error occurs.
     */
    List<ProductCategoryDTO> getAllProductCategories() throws CategoryException;


    /**
     * Saves (creates or updates) a product category.
     * Validates required fields and checks for uniqueness of English name.
     * @param categoryDto The DTO containing category details.
     * @return The saved ProductCategoryDTO, updated with ID if new.
     * @throws ValidationException if category data is invalid.
     * @throws CategoryAlreadyExistsException if the English or Arabic name already exists.
     * @throws CategoryException if a service-level error occurs.
     */
    ProductCategoryDTO saveProductCategory(ProductCategoryDTO categoryDto) throws ValidationException, CategoryAlreadyExistsException, CategoryException;

    /**
     * Deletes a product category by its ID.
     * Prevents deletion if the category is currently in use by any products.
     * @param id The ID of the category to delete.
     * @throws CategoryNotFoundException if the category is not found.
     * @throws CategoryInUseException if the category is in use.
     * @throws CategoryException if a service-level error occurs.
     */
    void deleteProductCategory(int id) throws CategoryNotFoundException, CategoryInUseException, CategoryException;
}
