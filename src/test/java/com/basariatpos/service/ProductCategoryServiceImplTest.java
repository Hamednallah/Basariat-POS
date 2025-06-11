package com.basariatpos.service;

import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.repository.ProductCategoryRepository;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {

    @Mock
    private ProductCategoryRepository mockCategoryRepository;

    @InjectMocks
    private ProductCategoryServiceImpl categoryService;

    private ProductCategoryDTO testCategoryDto;

    @BeforeEach
    void setUp() {
        testCategoryDto = new ProductCategoryDTO(1, "Sunglasses", "نظارات شمسية");
    }

    @Test
    void constructor_nullRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ProductCategoryServiceImpl(null));
    }

    // --- saveProductCategory ---
    @Test
    void saveProductCategory_newValidCategory_returnsSavedDto() throws Exception {
        ProductCategoryDTO newCategory = new ProductCategoryDTO("Lenses EN", "عدسات AR");
        ProductCategoryDTO savedCategory = new ProductCategoryDTO(100, "Lenses EN", "عدسات AR");

        when(mockCategoryRepository.findByNameEn("Lenses EN")).thenReturn(Optional.empty());
        when(mockCategoryRepository.findByNameAr("عدسات AR")).thenReturn(Optional.empty());
        when(mockCategoryRepository.save(any(ProductCategoryDTO.class))).thenReturn(savedCategory);

        ProductCategoryDTO result = categoryService.saveProductCategory(newCategory);

        assertNotNull(result);
        assertEquals(100, result.getCategoryId());
        verify(mockCategoryRepository).save(newCategory);
    }

    @Test
    void saveProductCategory_englishNameExists_throwsCategoryAlreadyExistsException() {
        ProductCategoryDTO newCategory = new ProductCategoryDTO("Sunglasses", "فريد AR"); // English name same as testCategoryDto
        when(mockCategoryRepository.findByNameEn("Sunglasses")).thenReturn(Optional.of(testCategoryDto));

        assertThrows(CategoryAlreadyExistsException.class, () -> {
            categoryService.saveProductCategory(newCategory);
        });
    }

    @Test
    void saveProductCategory_emptyEnglishName_throwsValidationException() {
        ProductCategoryDTO invalidCategory = new ProductCategoryDTO("", "صالح AR");
        // Assuming ValidationException is from com.basariatpos.service package
        assertThrows(com.basariatpos.service.ValidationException.class, () -> {
            categoryService.saveProductCategory(invalidCategory);
        });
    }


    // --- deleteProductCategory ---
    @Test
    void deleteProductCategory_categoryExistsAndNotInUse_deletesSuccessfully() throws Exception {
        when(mockCategoryRepository.findById(1)).thenReturn(Optional.of(testCategoryDto));
        when(mockCategoryRepository.isCategoryInUse(1)).thenReturn(false); // Not in use
        doNothing().when(mockCategoryRepository).deleteById(1);

        categoryService.deleteProductCategory(1);
        verify(mockCategoryRepository).deleteById(1);
    }

    @Test
    void deleteProductCategory_categoryNotFound_throwsCategoryNotFoundException() {
        when(mockCategoryRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.deleteProductCategory(99);
        });
    }

    @Test
    void deleteProductCategory_categoryInUse_throwsCategoryInUseException() {
        when(mockCategoryRepository.findById(1)).thenReturn(Optional.of(testCategoryDto));
        when(mockCategoryRepository.isCategoryInUse(1)).thenReturn(true); // Is in use

        assertThrows(CategoryInUseException.class, () -> {
            categoryService.deleteProductCategory(1);
        });
        verify(mockCategoryRepository, never()).deleteById(1);
    }

    // --- getProductCategoryById ---
    @Test
    void getProductCategoryById_exists_returnsDto() {
        when(mockCategoryRepository.findById(1)).thenReturn(Optional.of(testCategoryDto));
        Optional<ProductCategoryDTO> result = categoryService.getProductCategoryById(1);
        assertTrue(result.isPresent());
        assertEquals(testCategoryDto.getCategoryNameEn(), result.get().getCategoryNameEn());
    }

    // --- getAllProductCategories ---
    @Test
    void getAllProductCategories_callsRepositoryFindAll() {
        List<ProductCategoryDTO> list = new ArrayList<>();
        list.add(testCategoryDto);
        when(mockCategoryRepository.findAll()).thenReturn(list);

        List<ProductCategoryDTO> result = categoryService.getAllProductCategories();

        assertEquals(1, result.size());
        verify(mockCategoryRepository).findAll();
    }
}
