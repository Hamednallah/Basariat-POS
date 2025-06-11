package com.basariatpos.service;

import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.repository.ExpenseCategoryRepository;
import com.basariatpos.repository.ExpenseCategoryRepositoryImpl; // For protected names

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryServiceImplTest {

    @Mock
    private ExpenseCategoryRepository mockCategoryRepository;

    @InjectMocks
    private ExpenseCategoryServiceImpl categoryService;

    private ExpenseCategoryDTO testCategoryDto;
    private ExpenseCategoryDTO protectedCategoryDto;

    @BeforeEach
    void setUp() {
        testCategoryDto = new ExpenseCategoryDTO(1, "Groceries", "بقالة", true);
        protectedCategoryDto = new ExpenseCategoryDTO(2,
            ExpenseCategoryRepositoryImpl.LOSS_ON_ABANDONED_ORDERS_EN,
            ExpenseCategoryRepositoryImpl.LOSS_ON_ABANDONED_ORDERS_AR,
            true);
    }

    @Test
    void constructor_nullRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ExpenseCategoryServiceImpl(null));
    }

    // --- saveExpenseCategory ---
    @Test
    void saveExpenseCategory_newValidCategory_returnsSavedDto() throws Exception {
        ExpenseCategoryDTO newCategory = new ExpenseCategoryDTO("Food EN", "طعام AR", true);
        ExpenseCategoryDTO savedCategory = new ExpenseCategoryDTO(100, "Food EN", "طعام AR", true);

        when(mockCategoryRepository.findByNameEn("Food EN")).thenReturn(Optional.empty());
        when(mockCategoryRepository.findByNameAr("طعام AR")).thenReturn(Optional.empty());
        when(mockCategoryRepository.save(any(ExpenseCategoryDTO.class))).thenReturn(savedCategory);

        ExpenseCategoryDTO result = categoryService.saveExpenseCategory(newCategory);

        assertNotNull(result);
        assertEquals(100, result.getExpenseCategoryId());
        verify(mockCategoryRepository).save(newCategory);
    }

    @Test
    void saveExpenseCategory_englishNameExists_throwsCategoryAlreadyExistsException() {
        ExpenseCategoryDTO newCategory = new ExpenseCategoryDTO("Groceries", "جديد AR", true);
        when(mockCategoryRepository.findByNameEn("Groceries")).thenReturn(Optional.of(testCategoryDto));

        assertThrows(CategoryAlreadyExistsException.class, () -> {
            categoryService.saveExpenseCategory(newCategory);
        });
    }

    @Test
    void saveExpenseCategory_protectedCategoryNameChangeAttempt_throwsCategoryException() {
        // Try to save the protected category with a new name (even if ID is same)
        ExpenseCategoryDTO changedProtected = new ExpenseCategoryDTO(protectedCategoryDto.getExpenseCategoryId(),
                                                                  "New Name For Protected",
                                                                  protectedCategoryDto.getCategoryNameAr(), true);
        when(mockCategoryRepository.findById(protectedCategoryDto.getExpenseCategoryId())).thenReturn(Optional.of(protectedCategoryDto));

        CategoryException ex = assertThrows(CategoryException.class, () -> {
            categoryService.saveExpenseCategory(changedProtected);
        });
        assertTrue(ex.getMessage().contains("Cannot change the name of the protected category"));
    }


    // --- toggleExpenseCategoryStatus ---
    @Test
    void toggleExpenseCategoryStatus_activeToInactive_success() throws Exception {
        when(mockCategoryRepository.findById(1)).thenReturn(Optional.of(testCategoryDto)); // Active
        when(mockCategoryRepository.isCategoryInUse(1)).thenReturn(false); // Not in use
        doNothing().when(mockCategoryRepository).setActiveStatus(1, false);

        categoryService.toggleExpenseCategoryStatus(1);
        verify(mockCategoryRepository).setActiveStatus(1, false);
    }

    @Test
    void toggleExpenseCategoryStatus_inactiveToActive_success() throws Exception {
        testCategoryDto.setActive(false); // Make it inactive
        when(mockCategoryRepository.findById(1)).thenReturn(Optional.of(testCategoryDto));
        // isCategoryInUse is not checked when activating
        doNothing().when(mockCategoryRepository).setActiveStatus(1, true);

        categoryService.toggleExpenseCategoryStatus(1);
        verify(mockCategoryRepository).setActiveStatus(1, true);
    }

    @Test
    void toggleExpenseCategoryStatus_categoryInUse_throwsCategoryInUseException() {
        when(mockCategoryRepository.findById(1)).thenReturn(Optional.of(testCategoryDto)); // Active
        when(mockCategoryRepository.isCategoryInUse(1)).thenReturn(true); // Is in use

        assertThrows(CategoryInUseException.class, () -> {
            categoryService.toggleExpenseCategoryStatus(1);
        });
        verify(mockCategoryRepository, never()).setActiveStatus(anyInt(), anyBoolean());
    }

    @Test
    void toggleExpenseCategoryStatus_protectedCategoryDeactivationAttempt_throwsCategoryException() {
        when(mockCategoryRepository.findById(protectedCategoryDto.getExpenseCategoryId())).thenReturn(Optional.of(protectedCategoryDto));
        // isProtectedCategory is true, isActive is true

        CategoryException ex = assertThrows(CategoryException.class, () -> {
            categoryService.toggleExpenseCategoryStatus(protectedCategoryDto.getExpenseCategoryId());
        });
        assertTrue(ex.getMessage().contains("Cannot deactivate the protected category"));
        verify(mockCategoryRepository, never()).setActiveStatus(anyInt(), anyBoolean());
    }

    @Test
    void toggleExpenseCategoryStatus_protectedCategoryActivation_isAllowed() throws Exception {
        protectedCategoryDto.setActive(false); // if it was somehow made inactive
        when(mockCategoryRepository.findById(protectedCategoryDto.getExpenseCategoryId())).thenReturn(Optional.of(protectedCategoryDto));
        doNothing().when(mockCategoryRepository).setActiveStatus(protectedCategoryDto.getExpenseCategoryId(), true);

        categoryService.toggleExpenseCategoryStatus(protectedCategoryDto.getExpenseCategoryId());

        verify(mockCategoryRepository).setActiveStatus(protectedCategoryDto.getExpenseCategoryId(), true);
    }


    @Test
    void toggleExpenseCategoryStatus_categoryNotFound_throwsCategoryNotFoundException() {
        when(mockCategoryRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.toggleExpenseCategoryStatus(99);
        });
    }

    // --- isProtectedCategory ---
    @Test
    void isProtectedCategory_protectedNameEn_returnsTrue() {
        ExpenseCategoryDTO dto = new ExpenseCategoryDTO("Loss on Abandoned Orders", "عادي", true);
        assertTrue(categoryService.isProtectedCategory(dto));
    }

    @Test
    void isProtectedCategory_protectedNameAr_returnsTrue() {
        ExpenseCategoryDTO dto = new ExpenseCategoryDTO("Normal", "خسارة الطلبات الملغاة", true);
        assertTrue(categoryService.isProtectedCategory(dto));
    }

    @Test
    void isProtectedCategory_normalName_returnsFalse() {
         ExpenseCategoryDTO dto = new ExpenseCategoryDTO("Normal Cat", "عادي", true);
        assertFalse(categoryService.isProtectedCategory(dto));
    }

    @Test
    void isProtectedCategory_caseInsensitiveEn_returnsTrue() {
        ExpenseCategoryDTO dto = new ExpenseCategoryDTO("loss on abandoned orders", "عادي", true);
        assertTrue(categoryService.isProtectedCategory(dto));
    }


    // Add tests for: getExpenseCategoryById, getAllExpenseCategories, getActiveExpenseCategories
    // ValidationException cases for saveExpenseCategory
}
