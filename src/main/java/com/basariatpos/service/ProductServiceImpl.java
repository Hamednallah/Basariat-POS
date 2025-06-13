package com.basariatpos.service;

import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.repository.ProductRepository;
import com.basariatpos.service.exception.ProductAlreadyExistsException;
import com.basariatpos.service.exception.ProductInUseException;
import com.basariatpos.service.exception.ProductNotFoundException;
import com.basariatpos.service.exception.ProductServiceException;
import com.basariatpos.service.exception.ProductValidationException;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {

    private static final Logger logger = AppLogger.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final ProductCategoryService productCategoryService; // To validate categoryId

    public ProductServiceImpl(ProductRepository productRepository, ProductCategoryService productCategoryService) {
        if (productRepository == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null.");
        }
        if (productCategoryService == null) {
            throw new IllegalArgumentException("ProductCategoryService cannot be null.");
        }
        this.productRepository = productRepository;
        this.productCategoryService = productCategoryService;
    }

    @Override
    public ProductDTO saveProduct(ProductDTO productDto)
        throws ProductValidationException, CategoryNotFoundException, ProductAlreadyExistsException, ProductServiceException {

        validateProductDto(productDto);

        try {
            // Validate categoryId exists
            productCategoryService.getProductCategoryById(productDto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(productDto.getCategoryId()));

            // Check for product code uniqueness if it's a new product or if code is being changed
            // For simplicity, checking uniqueness on code always if provided and not blank.
            // A more nuanced check might be needed if product codes can be changed.
            if (productDto.getProductCode() != null && !productDto.getProductCode().trim().isEmpty()) {
                Optional<ProductDTO> existingByCode = productRepository.findByCode(productDto.getProductCode());
                if (existingByCode.isPresent() && (productDto.getProductId() == 0 || productDto.getProductId() != existingByCode.get().getProductId())) {
                    throw new ProductAlreadyExistsException(productDto.getProductCode());
                }
            }

            return productRepository.save(productDto);
        } catch (CategoryNotFoundException | ProductAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error saving product '{}': {}", productDto.getProductNameEn(), e.getMessage(), e);
            throw new ProductServiceException("Could not save product.", e);
        }
    }

    @Override
    public void deleteProduct(int productId)
        throws ProductNotFoundException, ProductInUseException, ProductServiceException {
        try {
            productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

            if (productRepository.isProductInUse(productId)) {
                throw new ProductInUseException(productId);
            }
            productRepository.deleteById(productId);
            logger.info("Product with ID {} deleted successfully.", productId);
        } catch (ProductNotFoundException | ProductInUseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting product ID '{}': {}", productId, e.getMessage(), e);
            throw new ProductServiceException("Could not delete product.", e);
        }
    }

    @Override
    public Optional<ProductDTO> getProductById(int id) throws ProductServiceException {
        try {
            return productRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving product by ID {}: {}", id, e.getMessage(), e);
            throw new ProductServiceException("Error retrieving product by ID.", e);
        }
    }

    @Override
    public Optional<ProductDTO> getProductByCode(String code) throws ProductServiceException {
        if (code == null || code.trim().isEmpty()) {
            throw new ProductValidationException("Product code cannot be empty for lookup.", List.of("Product code required."));
        }
        try {
            return productRepository.findByCode(code);
        } catch (Exception e) {
            logger.error("Error retrieving product by code '{}': {}", code, e.getMessage(), e);
            throw new ProductServiceException("Error retrieving product by code.", e);
        }
    }

    @Override
    public List<ProductDTO> getAllProducts() throws ProductServiceException {
        try {
            return productRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all products: {}", e.getMessage(), e);
            throw new ProductServiceException("Error retrieving all products.", e);
        }
    }

    @Override
    public List<ProductDTO> getProductsByCategory(int categoryId) throws ProductServiceException {
        try {
            return productRepository.findByCategoryId(categoryId);
        } catch (Exception e) {
            logger.error("Error retrieving products for category ID {}: {}", categoryId, e.getMessage(), e);
            throw new ProductServiceException("Error retrieving products by category.", e);
        }
    }

    @Override
    public List<ProductDTO> searchProducts(String query) throws ProductServiceException {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts(); // Or return empty list based on desired behavior for empty search
        }
        try {
            return productRepository.searchProducts(query);
        } catch (Exception e) {
            logger.error("Error searching products with query '{}': {}", query, e.getMessage(), e);
            throw new ProductServiceException("Error during product search.", e);
        }
    }


    private void validateProductDto(ProductDTO dto) throws ProductValidationException {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("Product data cannot be null.");
            throw new ProductValidationException("Product data is null.", errors);
        }
        if (dto.getProductNameEn() == null || dto.getProductNameEn().trim().isEmpty()) {
            errors.add("English product name is required.");
        }
        if (dto.getProductNameAr() == null || dto.getProductNameAr().trim().isEmpty()) {
            errors.add("Arabic product name is required.");
        }
        if (dto.getProductCode() == null || dto.getProductCode().trim().isEmpty()) {
            errors.add("Product code is required.");
        }
        if (dto.getCategoryId() <= 0) {
            errors.add("A valid product category must be selected.");
        }
        // If isService is false, isStockItem can be true or false.
        // If isService is true, isStockItem must be false. (Handled in DTO setter)
        if (dto.isService() && dto.isStockItem()) {
             errors.add("A service cannot be a stock item. Please uncheck 'Is Stock Item' or 'Is Service'.");
        }


        if (!errors.isEmpty()) {
            throw new ProductValidationException("Product data validation failed.", errors);
        }
    }
}
