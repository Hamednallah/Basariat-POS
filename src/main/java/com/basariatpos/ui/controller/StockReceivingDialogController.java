package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;
import com.basariatpos.service.PurchaseOrderService;
import com.basariatpos.service.exception.*; // Assuming all relevant exceptions
import com.basariatpos.util.AppLogger;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StockReceivingDialogController {

    private static final Logger logger = AppLogger.getLogger(StockReceivingDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private Label poDetailsLabel;
    @FXML private TableView<PurchaseOrderItemDTO> itemsToReceiveTable;
    @FXML private TableColumn<PurchaseOrderItemDTO, String> itemNameColumn;
    @FXML private TableColumn<PurchaseOrderItemDTO, Integer> qtyOrderedColumn;
    @FXML private TableColumn<PurchaseOrderItemDTO, Integer> qtyAlreadyReceivedColumn;
    @FXML private TableColumn<PurchaseOrderItemDTO, Integer> qtyRemainingColumn;
    @FXML private TableColumn<PurchaseOrderItemDTO, Integer> qtyToReceiveNowColumn;
    @FXML private TableColumn<PurchaseOrderItemDTO, BigDecimal> newPricePerUnitColumn;

    @FXML private Button confirmReceptionButton;
    @FXML private Button cancelButton;
    @FXML private BorderPane stockReceivingDialogRootPane; // For RTL

    private Stage dialogStage;
    private PurchaseOrderService poService;
    private PurchaseOrderDTO currentPO;
    private ObservableList<PurchaseOrderItemDTO> itemsToReceiveList = FXCollections.observableArrayList();
    private boolean receptionConfirmed = false;

    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0.00");

    public void initializeDialog(PurchaseOrderService poService, Stage stage, PurchaseOrderDTO poData) {
        this.poService = poService;
        this.dialogStage = stage;
        this.currentPO = poData;

        dialogTitleLabel.setText(MessageProvider.getString("po.dialog.receive.title", String.valueOf(poData.getPurchaseOrderId())));
        poDetailsLabel.setText("Supplier: " + poData.getSupplierName() + " | Order Date: " + poData.getOrderDate());

        setupTableColumns();
        // Filter items that are not fully received yet
        itemsToReceiveList.setAll(
            poData.getItems().stream()
                  .filter(item -> item.getQuantityOrdered() > item.getQuantityReceived())
                  .collect(Collectors.toList())
        );
        itemsToReceiveTable.setItems(itemsToReceiveList);
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (stockReceivingDialogRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                stockReceivingDialogRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                stockReceivingDialogRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("stockReceivingDialogRootPane is null. Cannot set RTL/LTR orientation.");
        }
        if (dialogStage != null && dialogStage.getScene() != null && stockReceivingDialogRootPane != null) {
             dialogStage.getScene().setNodeOrientation(stockReceivingDialogRootPane.getNodeOrientation());
        }
    }

    private void setupTableColumns() {
        itemNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInventoryItemDisplayFullName()));
        qtyOrderedColumn.setCellValueFactory(new PropertyValueFactory<>("quantityOrdered"));
        qtyAlreadyReceivedColumn.setCellValueFactory(new PropertyValueFactory<>("quantityReceived"));

        qtyRemainingColumn.setCellValueFactory(cellData -> {
            int remaining = cellData.getValue().getQuantityOrdered() - cellData.getValue().getQuantityReceived();
            return new SimpleIntegerProperty(remaining).asObject();
        });

        qtyToReceiveNowColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(0)); // Default to 0
        qtyToReceiveNowColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        qtyToReceiveNowColumn.setOnEditCommit(event -> {
            PurchaseOrderItemDTO item = event.getRowValue();
            int qtyOrdered = item.getQuantityOrdered();
            int qtyAlreadyReceived = item.getQuantityReceived();
            int qtyRemaining = qtyOrdered - qtyAlreadyReceived;
            Integer newValue = event.getNewValue();

            if (newValue == null || newValue < 0) {
                newValue = 0;
            } else if (newValue > qtyRemaining) {
                newValue = qtyRemaining; // Cap at remaining
                 showErrorAlert("Validation Error", MessageProvider.getString("po.stockreceive.error.qtyExceeds",
                    String.valueOf(event.getNewValue()), item.getInventoryItemDisplayFullName(), String.valueOf(qtyRemaining)));
            }
            // This is tricky: how to store this temporary "to receive" value?
            // For now, we can process it directly in handleConfirmReception or add a transient field to DTO.
            // Let's assume we process it from the table directly.
            // To make the cell reflect the change immediately if capped:
            ((SimpleObjectProperty<Integer>) event.getTableColumn().getCellObservableValue(event.getRowValue())).set(newValue);
            itemsToReceiveTable.refresh(); // Refresh to show capped value if necessary
        });

        newPricePerUnitColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePricePerUnit")); // Show current price
        newPricePerUnitColumn.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        newPricePerUnitColumn.setOnEditCommit(event -> {
            PurchaseOrderItemDTO item = event.getRowValue();
            BigDecimal newPrice = event.getNewValue();
            if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
                // Revert to old value if invalid, show error
                 showErrorAlert("Validation Error", MessageProvider.getString("po.stockreceive.error.priceInvalid", item.getInventoryItemDisplayFullName()));
                ((SimpleObjectProperty<BigDecimal>) event.getTableColumn().getCellObservableValue(event.getRowValue())).set(event.getOldValue());
                itemsToReceiveTable.refresh();
            } else {
                item.setPurchasePricePerUnit(newPrice); // Temporarily update DTO for submission
            }
        });
    }

    @FXML
    private void handleConfirmReceptionButtonAction(ActionEvent event) {
        List<String> errors = new ArrayList<>();
        boolean itemsToReceiveExist = false;

        for (int i = 0; i < itemsToReceiveTable.getItems().size(); i++) {
            PurchaseOrderItemDTO item = itemsToReceiveTable.getItems().get(i);
            // Get the edited value from the cell for qtyToReceiveNowColumn
            TableColumn<PurchaseOrderItemDTO, Integer> qtyReceiveCol = qtyToReceiveNowColumn;
            Integer qtyToReceiveNow = (Integer) qtyReceiveCol.getCellObservableValue(item).getValue();
             if(qtyToReceiveNow == null) qtyToReceiveNow = 0; // if cell was not touched, default to 0

            // Get the edited value from the cell for newPricePerUnitColumn
            TableColumn<PurchaseOrderItemDTO, BigDecimal> priceCol = newPricePerUnitColumn;
            BigDecimal newPrice = (BigDecimal) priceCol.getCellObservableValue(item).getValue();
            if (newPrice == null) newPrice = item.getPurchasePricePerUnit(); // Use original if not changed


            if (qtyToReceiveNow > 0) {
                itemsToReceiveExist = true;
                if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(MessageProvider.getString("po.stockreceive.error.priceInvalid", item.getInventoryItemDisplayFullName()));
                }
                int qtyRemaining = item.getQuantityOrdered() - item.getQuantityReceived();
                if (qtyToReceiveNow > qtyRemaining) {
                     errors.add(MessageProvider.getString("po.stockreceive.error.qtyExceeds",
                        String.valueOf(qtyToReceiveNow), item.getInventoryItemDisplayFullName(), String.valueOf(qtyRemaining)));
                }
            } else if (qtyToReceiveNow < 0) {
                 errors.add(MessageProvider.getString("po.stockreceive.error.qtyInvalid", item.getInventoryItemDisplayFullName()));
            }
        }

        if (!itemsToReceiveExist && errors.isEmpty()) { // No items entered for reception
            errors.add("Please enter quantities for at least one item to receive."); // TODO: i18n
        }

        if (!errors.isEmpty()) {
            showValidationErrorAlert(errors);
            return;
        }

        // Process reception
        try {
            for (int i = 0; i < itemsToReceiveTable.getItems().size(); i++) {
                PurchaseOrderItemDTO item = itemsToReceiveTable.getItems().get(i);
                Integer qtyToReceiveNow = (Integer) qtyToReceiveNowColumn.getCellObservableValue(item).getValue();
                if(qtyToReceiveNow == null) qtyToReceiveNow = 0;

                BigDecimal newPrice = (BigDecimal) newPricePerUnitColumn.getCellObservableValue(item).getValue();
                 if (newPrice == null) newPrice = item.getPurchasePricePerUnit(); // Use original if not changed in cell


                if (qtyToReceiveNow > 0) {
                    poService.receiveStockForItem(item.getPoItemId(), currentPO.getPurchaseOrderId(), qtyToReceiveNow, newPrice);
                }
            }
            receptionConfirmed = true;
            showSuccessAlert(MessageProvider.getString("po.stockreceive.success", String.valueOf(currentPO.getPurchaseOrderId())));
            closeDialog();
        } catch (Exception e) {
            logger.error("Error during stock reception for PO ID {}: {}", currentPO.getPurchaseOrderId(), e.getMessage(), e);
            showErrorAlert("Stock Reception Error", e.getMessage());
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        receptionConfirmed = false;
        closeDialog();
    }

    public boolean isReceptionConfirmed() { return receptionConfirmed; }

    private void showValidationErrorAlert(List<String> errors) { /* ... as in other controllers ... */ }
    private void showErrorAlert(String title, String content) { /* ... as in other controllers ... */ }
    private void showSuccessAlert(String message) { /* ... as in other controllers ... */ }
    private void closeDialog() { if (dialogStage != null) dialogStage.close(); }
}
