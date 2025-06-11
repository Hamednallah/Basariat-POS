package com.basariatpos.service;

import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.repository.ProductCategoryRepository;
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductCategoryServiceImpl implements ProductCategoryService {

    private static final Logger logger = AppLogger.getLogger(ProductCategoryServiceImpl.class);
    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryServiceImpl(ProductCategoryRepository productCategoryRepository) {
        if (productCategoryRepository == null) {
            throw new IllegalArgumentException("ProductCategoryRepository cannot be null.");
        }
        this.productCategoryRepository = productCategoryRepository;
    }

    @Override
    public Optional<ProductCategoryDTO> getProductCategoryById(int id) throws CategoryException {
        try {
            return productCategoryRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error getting product category by ID {}: {}", id, e.getMessage(), e);
            throw new CategoryException("Could not retrieve product category by ID.", e);
        }
    }

    @Override
    public List<ProductCategoryDTO> getAllProductCategories() throws CategoryException {
        try {
            return productCategoryRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting all product categories: {}", e.getMessage(), e);
            throw new CategoryException("Could not retrieve all product categories.", e);
        }
    }

    @Override
    public ProductCategoryDTO saveProductCategory(ProductCategoryDTO categoryDto) throws ValidationException, CategoryAlreadyExistsException, CategoryException {
        validateCategoryDto(categoryDto);

        try {
            // Check for uniqueness of English name (case-insensitive)
            Optional<ProductCategoryDTO> existingByNameEn = productCategoryRepository.findByNameEn(categoryDto.getCategoryNameEn());
            if (existingByNameEn.isPresent() && (categoryDto.getCategoryId() == 0 || categoryDto.getCategoryId() != existingByNameEn.get().getCategoryId())) {
                throw new CategoryAlreadyExistsException(categoryDto.getCategoryNameEn() + " (English)");
            }
            // Check for uniqueness of Arabic name
            Optional<ProductCategoryDTO> existingByNameAr = productCategoryRepository.findByNameAr(categoryDto.getCategoryNameAr());
            if (existingByNameAr.isPresent() && (categoryDto.getCategoryId() == 0 || categoryDto.getCategoryId() != existingByNameAr.get().getCategoryId())) {
                throw new CategoryAlreadyExistsException(categoryDto.getCategoryNameAr() + " (Arabic)");
            }

            return productCategoryRepository.save(categoryDto);
        } catch (CategoryAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error saving product category '{}': {}", categoryDto.getCategoryNameEn(), e.getMessage(), e);
            throw new CategoryException("Could not save product category.", e);
        }
    }

    @Override
    public void deleteProductCategory(int id) throws CategoryNotFoundException, CategoryInUseException, CategoryException {
        try {
            // Check if category exists
            productCategoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

            // Check if category is in use
            if (productCategoryRepository.isCategoryInUse(id)) {
                throw new CategoryInUseException(id);
            }

            productCategoryRepository.deleteById(id);
            logger.info("Product category with ID {} deleted successfully.", id);
        } catch (CategoryNotFoundException | CategoryInUseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting product category with ID {}: {}", id, e.getMessage(), e);
            throw new CategoryException("Could not delete product category.", e);
        }
    }

    private void validateCategoryDto(ProductCategoryDTO categoryDto) throws ValidationException {
        List<String> errors = new ArrayList<>();
        if (categoryDto == null) {
            errors.add("Product category data cannot be null.");
            // Assuming ValidationException is available (e.g. from com.basariatpos.service package)
            throw new com.basariatpos.service.ValidationException("Product category data is null.", errors);
        }
        if (categoryDto.getCategoryNameEn() == null || categoryDto.getCategoryNameEn().trim().isEmpty()) {
            // These keys would need to be in properties files and MessageProvider used for i18n in real validation messages
            errors.add("English category name is required.");
        }
        if (categoryDto.getCategoryNameAr() == null || categoryDto.getCategoryNameAr().trim().isEmpty()) {
            errors.add("Arabic category name is required.");
        }

        if (!errors.isEmpty()) {
            throw new com.basariatpos.service.ValidationException("Product category validation failed.", errors);
        }
    }
}
