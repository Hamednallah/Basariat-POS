package com.basariatpos.service;

import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.repository.ExpenseCategoryRepository;
import com.basariatpos.repository.ExpenseCategoryRepositoryImpl; // For protected category names
import com.basariatpos.util.AppLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpenseCategoryServiceImpl implements ExpenseCategoryService {

    private static final Logger logger = AppLogger.getLogger(ExpenseCategoryServiceImpl.class);
    private final ExpenseCategoryRepository expenseCategoryRepository;

    // Define protected category names (should match values in ExpenseCategoryRepositoryImpl or a shared constants file)
    private static final String PROTECTED_CAT_LOSS_EN = ExpenseCategoryRepositoryImpl.LOSS_ON_ABANDONED_ORDERS_EN;
    private static final String PROTECTED_CAT_LOSS_AR = ExpenseCategoryRepositoryImpl.LOSS_ON_ABANDONED_ORDERS_AR;


    public ExpenseCategoryServiceImpl(ExpenseCategoryRepository expenseCategoryRepository) {
        if (expenseCategoryRepository == null) {
            throw new IllegalArgumentException("ExpenseCategoryRepository cannot be null.");
        }
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    @Override
    public Optional<ExpenseCategoryDTO> getExpenseCategoryById(int id) throws CategoryException {
        try {
            return expenseCategoryRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error getting expense category by ID {}: {}", id, e.getMessage(), e);
            throw new CategoryException("Could not retrieve expense category by ID.", e);
        }
    }

    @Override
    public List<ExpenseCategoryDTO> getAllExpenseCategories(boolean includeInactive) throws CategoryException {
        try {
            return expenseCategoryRepository.findAll(includeInactive);
        } catch (Exception e) {
            logger.error("Error getting all expense categories (includeInactive={}): {}", includeInactive, e.getMessage(), e);
            throw new CategoryException("Could not retrieve all expense categories.", e);
        }
    }

    @Override
    public List<ExpenseCategoryDTO> getActiveExpenseCategories() throws CategoryException {
        try {
            return expenseCategoryRepository.findAll(false); // false for active only
        } catch (Exception e) {
            logger.error("Error getting active expense categories: {}", e.getMessage(), e);
            throw new CategoryException("Could not retrieve active expense categories.", e);
        }
    }

    @Override
    public ExpenseCategoryDTO saveExpenseCategory(ExpenseCategoryDTO categoryDto) throws ValidationException, CategoryAlreadyExistsException, CategoryException {
        validateCategoryDto(categoryDto);

        try {
            Optional<ExpenseCategoryDTO> existingById = Optional.empty();
            if(categoryDto.getExpenseCategoryId() > 0) {
                existingById = expenseCategoryRepository.findById(categoryDto.getExpenseCategoryId());
            }

            // Prevent name change for protected category "Loss on Abandoned Orders"
            if (existingById.isPresent() && isProtectedCategory(existingById.get())) {
                if (!existingById.get().getCategoryNameEn().equals(categoryDto.getCategoryNameEn()) ||
                    !existingById.get().getCategoryNameAr().equals(categoryDto.getCategoryNameAr())) {
                    throw new CategoryException("Cannot change the name of the protected category: " + existingById.get().getCategoryNameEn());
                }
            }

            // Check for name uniqueness (English)
            Optional<ExpenseCategoryDTO> existingByNameEn = expenseCategoryRepository.findByNameEn(categoryDto.getCategoryNameEn());
            if (existingByNameEn.isPresent() && (categoryDto.getExpenseCategoryId() == 0 || categoryDto.getExpenseCategoryId() != existingByNameEn.get().getExpenseCategoryId())) {
                throw new CategoryAlreadyExistsException(categoryDto.getCategoryNameEn() + " (English)");
            }
            // Check for name uniqueness (Arabic)
            Optional<ExpenseCategoryDTO> existingByNameAr = expenseCategoryRepository.findByNameAr(categoryDto.getCategoryNameAr());
            if (existingByNameAr.isPresent() && (categoryDto.getExpenseCategoryId() == 0 || categoryDto.getExpenseCategoryId() != existingByNameAr.get().getExpenseCategoryId())) {
                throw new CategoryAlreadyExistsException(categoryDto.getCategoryNameAr() + " (Arabic)");
            }

            return expenseCategoryRepository.save(categoryDto);
        } catch (CategoryAlreadyExistsException | CategoryException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Error saving expense category '{}': {}", categoryDto.getCategoryNameEn(), e.getMessage(), e);
            throw new CategoryException("Could not save expense category.", e);
        }
    }

    @Override
    public void toggleExpenseCategoryStatus(int id) throws CategoryNotFoundException, CategoryInUseException, CategoryException {
        try {
            ExpenseCategoryDTO category = expenseCategoryRepository.findById(id)
                                           .orElseThrow(() -> new CategoryNotFoundException(id));

            // Prevent deactivation of "Loss on Abandoned Orders"
            if (isProtectedCategory(category) && category.isActive()) {
                throw new CategoryException("Cannot deactivate the protected category: " + category.getCategoryNameEn());
            }

            // Prevent deactivation if category is in use (only if attempting to deactivate)
            if (category.isActive() && expenseCategoryRepository.isCategoryInUse(id)) {
                throw new CategoryInUseException(id);
            }

            expenseCategoryRepository.setActiveStatus(id, !category.isActive());
            logger.info("Toggled active status for expense category ID {}. New status: {}", id, !category.isActive());
        } catch (CategoryNotFoundException | CategoryInUseException | CategoryException ce) {
            throw ce;
        }
        catch (Exception e) {
            logger.error("Error toggling status for expense category ID {}: {}", id, e.getMessage(), e);
            throw new CategoryException("Could not toggle expense category status.", e);
        }
    }

    @Override
    public boolean isProtectedCategory(ExpenseCategoryDTO categoryDto) {
        if (categoryDto == null) return false;
        return PROTECTED_CAT_LOSS_EN.equalsIgnoreCase(categoryDto.getCategoryNameEn()) ||
               PROTECTED_CAT_LOSS_AR.equals(categoryDto.getCategoryNameAr());
    }


    private void validateCategoryDto(ExpenseCategoryDTO categoryDto) throws ValidationException {
        List<String> errors = new ArrayList<>();
        if (categoryDto == null) {
            errors.add("Expense category data cannot be null.");
            // Using ValidationException from UserService for now.
            throw new com.basariatpos.service.ValidationException("Expense category data is null.", errors);
        }
        if (categoryDto.getCategoryNameEn() == null || categoryDto.getCategoryNameEn().trim().isEmpty()) {
            errors.add(MessageProvider.getString("expensecategory.validation.nameEn.required")); // Placeholder, assumes MessageProvider access
        }
        if (categoryDto.getCategoryNameAr() == null || categoryDto.getCategoryNameAr().trim().isEmpty()) {
            errors.add(MessageProvider.getString("expensecategory.validation.nameAr.required"));
        }

        if (!errors.isEmpty()) {
            throw new com.basariatpos.service.ValidationException("Expense category validation failed.", errors);
        }
    }
}
