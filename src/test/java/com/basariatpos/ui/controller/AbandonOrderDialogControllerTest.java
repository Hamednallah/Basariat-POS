package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.SalesOrderItemDTO;
import com.basariatpos.service.SalesOrderService;
import com.basariatpos.service.exception.SalesOrderValidationException;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane; // If FXML root is DialogPane
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class AbandonOrderDialogControllerTest {

    @Mock private SalesOrderService mockSalesOrderService;

    private AbandonOrderDialogController controller;
    private Stage stage;
    private SalesOrderDTO testOrder;
    private SalesOrderItemDTO stockItem1;
    private SalesOrderItemDTO stockItem2;
    private SalesOrderItemDTO serviceItem1;

    @BeforeAll
    static void setUpClass() throws Exception {
        // Initialize JavaFX Toolkit if not already running
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already running, ignore
        }
        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Or your test default
        MessageProvider.loadBundle(LocaleManager.getCurrentLocale());
        // Headless setup for CI if needed (copied from other tests)
        if (System.getProperty("os.name", "").toLowerCase().startsWith("linux")) {
            System.setProperty("java.awt.headless", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (stage != null && stage.isShowing()) {
             org.testfx.api.FxToolkit.cleanupStages();
        }
    }

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        // Prepare test data
        testOrder = new SalesOrderDTO();
        testOrder.setSalesOrderId(123);
        testOrder.setPatientFullName("Test Patient");
        testOrder.setStatus("Pending");
        testOrder.setTotalAmount(new BigDecimal("250.00"));
        testOrder.setOrderDate(OffsetDateTime.now());

        stockItem1 = new SalesOrderItemDTO();
        stockItem1.setSoItemId(101);
        stockItem1.setInventoryItemId(1); // Stock item
        stockItem1.setItemDisplayNameEn("Stock Item A");
        stockItem1.setQuantity(2);

        stockItem2 = new SalesOrderItemDTO();
        stockItem2.setSoItemId(102);
        stockItem2.setInventoryItemId(2); // Stock item
        stockItem2.setItemDisplayNameEn("Stock Item B");
        stockItem2.setQuantity(1);

        serviceItem1 = new SalesOrderItemDTO();
        serviceItem1.setSoItemId(103);
        serviceItem1.setServiceProductId(3); // Service item
        serviceItem1.setItemDisplayNameEn("Service Item C");
        serviceItem1.setQuantity(1);

        List<SalesOrderItemDTO> items = new ArrayList<>();
        items.add(stockItem1);
        items.add(stockItem2);
        items.add(serviceItem1);
        testOrder.setItems(items);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/AbandonOrderDialog.fxml"));
        loader.setResources(MessageProvider.getBundle());
        // Assuming FXML root is BorderPane as per its creation
        Parent root = loader.load();
        controller = loader.getController();

        // Call initializeDialog on the JavaFX application thread
        Platform.runLater(() -> controller.initializeDialog(testOrder, mockSalesOrderService, stage));
        WaitForAsyncUtils.waitForFxEvents();


        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(MessageProvider.getString("abandonorder.dialog.title", String.valueOf(testOrder.getSalesOrderId())));
        stage.show();
    }

    @Test
    void initializeDialog_populatesOrderInfoAndItemsTable(FxRobot robot) {
        Label orderInfoLbl = robot.lookup("#orderInfoLabel").queryAs(Label.class);
        assertTrue(orderInfoLbl.getText().contains(String.valueOf(testOrder.getSalesOrderId())));
        assertTrue(orderInfoLbl.getText().contains(testOrder.getPatientFullName()));

        TableView<AbandonOrderDialogController.SalesOrderItemWrapper> table = robot.lookup("#itemsTable").queryTableView();
        assertEquals(3, table.getItems().size());

        // Check item descriptions
        assertEquals(stockItem1.getItemDisplayNameEn(), table.getItems().get(0).getItemDescriptionForDisplay());
        assertEquals(serviceItem1.getItemDisplayNameEn(), table.getItems().get(2).getItemDescriptionForDisplay());

        // Check quantities
        assertEquals(stockItem1.getQuantity(), table.getItems().get(0).getQuantity());
    }

    @Test
    void initializeDialog_restockCheckboxStateAndAvailability(FxRobot robot) {
        TableView<AbandonOrderDialogController.SalesOrderItemWrapper> table = robot.lookup("#itemsTable").queryTableView();

        // Stock Item 1 - should be enabled, default checked
        AbandonOrderDialogController.SalesOrderItemWrapper wrapperStock1 = table.getItems().get(0);
        assertTrue(wrapperStock1.isRestock(), "Stock item 1 should default to restock=true");
        // Checkbox in cell should be enabled
        CheckBox cbStock1 = getCheckBoxFromCell(robot, table, 0, controller.restockItemColumn);
        assertNotNull(cbStock1, "Checkbox for stock item 1 not found");
        assertFalse(cbStock1.isDisabled(), "Checkbox for stock item 1 should be enabled");


        // Stock Item 2 - should be enabled, default checked
        AbandonOrderDialogController.SalesOrderItemWrapper wrapperStock2 = table.getItems().get(1);
        assertTrue(wrapperStock2.isRestock(), "Stock item 2 should default to restock=true");
        CheckBox cbStock2 = getCheckBoxFromCell(robot, table, 1, controller.restockItemColumn);
        assertNotNull(cbStock2, "Checkbox for stock item 2 not found");
        assertFalse(cbStock2.isDisabled(), "Checkbox for stock item 2 should be enabled");


        // Service Item 1 - should be disabled, default unchecked
        AbandonOrderDialogController.SalesOrderItemWrapper wrapperService1 = table.getItems().get(2);
        assertFalse(wrapperService1.isRestock(), "Service item should default to restock=false");
        CheckBox cbService1 = getCheckBoxFromCell(robot, table, 2, controller.restockItemColumn);
        assertNotNull(cbService1, "Checkbox for service item 1 not found");
        assertTrue(cbService1.isDisabled(), "Checkbox for service item 1 should be disabled");
    }

    private CheckBox getCheckBoxFromCell(FxRobot robot, TableView<AbandonOrderDialogController.SalesOrderItemWrapper> table, int rowIndex, TableColumn<AbandonOrderDialogController.SalesOrderItemWrapper, Boolean> column) {
        // This is a helper to get the CheckBox from within a CheckBoxTableCell.
        // It assumes the CheckBox is the graphic of the cell.
        return robot.from(table)
            .lookup(row -> row.getTableRow().getIndex() == rowIndex)
            .lookup(col -> table.getColumns().indexOf(col) == table.getColumns().indexOf(column))
            .queryAs(CheckBoxTableCell.class) // Query as the cell
            .getChildren().stream() // Get children of the cell
            .filter(node -> node instanceof CheckBox) // Find the CheckBox
            .map(node -> (CheckBox) node)
            .findFirst()
            .orElse(null);
    }


    @Test
    void handleConfirmAbandonment_restockSelection_callsServiceCorrectly(FxRobot robot) throws Exception {
        // Uncheck the first stock item for restocking
        Platform.runLater(() -> controller.itemsTable.getItems().get(0).setRestock(false));
        // Second stock item remains true (default)
        // Service item is false and disabled (default)
        WaitForAsyncUtils.waitForFxEvents();

        ArgumentCaptor<List<Integer>> restockListCaptor = ArgumentCaptor.forClass(List.class);
        when(mockSalesOrderService.abandonOrder(eq(testOrder.getSalesOrderId()), restockListCaptor.capture()))
            .thenReturn(new SalesOrderDTO()); // Return a dummy DTO for success

        robot.clickOn("#confirmButton");
        WaitForAsyncUtils.waitForFxEvents(); // Wait for service call and alert

        verify(mockSalesOrderService).abandonOrder(eq(testOrder.getSalesOrderId()), anyList());
        List<Integer> capturedList = restockListCaptor.getValue();
        assertNotNull(capturedList);
        assertEquals(1, capturedList.size(), "Only one item should be marked for restocking.");
        assertTrue(capturedList.contains(stockItem2.getSoItemId()), "Stock Item B (ID 102) should be in restock list.");
        assertFalse(capturedList.contains(stockItem1.getSoItemId()), "Stock Item A (ID 101) should NOT be in restock list.");
        assertFalse(capturedList.contains(serviceItem1.getSoItemId()), "Service Item C (ID 103) should NOT be in restock list.");

        assertTrue(controller.isAbandonConfirmed());
        // Stage should be closed by controller on success (assertFalse(stage.isShowing()) can be flaky)
    }

    @Test
    void handleConfirmAbandonment_serviceThrowsValidationException_showsErrorAlert(FxRobot robot) throws Exception {
        String validationErrorMsg = "Order status is too advanced to abandon.";
        when(mockSalesOrderService.abandonOrder(anyInt(), anyList()))
            .thenThrow(new SalesOrderValidationException(validationErrorMsg, List.of(validationErrorMsg)));

        // Use try-with-resources for mockConstruction if available, or manage manually
        try (var alertMock = org.mockito.Mockito.mockConstruction(Alert.class)) {
            robot.clickOn("#confirmButton");
            WaitForAsyncUtils.waitForFxEvents();

            assertTrue(alertMock.constructed().size() > 0, "Alert should have been shown.");
            Alert shownAlert = alertMock.constructed().get(0);
            assertEquals(Alert.AlertType.ERROR, shownAlert.getAlertType());
            assertTrue(shownAlert.getContentText().contains(validationErrorMsg));
            assertFalse(controller.isAbandonConfirmed()); // Should not be confirmed
            assertTrue(stage.isShowing()); // Dialog should remain open
        }
    }

    @Test
    void handleCancelButton_closesDialog_abandonNotConfirmed(FxRobot robot) {
        robot.clickOn("#cancelButton");
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(controller.isAbandonConfirmed());
        assertFalse(stage.isShowing()); // Verify dialog is closed
    }
}
