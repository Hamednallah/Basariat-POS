package com.basariatpos.service;

import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.ProductDTO; // For mocking ProductService
import com.basariatpos.repository.InventoryItemRepository;
import com.basariatpos.service.exception.InventoryItemNotFoundException;
import com.basariatpos.service.exception.InventoryItemValidationException;
import com.basariatpos.service.exception.ProductNotFoundException; // From ProductService

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceImplTest {

    @Mock private InventoryItemRepository mockItemRepository;
    @Mock private ProductService mockProductService;
    @Mock private AuditLogRepository mockAuditLogRepository;
    @Mock private UserSessionService mockUserSessionService;

    @InjectMocks
    private InventoryItemServiceImpl itemService;

    private InventoryItemDTO testItemDto;
    private com.basariatpos.model.UserDTO testUserDto; // For UserSessionService
    private ProductDTO testProductDto;

    @BeforeEach
    void setUp() {
        testProductDto = new ProductDTO(10, "P100", "Test Product", "منتج اختباري", 1, "Category", "فئة", null, null, false, true);

        testItemDto = new InventoryItemDTO();
        testItemDto.setInventoryItemId(1);
        testItemDto.setProductId(10);
        testItemDto.setProductNameEn("Test Product"); // Usually set by repo join, but can be set for test input
        testItemDto.setItemSpecificNameEn("Red, Large");
        testItemDto.setQuantityOnHand(50);
        testItemDto.setSellingPrice(new BigDecimal("19.99"));
        testItemDto.setActive(true);
        testItemDto.setUnitOfMeasure("pcs");
    }

    @Test
    void constructor_nullItemRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryItemServiceImpl(null, mockProductService, mockAuditLogRepository, mockUserSessionService));
    }
    @Test
    void constructor_nullProductService_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryItemServiceImpl(mockItemRepository, null, mockAuditLogRepository, mockUserSessionService));
    }
    @Test
    void constructor_nullAuditLogRepository_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryItemServiceImpl(mockItemRepository, mockProductService, null, mockUserSessionService));
    }
    @Test
    void constructor_nullUserSessionService_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryItemServiceImpl(mockItemRepository, mockProductService, mockAuditLogRepository, null));
    }


    // --- saveInventoryItem ---
    @Test
    void saveInventoryItem_validNewItem_returnsSavedDto() throws Exception {
        InventoryItemDTO newItem = new InventoryItemDTO();
        newItem.setProductId(10);
        newItem.setItemSpecificNameEn("Blue, Small");
        newItem.setSellingPrice(new BigDecimal("25.50"));
        newItem.setUnitOfMeasure("pcs");
        // Assume other fields like QOH, MinStock are defaulted or set

        when(mockProductService.getProductById(10)).thenReturn(Optional.of(testProductDto)); // Product exists
        when(mockItemRepository.save(any(InventoryItemDTO.class))).thenAnswer(inv -> {
            InventoryItemDTO arg = inv.getArgument(0);
            arg.setInventoryItemId(101); // Simulate ID generation by repo
            return arg;
        });

        InventoryItemDTO result = itemService.saveInventoryItem(newItem);

        assertNotNull(result);
        assertEquals(101, result.getInventoryItemId());
        assertEquals("Blue, Small", result.getItemSpecificNameEn());
        verify(mockItemRepository).save(newItem);
    }

    @Test
    void saveInventoryItem_productIdNotExists_throwsProductNotFoundException() throws Exception {
        InventoryItemDTO newItem = new InventoryItemDTO();
        newItem.setProductId(99); // Non-existent product
        newItem.setItemSpecificNameEn("Test");
        newItem.setSellingPrice(BigDecimal.TEN);
        newItem.setUnitOfMeasure("pcs");

        when(mockProductService.getProductById(99)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            itemService.saveInventoryItem(newItem);
        });
        verify(mockItemRepository, never()).save(any());
    }

    @Test
    void saveInventoryItem_invalidDto_throwsInventoryItemValidationException() {
        InventoryItemDTO invalidItem = new InventoryItemDTO();
        invalidItem.setProductId(10);
        // itemSpecificNameEn is null/empty - should fail validation
        invalidItem.setSellingPrice(new BigDecimal("-5.00")); // Invalid price
        invalidItem.setUnitOfMeasure("");

        when(mockProductService.getProductById(10)).thenReturn(Optional.of(testProductDto)); // Product exists

        InventoryItemValidationException ex = assertThrows(InventoryItemValidationException.class, () -> {
            itemService.saveInventoryItem(invalidItem);
        });
        assertTrue(ex.getErrors().size() >= 2); // Check for multiple errors
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Specific Name (English) is required")));
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Selling price must be a non-negative value")));
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Unit of measure is required")));
    }

    @Test
    void saveInventoryItem_invalidJsonInAttributes_throwsInventoryItemValidationException() {
        InventoryItemDTO itemWithInvalidJson = new InventoryItemDTO();
        itemWithInvalidJson.setProductId(10);
        itemWithInvalidJson.setItemSpecificNameEn("Valid Name");
        itemWithInvalidJson.setSellingPrice(BigDecimal.TEN);
        itemWithInvalidJson.setUnitOfMeasure("pcs");
        itemWithInvalidJson.setAttributes("{not:json}");

        when(mockProductService.getProductById(10)).thenReturn(Optional.of(testProductDto));

        InventoryItemValidationException ex = assertThrows(InventoryItemValidationException.class, () -> {
            itemService.saveInventoryItem(itemWithInvalidJson);
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Attributes field does not contain valid JSON")));
    }


    // --- toggleActiveStatus ---
    @Test
    void toggleActiveStatus_itemExists_togglesStatus() throws Exception {
        when(mockItemRepository.findById(1)).thenReturn(Optional.of(testItemDto)); // Initially active
        doNothing().when(mockItemRepository).setActiveStatus(1, false);

        itemService.toggleActiveStatus(1);
        verify(mockItemRepository).setActiveStatus(1, false);
    }

    @Test
    void toggleActiveStatus_itemNotFound_throwsInventoryItemNotFoundException() {
        when(mockItemRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(InventoryItemNotFoundException.class, () -> {
            itemService.toggleActiveStatus(99);
        });
    }

    // --- getLowStockItemsReport ---
    @Test
    void getLowStockItemsReport_callsRepository() {
        List<InventoryItemDTO> lowStockList = new ArrayList<>();
        lowStockList.add(testItemDto); // Assume this is a low stock item
        when(mockItemRepository.getLowStockItems()).thenReturn(lowStockList);

        List<InventoryItemDTO> result = itemService.getLowStockItemsReport();

        assertEquals(1, result.size());
        verify(mockItemRepository).getLowStockItems();
    }

    // Add tests for other getters: getInventoryItemById, getInventoryItemsByProduct, getAllInventoryItems, searchInventoryItems

    // --- performStockAdjustment ---

    @BeforeEach
    void setupUserDto() { // Separate setup for user DTO for relevant tests
        testUserDto = new com.basariatpos.model.UserDTO();
        testUserDto.setUserId(123);
        testUserDto.setUsername("testUser");
    }

    @Test
    void performStockAdjustment_success_logsAndAdjusts() throws Exception {
        when(mockItemRepository.findById(1)).thenReturn(Optional.of(testItemDto)); // Original QOH is 50
        when(mockUserSessionService.getCurrentUser()).thenReturn(testUserDto);
        when(mockItemRepository.adjustStockQuantity(1, 10)).thenReturn(true);

        itemService.performStockAdjustment(1, 10, "Test Reason");

        verify(mockItemRepository).adjustStockQuantity(1, 10);
        verify(mockAuditLogRepository).logStockAdjustment(
            eq(1),                  // inventoryItemId
            anyString(),            // itemName
            eq(10),                 // quantityChange
            eq(50),                 // oldQty
            eq(60),                 // newQty
            eq("Test Reason"),      // reason
            eq(123)                 // adjustedByUserId
        );
    }

    @Test
    void performStockAdjustment_itemNotFound_throwsInventoryItemNotFoundException() {
        when(mockItemRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(InventoryItemNotFoundException.class, () -> {
            itemService.performStockAdjustment(99, 5, "Test Reason");
        });
        verify(mockAuditLogRepository, never()).logStockAdjustment(anyInt(), anyString(), anyInt(), anyInt(), anyInt(), anyString(), anyInt());
    }

    @Test
    void performStockAdjustment_blankReason_throwsInventoryItemValidationException() {
        assertThrows(InventoryItemValidationException.class, () -> {
            itemService.performStockAdjustment(1, 5, "  ");
        });
         verify(mockItemRepository, never()).findById(anyInt());
    }

    @Test
    void performStockAdjustment_zeroQuantityChange_throwsInventoryItemValidationException() {
         assertThrows(InventoryItemValidationException.class, () -> {
            itemService.performStockAdjustment(1, 0, "No change");
        });
        verify(mockItemRepository, never()).findById(anyInt());
    }

    @Test
    void performStockAdjustment_qohBecomesNegative_throwsInventoryItemValidationException() {
        when(mockItemRepository.findById(1)).thenReturn(Optional.of(testItemDto)); // Original QOH is 50

        assertThrows(InventoryItemValidationException.class, () -> {
            itemService.performStockAdjustment(1, -60, "Too much decrease"); // 50 - 60 = -10
        });
        verify(mockAuditLogRepository, never()).logStockAdjustment(anyInt(), anyString(), anyInt(), anyInt(), anyInt(), anyString(), anyInt());
        verify(mockItemRepository, never()).adjustStockQuantity(anyInt(),anyInt());
    }

    @Test
    void performStockAdjustment_repositoryAdjustFails_throwsInventoryItemServiceExceptionAndNoLog() {
        when(mockItemRepository.findById(1)).thenReturn(Optional.of(testItemDto));
        when(mockUserSessionService.getCurrentUser()).thenReturn(testUserDto);
        when(mockItemRepository.adjustStockQuantity(1, 5)).thenReturn(false); // Simulate repo update failure

        assertThrows(InventoryItemServiceException.class, () -> {
            itemService.performStockAdjustment(1, 5, "Test Reason");
        });
        verify(mockAuditLogRepository, never()).logStockAdjustment(anyInt(), anyString(), anyInt(), anyInt(), anyInt(), anyString(), anyInt());
    }

    @Test
    void performStockAdjustment_auditLogUserIdNull_whenNoCurrentUser() throws Exception {
        when(mockItemRepository.findById(1)).thenReturn(Optional.of(testItemDto));
        when(mockUserSessionService.getCurrentUser()).thenReturn(null); // No user logged in
        when(mockItemRepository.adjustStockQuantity(1, -5)).thenReturn(true);

        itemService.performStockAdjustment(1, -5, "Adjustment by system/unknown");

        verify(mockAuditLogRepository).logStockAdjustment(
            eq(1),
            anyString(),
            eq(-5),
            eq(50),
            eq(45),
            eq("Adjustment by system/unknown"),
            isNull() // Check that userId is null
        );
    }
}
