package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;
import com.basariatpos.service.PurchaseOrderService;
import com.basariatpos.service.exception.PurchaseOrderException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockReceivingDialogControllerTest {

    @Mock private Label dialogTitleLabel, poDetailsLabel;
    @Mock private TableView<PurchaseOrderItemDTO> itemsToReceiveTable;
    @Mock private TableColumn<PurchaseOrderItemDTO, Integer> qtyToReceiveNowColumn;
    @Mock private TableColumn<PurchaseOrderItemDTO, BigDecimal> newPricePerUnitColumn;
    // Other columns are not directly interacted with in the same way for edit commits
    @Mock private BorderPane stockReceivingDialogRootPane;
    @Mock private Stage mockDialogStage;
    @Mock private PurchaseOrderService mockPoService;

    @Spy
    private ObservableList<PurchaseOrderItemDTO> itemsToReceiveList = FXCollections.observableArrayList();

    @InjectMocks
    private StockReceivingDialogController controller;

    private static ResourceBundle resourceBundle;
    private PurchaseOrderDTO testPO;
    private PurchaseOrderItemDTO testItem1, testItem2;

    @BeforeAll
    static void setUpClass() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        resourceBundle = MessageProvider.getBundle();
         try {
            new javafx.embed.swing.JFXPanel();
        } catch (Exception e) { /* Ignore */ }
    }

    @BeforeEach
    void setUp() {
        controller.dialogTitleLabel = dialogTitleLabel;
        controller.poDetailsLabel = poDetailsLabel;
        controller.itemsToReceiveTable = itemsToReceiveTable;
        controller.qtyToReceiveNowColumn = qtyToReceiveNowColumn;
        controller.newPricePerUnitColumn = newPricePerUnitColumn;
        controller.stockReceivingDialogRootPane = stockReceivingDialogRootPane;

        // Spy injection for the list
        controller.itemsToReceiveList = itemsToReceiveList;


        testPO = new PurchaseOrderDTO();
        testPO.setPurchaseOrderId(1L);
        testPO.setSupplierName("Test Supplier");
        testPO.setOrderDate(LocalDate.now());

        testItem1 = new PurchaseOrderItemDTO();
        testItem1.setPoItemId(101L);
        testItem1.setInventoryItemId(1);
        testItem1.setInventoryItemDisplayFullName("Item A");
        testItem1.setQuantityOrdered(10);
        testItem1.setQuantityReceived(2); // Remaining 8
        testItem1.setPurchasePricePerUnit(new BigDecimal("5.00"));

        testItem2 = new PurchaseOrderItemDTO();
        testItem2.setPoItemId(102L);
        testItem2.setInventoryItemId(2);
        testItem2.setInventoryItemDisplayFullName("Item B");
        testItem2.setQuantityOrdered(5);
        testItem2.setQuantityReceived(5); // Fully received

        List<PurchaseOrderItemDTO> poItems = new ArrayList<>();
        poItems.add(testItem1);
        poItems.add(testItem2);
        testPO.setItems(poItems);

        when(itemsToReceiveTable.getItems()).thenReturn(itemsToReceiveList);
        // Mock getCellObservableValue for qtyToReceiveNowColumn and newPricePerUnitColumn
        // This is a simplification; real cell value access is more complex.
        when(qtyToReceiveNowColumn.getCellObservableValue(any(PurchaseOrderItemDTO.class)))
            .thenAnswer(invocation -> new javafx.beans.property.SimpleObjectProperty<>(0)); // Default to 0
        when(newPricePerUnitColumn.getCellObservableValue(any(PurchaseOrderItemDTO.class)))
            .thenAnswer(invocation -> new javafx.beans.property.SimpleObjectProperty<>( ((PurchaseOrderItemDTO)invocation.getArgument(0)).getPurchasePricePerUnit() ));


        controller.initializeDialog(mockPoService, mockDialogStage, testPO);
    }

    @Test
    void initializeDialog_filtersFullyReceivedItems_setsOrientation() {
        assertEquals(1, itemsToReceiveList.size()); // Only testItem1 should be in the list
        assertSame(testItem1, itemsToReceiveList.get(0));
        verify(dialogTitleLabel).setText(MessageProvider.getString("po.dialog.receive.title", "1"));
        verify(stockReceivingDialogRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void handleConfirmReceptionButtonAction_validReception_callsService() throws Exception {
        // Simulate user inputting a quantity to receive for testItem1
        // This is tricky because cell editing is complex. We'll mock the outcome of cell editing.
        // Assume qtyToReceiveNowColumn's cell for testItem1 now effectively holds '5'
        // And newPricePerUnitColumn's cell for testItem1 holds '5.50'

        // To test the loop in handleConfirmReceptionButtonAction, we directly modify the DTOs in the list
        // as if the table editing has already updated them, or mock getCellObservableValue more precisely.
        // For this test, we'll use the simplified getCellObservableValue mock from setUp and
        // ensure the controller fetches these mocked values.

        // Let's refine the mock for getCellObservableValue for this specific test
        when(qtyToReceiveNowColumn.getCellObservableValue(testItem1))
            .thenReturn(new javafx.beans.property.SimpleObjectProperty<>(5)); // User wants to receive 5
        when(newPricePerUnitColumn.getCellObservableValue(testItem1))
            .thenReturn(new javafx.beans.property.SimpleObjectProperty<>(new BigDecimal("5.50"))); // New price


        controller.handleConfirmReceptionButtonAction(null);

        assertTrue(controller.isReceptionConfirmed());
        verify(mockPoService).receiveStockForItem(testItem1.getPoItemId(), testPO.getPurchaseOrderId(), 5, new BigDecimal("5.50"));
        verify(mockDialogStage).close();
    }

    @Test
    void handleConfirmReceptionButtonAction_qtyExceedsRemaining_showsError() {
        // Simulate qtyToReceiveNow > remaining
        when(qtyToReceiveNowColumn.getCellObservableValue(testItem1))
            .thenReturn(new javafx.beans.property.SimpleObjectProperty<>(9)); // Remaining is 8 for testItem1

        when(itemsToReceiveTable.getScene()).thenReturn(mock(javafx.scene.Scene.class)); // For showErrorAlert
        when(itemsToReceiveTable.getScene().getWindow()).thenReturn(mockDialogStage);

        controller.handleConfirmReceptionButtonAction(null);

        assertFalse(controller.isReceptionConfirmed());
        verify(mockPoService, never()).receiveStockForItem(anyLong(), anyLong(), anyInt(), any());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void initializeDialog_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initializeDialog(mockPoService, mockDialogStage, testPO);

        verify(stockReceivingDialogRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
