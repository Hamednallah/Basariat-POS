package com.basariatpos.service;

import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.repository.ProductRepository;
import com.basariatpos.service.exception.*; // Import all custom exceptions

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
class ProductServiceImplTest {

    @Mock private ProductRepository mockProductRepository;
    @Mock private ProductCategoryService mockProductCategoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTO testProductDto;
    private ProductCategoryDTO testCategoryDto;

    @BeforeEach
    void setUp() {
        testCategoryDto = new ProductCategoryDTO(1, "Frames", "إطارات");
        testProductDto = new ProductDTO(1, "P001", "Sunglasses", "نظارات شمسية", 1,
                                       "Frames", "إطارات", "Ray-Ban Wayfarer", "وصف", false, true);
    }

    @Test
    void constructor_nullProductRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ProductServiceImpl(null, mockProductCategoryService));
    }
    @Test
    void constructor_nullProductCategoryService_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ProductServiceImpl(mockProductRepository, null));
    }

    // --- saveProduct ---
    @Test
    void saveProduct_newValidProduct_returnsSavedDto() throws Exception {
        ProductDTO newProductInput = new ProductDTO("P002", "Lenses Cleaner", "منظف عدسات", 1,
                                                  "Cleaner for all types", "وصف", true, false); // isService=true, isStockItem=false

        when(mockProductCategoryService.getProductCategoryById(1)).thenReturn(Optional.of(testCategoryDto));
        when(mockProductRepository.findByCode("P002")).thenReturn(Optional.empty());
        when(mockProductRepository.save(any(ProductDTO.class))).thenAnswer(inv -> {
            ProductDTO arg = inv.getArgument(0);
            arg.setProductId(2); // Simulate ID generation
            return arg;
        });

        ProductDTO result = productService.saveProduct(newProductInput);

        assertNotNull(result);
        assertEquals(2, result.getProductId());
        assertEquals("P002", result.getProductCode());
        assertTrue(result.isService());
        assertFalse(result.isStockItem()); // Check DTO logic for service implies not stock item
        verify(mockProductRepository).save(newProductInput);
    }

    @Test
    void saveProduct_productCodeExists_throwsProductAlreadyExistsException() throws Exception {
        ProductDTO newProductInput = new ProductDTO("P001", "Another Sunglasses", "نظارات أخرى", 1, null, null, false, true);
        when(mockProductCategoryService.getProductCategoryById(1)).thenReturn(Optional.of(testCategoryDto));
        when(mockProductRepository.findByCode("P001")).thenReturn(Optional.of(testProductDto)); // Existing product with same code

        assertThrows(ProductAlreadyExistsException.class, () -> {
            productService.saveProduct(newProductInput);
        });
    }

    @Test
    void saveProduct_invalidCategory_throwsCategoryNotFoundException() throws Exception {
        ProductDTO newProductInput = new ProductDTO("P003", "Test Prod", "منتج", 99, null, null, false, true);
        when(mockProductCategoryService.getProductCategoryById(99)).thenReturn(Optional.empty()); // Category not found

        assertThrows(CategoryNotFoundException.class, () -> {
            productService.saveProduct(newProductInput);
        });
    }

    @Test
    void saveProduct_emptyProductNameEn_throwsProductValidationException() {
        ProductDTO invalidProduct = new ProductDTO("P004", "", "اسم عربي", 1, null, null, false, true);
        assertThrows(ProductValidationException.class, () -> {
            productService.saveProduct(invalidProduct);
        });
    }


    // --- deleteProduct ---
    @Test
    void deleteProduct_existsAndNotInUse_deletesSuccessfully() throws Exception {
        when(mockProductRepository.findById(1)).thenReturn(Optional.of(testProductDto));
        when(mockProductRepository.isProductInUse(1)).thenReturn(false);
        doNothing().when(mockProductRepository).deleteById(1);

        productService.deleteProduct(1);
        verify(mockProductRepository).deleteById(1);
    }

    @Test
    void deleteProduct_productNotFound_throwsProductNotFoundException() {
        when(mockProductRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> {
            productService.deleteProduct(99);
        });
    }

    @Test
    void deleteProduct_productInUse_throwsProductInUseException() {
        when(mockProductRepository.findById(1)).thenReturn(Optional.of(testProductDto));
        when(mockProductRepository.isProductInUse(1)).thenReturn(true);

        assertThrows(ProductInUseException.class, () -> {
            productService.deleteProduct(1);
        });
        verify(mockProductRepository, never()).deleteById(1);
    }

    // --- getProductById ---
    @Test
    void getProductById_exists_returnsDto() {
        when(mockProductRepository.findById(1)).thenReturn(Optional.of(testProductDto));
        Optional<ProductDTO> result = productService.getProductById(1);
        assertTrue(result.isPresent());
        assertEquals(testProductDto.getProductNameEn(), result.get().getProductNameEn());
    }

    // --- getAllProducts ---
    @Test
    void getAllProducts_callsRepositoryFindAll() {
        List<ProductDTO> list = new ArrayList<>();
        list.add(testProductDto);
        when(mockProductRepository.findAll()).thenReturn(list);

        List<ProductDTO> result = productService.getAllProducts();

        assertEquals(1, result.size());
        verify(mockProductRepository).findAll();
    }

    // Add tests for getProductByCode, getProductsByCategory, searchProducts
}
