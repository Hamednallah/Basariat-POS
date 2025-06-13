package com.basariatpos.repository;

import com.basariatpos.model.ProductDTO;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    /**
     * Finds a product by its auto-incremented primary ID.
     * Includes category names by joining with ProductCategories table.
     * @param id The product_id.
     * @return Optional of ProductDTO.
     */
    Optional<ProductDTO> findById(int id);

    /**
     * Finds a product by its unique product code (case-insensitive).
     * Includes category names by joining with ProductCategories table.
     * @param code The product_code.
     * @return Optional of ProductDTO.
     */
    Optional<ProductDTO> findByCode(String code);

    /**
     * Retrieves all products. Includes category names.
     * Consider pagination for large datasets.
     * @return List of all ProductDTOs.
     */
    List<ProductDTO> findAll();

    /**
     * Retrieves all products belonging to a specific category ID. Includes category names.
     * @param categoryId The ID of the product category.
     * @return List of ProductDTOs.
     */
    List<ProductDTO> findByCategoryId(int categoryId);

    /**
     * Searches for products by name (EN or AR) or code.
     * @param query The search query.
     * @return List of matching ProductDTOs.
     */
    List<ProductDTO> searchProducts(String query);


    /**
     * Saves (inserts or updates) a product.
     * If productDto.getProductId() is 0, it's an insert.
     * Otherwise, it's an update.
     * @param productDto The ProductDTO to save. The categoryId field must be valid.
     * @return The saved ProductDTO, updated with a generated ID if it was an insert.
     */
    ProductDTO save(ProductDTO productDto);

    /**
     * Deletes a product by its ID.
     * This is a hard delete. Check isProductInUse before calling.
     * @param id The product_id to delete.
     */
    void deleteById(int id);

    /**
     * Checks if the product is currently referenced in any InventoryItems.
     * @param productId The ID of the product to check.
     * @return true if the product is in use, false otherwise.
     */
    boolean isProductInUse(int productId);
}
