package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.service.PurchaseOrderService;
import com.basariatpos.service.exception.PurchaseOrderException;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PurchaseOrderListController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(PurchaseOrderListController.class);

    @FXML private TableView<PurchaseOrderDTO> poTable;
    @FXML private TableColumn<PurchaseOrderDTO, Integer> poIdColumn;
    @FXML private TableColumn<PurchaseOrderDTO, String> orderDateColumn;
    @FXML private TableColumn<PurchaseOrderDTO, String> supplierColumn;
    @FXML private TableColumn<PurchaseOrderDTO, String> statusColumn;
    @FXML private TableColumn<PurchaseOrderDTO, BigDecimal> totalAmountColumn;
    @FXML private TableColumn<PurchaseOrderDTO, String> createdByColumn;

    @FXML private Button addPOButton;
    @FXML private Button viewEditPOButton;
    @FXML private Button receiveStockButton;
    @FXML private BorderPane poListRootPane; // For RTL

    private PurchaseOrderService poService;
    private final ObservableList<PurchaseOrderDTO> poObservableList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.poService = AppLauncher.getPurchaseOrderService();
        if (this.poService == null) {
            logger.error("PurchaseOrderService is null. Cannot perform operations.");
            showErrorAlert("Critical Error", "Purchase Order Service is not available.");
            addPOButton.setDisable(true);
            viewEditPOButton.setDisable(true);
            receiveStockButton.setDisable(true);
            return;
        }
        setupTableColumns();
        loadPurchaseOrders();

        poTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean itemSelected = newSel != null;
            viewEditPOButton.setDisable(!itemSelected);
            // Enable receive stock only if status is Pending or Partial
            receiveStockButton.setDisable(!itemSelected ||
                (newSel != null && ("Received".equalsIgnoreCase(newSel.getStatus()) || "Cancelled".equalsIgnoreCase(newSel.getStatus())) )
            );
        });
        updateNodeOrientation(); // Call after other initializations
        logger.info("PurchaseOrderListController initialized.");
    }

    public void setPurchaseOrderService(PurchaseOrderService service) {
        this.poService = service;
        if(poTable != null) loadPurchaseOrders();
    }

    // Method to allow MainFrameController to set the stage if this view is loaded into it
    public void setStage(Stage stage) {
        // this.currentStage = stage; // If needed for dialog ownership directly from here
        updateNodeOrientation(); // Re-apply orientation if stage context changes things
    }

    private void updateNodeOrientation() {
        if (poListRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                poListRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                poListRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("poListRootPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    private void setupTableColumns() {
        poIdColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseOrderId"));
        orderDateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getOrderDate().format(dateFormatter))
        );
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        statusColumn.setCellValueFactory(cellData -> {
            String statusKey = "po.status." + cellData.getValue().getStatus().toLowerCase();
            return new SimpleStringProperty(MessageProvider.getString(statusKey, cellData.getValue().getStatus()));
        });
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        createdByColumn.setCellValueFactory(new PropertyValueFactory<>("createdByName"));
    }

    private void loadPurchaseOrders() {
        try {
            List<PurchaseOrderDTO> pos = poService.getAllPurchaseOrderSummaries();
            poObservableList.setAll(pos);
            poTable.setItems(poObservableList);
            logger.info("Purchase order summaries loaded. Count: {}", pos.size());
        } catch (PurchaseOrderException e) {
            logger.error("Failed to load PO summaries: {}", e.getMessage(), e);
            showErrorAlert("Load Error", "Could not load purchase orders: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddPOButtonAction(ActionEvent event) {
        showPurchaseOrderFormDialog(null);
    }

    @FXML
    private void handleViewEditPOButtonAction(ActionEvent event) {
        PurchaseOrderDTO selectedPO = poTable.getSelectionModel().getSelectedItem();
        if (selectedPO == null) {
            showErrorAlert("No Selection", "Please select a purchase order to view/edit.");
            return;
        }
        // Fetch full details before opening form
        try {
            Optional<PurchaseOrderDTO> fullPoOpt = poService.getPurchaseOrderDetails(selectedPO.getPurchaseOrderId());
            if(fullPoOpt.isPresent()){
                showPurchaseOrderFormDialog(fullPoOpt.get());
            } else {
                 showErrorAlert("Error", "Could not load details for PO ID: " + selectedPO.getPurchaseOrderId());
            }
        } catch (PurchaseOrderException e){
            showErrorAlert("Error Loading PO", e.getMessage());
        }
    }

    private void showPurchaseOrderFormDialog(PurchaseOrderDTO poToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/PurchaseOrderFormView.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            PurchaseOrderFormController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(poToEdit == null ?
                                 MessageProvider.getString("po.dialog.add.title") :
                                 MessageProvider.getString("po.dialog.edit.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner((Stage) addPOButton.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogRoot));

            controller.initializeDialog(AppLauncher.getPurchaseOrderService(), AppLauncher.getInventoryItemService(), AppLauncher.getUserSessionService(), dialogStage, poToEdit);

            dialogStage.showAndWait();
            loadPurchaseOrders(); // Refresh list after dialog closes

        } catch (IOException e) {
            logger.error("Failed to load PurchaseOrderFormView.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the purchase order form.");
        }
    }


    @FXML
    private void handleReceiveStockButtonAction(ActionEvent event) {
        PurchaseOrderDTO selectedPO = poTable.getSelectionModel().getSelectedItem();
        if (selectedPO == null) {
            showErrorAlert("No Selection", "Please select a purchase order to receive stock.");
            return;
        }
        if ("Received".equalsIgnoreCase(selectedPO.getStatus()) || "Cancelled".equalsIgnoreCase(selectedPO.getStatus())) {
            showInfoAlert("Cannot Receive Stock", "This PO is already '" + selectedPO.getStatus() + "' and cannot receive more stock.");
            return;
        }

        try {
            // Fetch full PO details including items before showing dialog
            Optional<PurchaseOrderDTO> fullPoOpt = poService.getPurchaseOrderDetails(selectedPO.getPurchaseOrderId());
            if (fullPoOpt.isEmpty()) {
                 showErrorAlert("Error", "Could not load details for PO ID: " + selectedPO.getPurchaseOrderId());
                 return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/StockReceivingDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            StockReceivingDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("po.dialog.receive.title", String.valueOf(selectedPO.getPurchaseOrderId())));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner((Stage) receiveStockButton.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogRoot));

            controller.initializeDialog(AppLauncher.getPurchaseOrderService(), dialogStage, fullPoOpt.get());

            dialogStage.showAndWait();
            loadPurchaseOrders(); // Refresh list to show updated status/totals

        } catch (IOException e) {
            logger.error("Failed to load StockReceivingDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the stock receiving dialog.");
        } catch (PurchaseOrderException e){
            logger.error("Error preparing stock receiving for PO ID {}: {}", selectedPO.getPurchaseOrderId(), e.getMessage(), e);
            showErrorAlert("Error", "Could not prepare stock receiving: " + e.getMessage());
        }
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner((Stage) poTable.getScene().getWindow());
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner((Stage) poTable.getScene().getWindow());
        alert.showAndWait();
    }
}
