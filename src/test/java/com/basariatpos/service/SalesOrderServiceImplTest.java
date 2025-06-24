package com.basariatpos.service;

import com.basariatpos.model.*;
import com.basariatpos.repository.SalesOrderRepository;
import com.basariatpos.service.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceImplTest {

    @Mock private SalesOrderRepository mockSalesOrderRepository;
    @Mock private UserSessionService mockUserSessionService;
    @Mock private InventoryItemService mockInventoryItemService;
    @Mock private ProductService mockProductService;
    @Mock private PatientService mockPatientService;

    @InjectMocks
    private SalesOrderServiceImpl salesOrderService;

    private UserDTO testUser;
    private ShiftDTO testShift;
    private SalesOrderDTO inputOrderDto;
    private SalesOrderItemDTO inputItemDtoInventory;
    private SalesOrderItemDTO inputItemDtoService;
    private InventoryItemDTO testInventoryItem;
    private ProductDTO testServiceProduct;
    private PatientDTO testPatient;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
        testUser.setPermissions(new ArrayList<>(List.of("CAN_GIVE_DISCOUNT"))); // Give permission for discount test

        testShift = new ShiftDTO();
        testShift.setShiftId(100);
        testShift.setStatus("Active");
        testShift.setUserId(testUser.getUserId());


        testInventoryItem = new InventoryItemDTO();
        testInventoryItem.setInventoryItemId(101);
        testInventoryItem.setProductNameEn("Test Inventory Item Product");
        testInventoryItem.setItemSpecificNameEn("Specifics");
        testInventoryItem.setActive(true);
        testInventoryItem.setQuantityOnHand(10);
        testInventoryItem.setIsStockItem(true); // Ensure it's a stock item

        testServiceProduct = new ProductDTO();
        testServiceProduct.setProductId(201);
        testServiceProduct.setProductNameEn("Test Service Product");
        testServiceProduct.setActive(true);
        testServiceProduct.setIsService(true);
        testServiceProduct.setIsStockItem(false);

        testPatient = new PatientDTO();
        testPatient.setPatientId(301);
        testPatient.setSystemPatientId("P001");
        testPatient.setFullNameEn("Test Patient");

        inputItemDtoInventory = new SalesOrderItemDTO();
        inputItemDtoInventory.setInventoryItemId(testInventoryItem.getInventoryItemId());
        inputItemDtoInventory.setQuantity(1);
        inputItemDtoInventory.setUnitPrice(new BigDecimal("10.00"));
        // itemDisplayNameEn will be populated by service

        inputItemDtoService = new SalesOrderItemDTO();
        inputItemDtoService.setServiceProductId(testServiceProduct.getProductId());
        inputItemDtoService.setQuantity(1);
        inputItemDtoService.setUnitPrice(new BigDecimal("50.00"));
        // itemDisplayNameEn will be populated by service


        inputOrderDto = new SalesOrderDTO();
        inputOrderDto.setPatientId(testPatient.getPatientId());
        inputOrderDto.setItems(new ArrayList<>(List.of(inputItemDtoInventory))); // Start with one item
        inputOrderDto.setRemarks("Test order");

        // Default mock behaviors
        when(mockUserSessionService.getCurrentUser()).thenReturn(testUser);
        when(mockUserSessionService.isShiftActive()).thenReturn(true);
        when(mockUserSessionService.getActiveShift()).thenReturn(testShift);
    }

    // --- Constructor Tests ---
    @Test
    void constructor_nullRepository_throwsException() {
        assertThrows(NullPointerException.class, () -> new SalesOrderServiceImpl(null, mockUserSessionService, mockInventoryItemService, mockProductService, mockPatientService));
    }
    // Add similar null checks for other constructor params if needed, or rely on @Mock and @InjectMocks to cover this.

    // --- createSalesOrder ---
    @Test
    void createSalesOrder_validOrder_success() throws Exception {
        when(mockPatientService.getPatientById(testPatient.getPatientId())).thenReturn(Optional.of(testPatient));
        when(mockInventoryItemService.getInventoryItemById(testInventoryItem.getInventoryItemId())).thenReturn(Optional.of(testInventoryItem));

        SalesOrderDTO headerWithId = new SalesOrderDTO();
        headerWithId.setSalesOrderId(1);
        when(mockSalesOrderRepository.saveOrderHeader(any(SalesOrderDTO.class))).thenReturn(headerWithId);

        SalesOrderItemDTO itemWithId = new SalesOrderItemDTO();
        itemWithId.setSoItemId(1);
        when(mockSalesOrderRepository.saveOrderItem(any(SalesOrderItemDTO.class))).thenReturn(itemWithId);

        SalesOrderDTO finalOrderMock = new SalesOrderDTO();
        finalOrderMock.setSalesOrderId(1);
        finalOrderMock.setItems(List.of(itemWithId)); // Simplified final order
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(finalOrderMock));

        SalesOrderDTO result = salesOrderService.createSalesOrder(inputOrderDto);

        assertNotNull(result);
        assertEquals(1, result.getSalesOrderId());
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getItems().get(0).getSoItemId());
        assertEquals(testUser.getUserId(), inputOrderDto.getCreatedByUserId());
        assertEquals(testShift.getShiftId(), inputOrderDto.getShiftId());

        verify(mockSalesOrderRepository).saveOrderHeader(inputOrderDto);
        verify(mockSalesOrderRepository).saveOrderItem(inputItemDtoInventory);
        verify(mockSalesOrderRepository).callRecalculateSalesOrderSubtotalProcedure(1);
        verify(mockSalesOrderRepository).findById(1);
    }

    @Test
    void createSalesOrder_noActiveShift_throwsNoActiveShiftException() {
        when(mockUserSessionService.isShiftActive()).thenReturn(false);
        assertThrows(NoActiveShiftException.class, () -> salesOrderService.createSalesOrder(inputOrderDto));
    }

    @Test
    void createSalesOrder_patientNotFound_throwsPatientNotFoundException() {
        when(mockPatientService.getPatientById(testPatient.getPatientId())).thenReturn(Optional.empty());
        assertThrows(PatientNotFoundException.class, () -> salesOrderService.createSalesOrder(inputOrderDto));
    }

    @Test
    void createSalesOrder_inventoryItemNotFound_throwsInventoryItemNotFoundException() {
        when(mockPatientService.getPatientById(testPatient.getPatientId())).thenReturn(Optional.of(testPatient));
        when(mockInventoryItemService.getInventoryItemById(testInventoryItem.getInventoryItemId())).thenReturn(Optional.empty());
        assertThrows(InventoryItemNotFoundException.class, () -> salesOrderService.createSalesOrder(inputOrderDto));
    }

    @Test
    void createSalesOrder_serviceProductNotFound_throwsProductNotFoundException() {
        inputOrderDto.setItems(List.of(inputItemDtoService)); // Switch to service item
        when(mockPatientService.getPatientById(testPatient.getPatientId())).thenReturn(Optional.of(testPatient));
        when(mockProductService.getProductById(testServiceProduct.getProductId())).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> salesOrderService.createSalesOrder(inputOrderDto));
    }


    // --- applyDiscountToOrder ---
    @Test
    void applyDiscountToOrder_hasPermission_success() throws Exception {
        when(mockUserSessionService.hasPermission("CAN_GIVE_DISCOUNT")).thenReturn(true);

        SalesOrderDTO order = new SalesOrderDTO();
        order.setSalesOrderId(1);
        order.setSubtotalAmount(new BigDecimal("100.00"));
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(order));
        // Simulate findById called again after update for the return
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(order));


        BigDecimal discount = new BigDecimal("10.00");
        salesOrderService.applyDiscountToOrder(1, discount);

        verify(mockSalesOrderRepository).updateOrderDiscount(1, discount);
    }

    @Test
    void applyDiscountToOrder_noPermission_throwsPermissionDeniedException() {
        when(mockUserSessionService.hasPermission("CAN_GIVE_DISCOUNT")).thenReturn(false);
        assertThrows(PermissionDeniedException.class, () -> salesOrderService.applyDiscountToOrder(1, new BigDecimal("5.00")));
    }

    @Test
    void applyDiscountToOrder_negativeDiscount_throwsSalesOrderValidationException() {
        when(mockUserSessionService.hasPermission("CAN_GIVE_DISCOUNT")).thenReturn(true);
        SalesOrderDTO order = new SalesOrderDTO();
        order.setSalesOrderId(1);
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(SalesOrderValidationException.class, () -> {
            salesOrderService.applyDiscountToOrder(1, new BigDecimal("-5.00"));
        });
    }


    // --- changeOrderStatus ---
    @Test
    void changeOrderStatus_toCompleted_balanceZero_callsStockUpdate() throws Exception {
        SalesOrderDTO order = new SalesOrderDTO();
        order.setSalesOrderId(1);
        order.setStatus("Pending");
        order.setBalanceDue(BigDecimal.ZERO);
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(order));
        // Simulate findById called again after update for the return
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(order));


        salesOrderService.changeOrderStatus(1, "Completed");

        verify(mockInventoryItemService).processOrderCompletionStockUpdate(1);
        verify(mockSalesOrderRepository).updateOrderStatus(1, "Completed");
    }

    @Test
    void changeOrderStatus_toCompleted_balanceNotZero_throwsSalesOrderValidationException() {
        SalesOrderDTO order = new SalesOrderDTO();
        order.setSalesOrderId(1);
        order.setStatus("Pending");
        order.setBalanceDue(new BigDecimal("10.00"));
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(order));

        SalesOrderValidationException ex = assertThrows(SalesOrderValidationException.class, () -> {
            salesOrderService.changeOrderStatus(1, "Completed");
        });
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Balance due must be <= 0 to complete order.")));
        verify(mockInventoryItemService, never()).processOrderCompletionStockUpdate(anyInt());
    }

    @Test
    void changeOrderStatus_fromCompletedToOther_doesNotCallStockUpdate() throws Exception {
        SalesOrderDTO order = new SalesOrderDTO();
        order.setSalesOrderId(1);
        order.setStatus("Completed");
        order.setBalanceDue(BigDecimal.ZERO);
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(order));
        // Simulate findById called again after update for the return
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(order));

        salesOrderService.changeOrderStatus(1, "PendingInvestigation");

        verify(mockInventoryItemService, never()).processOrderCompletionStockUpdate(1);
        verify(mockSalesOrderRepository).updateOrderStatus(1, "PendingInvestigation");
    }

    // --- addOrderItemToOrder ---
    @Test
    void addOrderItemToOrder_validItem_success() throws Exception {
        SalesOrderDTO existingOrder = new SalesOrderDTO();
        existingOrder.setSalesOrderId(1);
        existingOrder.setItems(new ArrayList<>()); // Start with empty items list
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(existingOrder));
        when(mockInventoryItemService.getInventoryItemById(testInventoryItem.getInventoryItemId())).thenReturn(Optional.of(testInventoryItem));
        when(mockSalesOrderRepository.saveOrderItem(any(SalesOrderItemDTO.class))).thenReturn(inputItemDtoInventory); // Assume it gets an ID
        // For the findById after operations
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(existingOrder));


        SalesOrderDTO result = salesOrderService.addOrderItemToOrder(1, inputItemDtoInventory);

        verify(mockSalesOrderRepository).saveOrderItem(inputItemDtoInventory);
        verify(mockSalesOrderRepository).callRecalculateSalesOrderSubtotalProcedure(1);
        assertNotNull(result);
    }

    @Test
    void addOrderItemToOrder_itemValidationFails_throwsSalesOrderValidationException() throws Exception {
        SalesOrderDTO existingOrder = new SalesOrderDTO();
        existingOrder.setSalesOrderId(1);
        when(mockSalesOrderRepository.findById(1)).thenReturn(Optional.of(existingOrder));

        inputItemDtoInventory.setQuantity(0); // Invalid quantity

        assertThrows(SalesOrderValidationException.class, () -> {
            salesOrderService.addOrderItemToOrder(1, inputItemDtoInventory);
        });
    }
}
