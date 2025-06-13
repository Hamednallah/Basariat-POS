package com.basariatpos.service;

import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.repository.PurchaseOrderRepository;
import com.basariatpos.service.exception.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {

    @Mock private PurchaseOrderRepository mockPoRepository;
    @Mock private InventoryItemService mockInventoryItemService;
    @Mock private UserSessionService mockUserSessionService;

    @InjectMocks
    private PurchaseOrderServiceImpl poService;

    private PurchaseOrderDTO testPoDto;
    private PurchaseOrderItemDTO testPoItemDto;
    private UserDTO mockCurrentUser;
    private InventoryItemDTO mockInventoryItem;


    @BeforeEach
    void setUp() {
        mockCurrentUser = new UserDTO(1, "testuser", "Test User", "Admin");

        testPoDto = new PurchaseOrderDTO(1, LocalDate.now(), "Test Supplier",
                                         BigDecimal.ZERO, "Pending", 1, "testuser", OffsetDateTime.now(), new ArrayList<>());
        testPoItemDto = new PurchaseOrderItemDTO();
        testPoItemDto.setPoItemId(10);
        testPoItemDto.setPurchaseOrderId(1);
        testPoItemDto.setInventoryItemId(101);
        testPoItemDto.setQuantityOrdered(10);
        testPoItemDto.setPurchasePricePerUnit(new BigDecimal("10.00"));
        testPoDto.getItems().add(testPoItemDto);

        mockInventoryItem = new InventoryItemDTO(); // Basic DTO for InventoryItemService mock
        mockInventoryItem.setInventoryItemId(101);
        mockInventoryItem.setProductId(201);
        mockInventoryItem.setProductNameEn("Test Inventory Product");
    }

    // --- createNewPurchaseOrder ---
    @Test
    void createNewPurchaseOrder_validDto_returnsSavedDtoWithUser() throws Exception {
        when(mockUserSessionService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(mockInventoryItemService.getInventoryItemById(anyInt())).thenReturn(Optional.of(mockInventoryItem)); // Assume item exists
        when(mockPoRepository.saveNewOrderWithItems(any(PurchaseOrderDTO.class))).thenAnswer(inv -> {
            PurchaseOrderDTO arg = inv.getArgument(0);
            arg.setPurchaseOrderId(100); // Simulate ID generation
            arg.setCreatedAt(OffsetDateTime.now());
            return arg;
        });

        PurchaseOrderDTO result = poService.createNewPurchaseOrder(testPoDto);

        assertNotNull(result);
        assertEquals(100, result.getPurchaseOrderId());
        assertEquals(mockCurrentUser.getUserId(), result.getCreatedByUserId());
        verify(mockPoRepository).saveNewOrderWithItems(testPoDto);
    }

    @Test
    void createNewPurchaseOrder_noUserInSession_throwsPurchaseOrderException() {
        when(mockUserSessionService.getCurrentUser()).thenReturn(null);
        assertThrows(PurchaseOrderException.class, () -> {
            poService.createNewPurchaseOrder(testPoDto);
        });
    }

    @Test
    void createNewPurchaseOrder_emptyItemsList_throwsPurchaseOrderValidationException() {
        testPoDto.setItems(new ArrayList<>()); // Empty items
        assertThrows(PurchaseOrderValidationException.class, () -> {
            poService.createNewPurchaseOrder(testPoDto);
        });
    }


    // --- receiveStockForItem ---
    @Test
    void receiveStockForItem_validReception_callsRepository() throws Exception {
        when(mockPoRepository.findById(1)).thenReturn(Optional.of(testPoDto));
        when(mockInventoryItemService.getInventoryItemById(101)).thenReturn(Optional.of(mockInventoryItem));
        doNothing().when(mockPoRepository).updateOrderItemReceivedQuantityAndPrice(10, 20, new BigDecimal("10.50")); // Assuming total received is 10+10=20

        testPoItemDto.setQuantityReceived(10); // Already received 10
        testPoItemDto.setQuantityOrdered(30);  // Ordered 30

        poService.receiveStockForItem(10, 1, 10, new BigDecimal("10.50")); // Receive 10 more

        verify(mockPoRepository).updateOrderItemReceivedQuantityAndPrice(10, 20, new BigDecimal("10.50"));
    }

    @Test
    void receiveStockForItem_overReceive_throwsStockReceivingException() {
        when(mockPoRepository.findById(1)).thenReturn(Optional.of(testPoDto));
        // testPoItemDto has quantityOrdered=10, quantityReceived=0 initially in this DTO instance for test
        testPoItemDto.setQuantityOrdered(10);
        testPoItemDto.setQuantityReceived(0);

        assertThrows(StockReceivingException.class, () -> {
            poService.receiveStockForItem(10, 1, 15, new BigDecimal("10.00")); // Attempt to receive 15
        });
    }

    @Test
    void receiveStockForItem_poNotFound_throwsPurchaseOrderNotFoundException() {
        when(mockPoRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(PurchaseOrderNotFoundException.class, () -> {
            poService.receiveStockForItem(10, 99, 5, BigDecimal.TEN);
        });
    }

    @Test
    void receiveStockForItem_poItemNotFoundOnPo_throwsPurchaseOrderNotFoundException() {
        when(mockPoRepository.findById(1)).thenReturn(Optional.of(testPoDto)); // PO exists
        // testPoDto contains item 10. Trying to receive for item 999.
        assertThrows(PurchaseOrderNotFoundException.class, () -> {
            poService.receiveStockForItem(999, 1, 5, BigDecimal.TEN);
        });
    }

    @Test
    void receiveStockForItem_invalidQuantity_throwsValidationException() {
         assertThrows(ValidationException.class, () -> {
            poService.receiveStockForItem(10,1,0, BigDecimal.TEN);
        });
    }


    // --- updatePurchaseOrderStatus ---
    @Test
    void updatePurchaseOrderStatus_valid_callsRepository() throws Exception {
        when(mockPoRepository.findById(1)).thenReturn(Optional.of(testPoDto));
        doNothing().when(mockPoRepository).updateOrderStatus(1, "Received");

        poService.updatePurchaseOrderStatus(1, "Received");
        verify(mockPoRepository).updateOrderStatus(1, "Received");
    }

    // Add more tests for other methods: updatePurchaseOrderHeader, addOrUpdateItemOnOrder, removeItemFromOrder,
    // getAllPurchaseOrderSummaries, getPurchaseOrderDetails and various exception scenarios.
}
