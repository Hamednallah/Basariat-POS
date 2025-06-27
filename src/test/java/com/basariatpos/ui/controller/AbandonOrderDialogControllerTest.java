package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.SalesOrderItemDTO;
import com.basariatpos.service.SalesOrderService;
import com.basariatpos.service.exception.SalesOrderException;
import com.basariatpos.ui.controller.AbandonOrderDialogController.SalesOrderItemWrapper;


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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbandonOrderDialogControllerTest {

    @Mock private Label dialogTitleLabel, orderInfoLabel;
    @Mock private TableView<SalesOrderItemWrapper> itemsTable;
    // Columns are not directly interacted with for logic test usually, unless custom cell factories are complex
    @Mock private BorderPane abandonOrderDialogPane;
    @Mock private Stage mockDialogStage;
    @Mock private SalesOrderService mockSalesOrderService;

    @Spy
    private ObservableList<SalesOrderItemWrapper> itemWrappers = FXCollections.observableArrayList();

    @InjectMocks
    private AbandonOrderDialogController controller;

    private static ResourceBundle resourceBundle;
    private SalesOrderDTO testOrder;
    private SalesOrderItemDTO testItemStock, testItemService;

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
        controller.orderInfoLabel = orderInfoLabel;
        controller.itemsTable = itemsTable;
        controller.abandonOrderDialogPane = abandonOrderDialogPane;

        controller.itemWrappers = itemWrappers; // Inject spy

        testOrder = new SalesOrderDTO();
        testOrder.setSalesOrderId(1L);
        testOrder.setPatientFullName("Test Patient");
        testOrder.setStatus("Pending");
        testOrder.setTotalAmount(new BigDecimal("100.00"));

        testItemStock = new SalesOrderItemDTO();
        testItemStock.setSoItemId(10);
        testItemStock.setInventoryItemId(100L); // Stock item
        testItemStock.setItemDisplayNameEn("Stock Item A");
        testItemStock.setQuantity(2);

        testItemService = new SalesOrderItemDTO();
        testItemService.setSoItemId(11);
        testItemService.setServiceProductId(200L); // Service item
        testItemService.setItemDisplayNameEn("Service B");
        testItemService.setQuantity(1);

        List<SalesOrderItemDTO> orderItems = new ArrayList<>();
        orderItems.add(testItemStock);
        orderItems.add(testItemService);
        testOrder.setItems(orderItems);

        when(itemsTable.getItems()).thenReturn(itemWrappers);

        controller.initialize(); // For basic FXML setup like column factories
        // initializeDialog is the main entry for this controller
    }

    @Test
    void initializeDialog_populatesInfoAndItems_setsOrientation() {
        controller.initializeDialog(testOrder, mockSalesOrderService, mockDialogStage);

        verify(dialogTitleLabel).setText(MessageProvider.getString("abandonorder.dialog.title", "1"));
        assertTrue(itemWrappers.size() == 2);
        // Stock item should be selected for restock by default, service item should not be
        SalesOrderItemWrapper stockWrapper = itemWrappers.stream().filter(w -> w.getOrderItem() == testItemStock).findFirst().orElse(null);
        SalesOrderItemWrapper serviceWrapper = itemWrappers.stream().filter(w -> w.getOrderItem() == testItemService).findFirst().orElse(null);

        assertNotNull(stockWrapper);
        assertTrue(stockWrapper.isRestock()); // Default for stockable
        assertNotNull(serviceWrapper);
        assertFalse(serviceWrapper.isRestock()); // Default for non-stockable

        verify(abandonOrderDialogPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void handleConfirmAbandonmentButtonAction_validRestockSelection_callsService() throws SalesOrderException {
        controller.initializeDialog(testOrder, mockSalesOrderService, mockDialogStage);
        // Simulate user deselecting the stock item for restocking
        itemWrappers.stream()
            .filter(w -> w.getOrderItem().getInventoryItemId() != null) // Find the stock item wrapper
            .findFirst().ifPresent(w -> w.setRestock(false)); // User unchecks restock for stock item

        controller.handleConfirmAbandonmentButtonAction(null);

        assertTrue(controller.isAbandonConfirmed());
        ArgumentCaptor<List<Integer>> restockIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockSalesOrderService).abandonOrder(eq(testOrder.getSalesOrderId()), restockIdsCaptor.capture());
        assertTrue(restockIdsCaptor.getValue().isEmpty()); // No items selected for restock
        verify(mockDialogStage).close();
    }

    @Test
    void handleConfirmAbandonmentButtonAction_stockItemRestock_callsServiceWithId() throws SalesOrderException {
        controller.initializeDialog(testOrder, mockSalesOrderService, mockDialogStage);
        // Stock item is restock=true by default from wrapper constructor

        controller.handleConfirmAbandonmentButtonAction(null);

        assertTrue(controller.isAbandonConfirmed());
        ArgumentCaptor<List<Integer>> restockIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockSalesOrderService).abandonOrder(eq(testOrder.getSalesOrderId()), restockIdsCaptor.capture());

        List<Integer> capturedIds = restockIdsCaptor.getValue();
        assertEquals(1, capturedIds.size());
        assertTrue(capturedIds.contains(testItemStock.getSoItemId()));
        verify(mockDialogStage).close();
    }

    @Test
    void initializeDialog_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initialize();
        controller.initializeDialog(testOrder, mockSalesOrderService, mockDialogStage);

        verify(abandonOrderDialogPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
