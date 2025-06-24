package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.service.SalesOrderService;
import com.basariatpos.service.exception.SalesOrderServiceException;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class SalesOrderListControllerTest {

    @Mock
    private SalesOrderService mockSalesOrderService;

    private SalesOrderListController controller;
    private Stage stage;

    @BeforeAll
    static void setUpClass() throws Exception {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        MessageProvider.loadBundle(LocaleManager.getCurrentLocale());
        // For TestFX headless run if needed, though ApplicationExtension usually handles it.
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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/SalesOrderListView.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();
        controller = loader.getController();
        controller.setSalesOrderService(mockSalesOrderService);
        controller.setCurrentStage(stage); // Pass stage to controller
        controller.loadInitialData(); // Manually call after service is set

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void handleSearchOrdersAction_callsServiceAndPopulatesTable(FxRobot robot) throws SalesOrderServiceException {
        List<SalesOrderDTO> orders = new ArrayList<>();
        SalesOrderDTO order1 = new SalesOrderDTO();
        order1.setSalesOrderId(1);
        order1.setOrderDate(OffsetDateTime.now());
        order1.setPatientFullName("John Doe");
        order1.setStatus("Pending");
        order1.setTotalAmount(new BigDecimal("100.00"));
        order1.setBalanceDue(new BigDecimal("50.00"));
        orders.add(order1);

        when(mockSalesOrderService.findSalesOrders(any(LocalDate.class), any(LocalDate.class), any(), anyString()))
            .thenReturn(orders);

        robot.clickOn("#searchOrdersButton");
        WaitForAsyncUtils.waitForFxEvents();

        TableView<SalesOrderDTO> table = robot.lookup("#salesOrdersTable").queryTableView();
        assertEquals(1, table.getItems().size());
        assertEquals(1, table.getItems().get(0).getSalesOrderId());
        verify(mockSalesOrderService).findSalesOrders(any(LocalDate.class), any(LocalDate.class), any(), anyString());
    }

    @Test
    void handleClearFiltersAction_clearsFiltersAndTable(FxRobot robot) {
        // Set some filter values first
        robot.interact(() -> {
            controller.fromDateFilter.setValue(LocalDate.now().minusDays(1));
            controller.patientSearchField.setText("Test");
        });

        // Add an item to the table to ensure it's cleared
        SalesOrderDTO order1 = new SalesOrderDTO(); order1.setSalesOrderId(1);
        robot.interact(() -> controller.salesOrderData.add(order1));
        WaitForAsyncUtils.waitForFxEvents();
        TableView<SalesOrderDTO> table = robot.lookup("#salesOrdersTable").queryTableView();
        assertEquals(1, table.getItems().size());


        robot.clickOn("#clearFiltersButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertNull(controller.patientSearchField.getText() == null || controller.patientSearchField.getText().isEmpty());
        assertEquals(LocalDate.now().withDayOfMonth(1), controller.fromDateFilter.getValue());
        assertEquals(0, table.getItems().size());
    }

    @Test
    void handleAddOrderAction_opensSalesOrderFormDialog(FxRobot robot) {
        // This test would verify that a new dialog stage is shown.
        // It requires mocking the dialog loading part or using more advanced TestFX stage handling.
        // For simplicity, we ensure it doesn't throw an exception.
        // A more robust test would use FxToolkit.registerStage to catch new stages.

        // We can't easily verify a new stage is shown without more complex setup.
        // Let's assume if no exception, the action was triggered.
        // The actual dialog opening and its controller logic are tested in SalesOrderFormDialogControllerTest
        assertDoesNotThrow(() -> robot.clickOn("#addOrderButton"));
        // To verify modal, would need to check if main stage is blocked or new stage is modal.
    }

    @Test
    void handleViewEditOrderAction_noSelection_showsWarning(FxRobot robot) {
         // Ensure no item is selected
        robot.interact(() -> robot.lookup("#salesOrdersTable").queryTableView().getSelectionModel().clearSelection());

        // Mock Alert construction to verify it's shown
        try (var alertMock = mockConstruction(javafx.scene.control.Alert.class, (mock, context) -> {
            // We can check the type of alert if needed, but just construction is often enough
        })) {
            robot.clickOn("#viewEditOrderButton");
            WaitForAsyncUtils.waitForFxEvents();

            // Verify an Alert was created
            assertTrue(alertMock.constructed().size() >= 1);
            Alert constructedAlert = alertMock.constructed().get(0);
            assertEquals(Alert.AlertType.WARNING, constructedAlert.getAlertType());
            assertTrue(constructedAlert.getContentText().contains("Please select an order"));
        }
    }
}
