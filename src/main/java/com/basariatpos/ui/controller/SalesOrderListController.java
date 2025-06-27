package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.service.SalesOrderService;
import com.basariatpos.service.exception.SalesOrderServiceException;
import com.basariatpos.ui.utilui.AlertUtil;
import com.basariatpos.ui.utilui.DateTimeUtil;
import com.basariatpos.util.AppLogger;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SalesOrderListController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(SalesOrderListController.class);

    @FXML private DatePicker fromDateFilter;
    @FXML private DatePicker toDateFilter;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TextField patientSearchField;
    @FXML private Button searchOrdersButton;
    @FXML private Button clearFiltersButton;
    @FXML private TableView<SalesOrderDTO> salesOrdersTable;
    @FXML private TableColumn<SalesOrderDTO, Integer> idColumn;
    @FXML private TableColumn<SalesOrderDTO, String> dateColumn; // String for formatted date
    @FXML private TableColumn<SalesOrderDTO, String> patientColumn;
    @FXML private TableColumn<SalesOrderDTO, String> statusColumn;
    @FXML private TableColumn<SalesOrderDTO, BigDecimal> totalColumn;
    @FXML private TableColumn<SalesOrderDTO, BigDecimal> balanceDueColumn;
    @FXML private Button addOrderButton;
    @FXML private Button viewEditOrderButton;
    @FXML private BorderPane salesOrderListRootPane; // For RTL

    private SalesOrderService salesOrderService;
    private ObservableList<SalesOrderDTO> salesOrderData = FXCollections.observableArrayList();

    // To hold the stage for this view, if needed for dialog ownership
    private Stage currentStage;

    public void setSalesOrderService(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    public void setCurrentStage(Stage stage) {
        this.currentStage = stage;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        populateStatusFilterCombo();

        // Default date range (e.g., this month)
        fromDateFilter.setValue(LocalDate.now().withDayOfMonth(1));
        toDateFilter.setValue(LocalDate.now());

        salesOrdersTable.setItems(salesOrderData);
        updateNodeOrientation();
        // Load initial data in loadInitialData() called by MainFrameController
    }

    private void updateNodeOrientation() {
        if (salesOrderListRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                salesOrderListRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                salesOrderListRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("salesOrderListRootPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("salesOrderId"));
        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(DateTimeUtil.formatUserFriendlyDateTime(cellData.getValue().getOrderDate())));
        patientColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getPatientFullName() != null ?
                                     cellData.getValue().getPatientFullName() :
                                     (cellData.getValue().getPatientSystemId() != null ?
                                      cellData.getValue().getPatientSystemId() : MessageProvider.getString("label.anonymous")))); // Localized "N/A"
        statusColumn.setCellValueFactory(cellData -> {
            String statusVal = cellData.getValue().getStatus();
            String statusKey = "salesorder.status." + (statusVal != null ? statusVal.toLowerCase() : "unknown");
            return new SimpleStringProperty(MessageProvider.getString(statusKey, statusVal)); // Localized status
        });
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        balanceDueColumn.setCellValueFactory(new PropertyValueFactory<>("balanceDue"));

        // TODO: Add custom cell formatting for amounts if needed (e.g. currency symbol from CenterProfile)
        // For now, assuming amounts are displayed as plain numbers. CSS class 'table-column-amount' can be used for alignment.
    }

    private void populateStatusFilterCombo() {
        // Example statuses - these should align with actual statuses used in the system
        List<String> statuses = Arrays.asList("All", "Pending", "Completed", "Cancelled", "Abandoned");
        statusFilterCombo.setItems(FXCollections.observableArrayList(statuses));
        statusFilterCombo.setValue("All"); // Default selection
    }

    @FXML
    void handleSearchOrdersAction(ActionEvent event) {
        if (salesOrderService == null) {
            AlertUtil.showError("Service Error", "Sales Order Service not available.");
            return;
        }
        LocalDate from = fromDateFilter.getValue();
        LocalDate to = toDateFilter.getValue();
        String status = statusFilterCombo.getValue();
        if ("All".equals(status)) {
            status = null; // No status filter
        }
        String patientQuery = patientSearchField.getText();

        try {
            List<SalesOrderDTO> orders = salesOrderService.findSalesOrders(from, to, status, patientQuery);
            salesOrderData.setAll(orders);
            if (orders.isEmpty()) {
                AlertUtil.showInfo("Search Results", "No sales orders found matching your criteria.");
            }
        } catch (SalesOrderServiceException e) {
            logger.error("Error searching sales orders: {}", e.getMessage(), e);
            AlertUtil.showError("Search Error", "Failed to retrieve sales orders: " + e.getMessage());
        }
    }

    @FXML
    void handleClearFiltersAction(ActionEvent event) {
        fromDateFilter.setValue(LocalDate.now().withDayOfMonth(1));
        toDateFilter.setValue(LocalDate.now());
        statusFilterCombo.setValue("All");
        patientSearchField.clear();
        salesOrderData.clear();
    }

    @FXML
    void handleAddOrderAction(ActionEvent event) {
        showSalesOrderFormDialog(null);
    }

    @FXML
    void handleViewEditOrderAction(ActionEvent event) {
        SalesOrderDTO selectedOrder = salesOrdersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            AlertUtil.showWarning("No Selection", "Please select an order from the table to view/edit.");
            return;
        }
        showSalesOrderFormDialog(selectedOrder);
    }

    private void showSalesOrderFormDialog(SalesOrderDTO orderToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/SalesOrderFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent parent = loader.load();

            SalesOrderFormDialogController controller = loader.getController();

            // Pass necessary services to the form dialog controller using AppLauncher
            controller.setServices(
                com.basariatpos.main.AppLauncher.getSalesOrderService(),
                com.basariatpos.main.AppLauncher.getPatientService(),
                com.basariatpos.main.AppLauncher.getInventoryItemService(),
                com.basariatpos.main.AppLauncher.getProductService(),
                com.basariatpos.main.AppLauncher.getUserSessionService(),
                com.basariatpos.main.AppLauncher.getWhatsAppNotificationService(), // Added
                com.basariatpos.main.AppLauncher.getCenterProfileService()      // Added
            );

            controller.initializeDialogData(orderToEdit);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(orderToEdit == null ?
                                 MessageProvider.getString("salesorder.form.add.title") :
                                 MessageProvider.getString("salesorder.form.edit.title", String.valueOf(orderToEdit.getSalesOrderId())));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (this.currentStage != null) { // currentStage is the stage of SalesOrderListView
                 dialogStage.initOwner(this.currentStage);
            } else if (addOrderButton != null && addOrderButton.getScene() != null) { // Fallback
                 dialogStage.initOwner(addOrderButton.getScene().getWindow());
            }


            Scene scene = new Scene(parent);
            dialogStage.setScene(scene);

            controller.setDialogStage(dialogStage); // Allow controller to close itself

            dialogStage.showAndWait();

            // Refresh list if an order was saved
            if (controller.isSaved()) {
                handleSearchOrdersAction(null);
            }

        } catch (IOException e) {
            logger.error("Failed to load Sales Order Form dialog: {}", e.getMessage(), e);
            AlertUtil.showError("UI Error", "Could not open the sales order form.");
        }
    }

    /**
     * Called by MainFrameController or similar to load this view.
     */
    public void loadInitialData() {
        if (salesOrderService != null) {
            handleSearchOrdersAction(null);
        } else {
            logger.warn("SalesOrderService is null. Cannot load initial data for SalesOrderListView.");
        }
    }
}
