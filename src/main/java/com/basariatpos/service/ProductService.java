package com.basariatpos.service;

import com.basariatpos.model.ProductDTO;
import com.basariatpos.service.exception.ProductAlreadyExistsException;
import com.basariatpos.service.exception.ProductInUseException;
import com.basariatpos.service.exception.ProductNotFoundException;
import com.basariatpos.service.exception.ProductServiceException;
import com.basariatpos.service.exception.ProductValidationException;
// Assuming ValidationException is ProductValidationException or a common one
// For CategoryNotFoundException, it might be from ExpenseCategoryService if we use its exceptions directly, or define a more generic one.

import java.util.List;
import java.util.Optional;

public interface ProductService {

    /**
     * Saves (creates or updates) a product.
     * Validates DTO, checks for product code uniqueness on create.
     * Validates that the provided categoryId exists.
     * @param productDto The DTO containing product details.
     * @return The saved ProductDTO, updated with ID if new.
     * @throws ProductValidationException if product data is invalid.
     * @throws CategoryNotFoundException if the categoryId in productDto does not refer to an existing category.
     * @throws ProductAlreadyExistsException if the product code already exists (for new products).
     * @throws ProductServiceException for other service or repository level errors.
     */
    ProductDTO saveProduct(ProductDTO productDto)
        throws ProductValidationException, CategoryNotFoundException, ProductAlreadyExistsException, ProductServiceException;

    /**
     * Deletes a product by its ID.
     * Prevents deletion if the product is currently in use (e.g., in inventory items or sales orders).
     * @param productId The ID of the product to delete.
     * @throws ProductNotFoundException if the product is not found.
     * @throws ProductInUseException if the product is in use.
     * @throws ProductServiceException for other service-level errors.
     */
    void deleteProduct(int productId)
        throws ProductNotFoundException, ProductInUseException, ProductServiceException;

    /**
     * Retrieves a product by its primary database ID.
     * @param id The product's primary ID.
     * @return Optional of ProductDTO.
     */
    Optional<ProductDTO> getProductById(int id) throws ProductServiceException;

    /**
     * Retrieves a product by its unique product code.
     * @param code The product's code.
     * @return Optional of ProductDTO.
     */
    Optional<ProductDTO> getProductByCode(String code) throws ProductServiceException;

    /**
     * Retrieves all products.
     * @return List of all ProductDTOs.
     */
    List<ProductDTO> getAllProducts() throws ProductServiceException;

    /**
     * Retrieves all products belonging to a specific category.
     * @param categoryId The ID of the product category.
     * @return List of ProductDTOs.
     */
    List<ProductDTO> getProductsByCategory(int categoryId) throws ProductServiceException;

    /**
     * Searches for products by name (EN or AR) or code.
     * @param query The search query.
     * @return List of matching ProductDTOs.
     */
    List<ProductDTO> searchProducts(String query) throws ProductServiceException;
}
