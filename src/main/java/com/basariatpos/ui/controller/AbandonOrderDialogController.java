package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.SalesOrderItemDTO;
import com.basariatpos.service.SalesOrderService;
import com.basariatpos.service.exception.NoActiveShiftException;
import com.basariatpos.service.exception.PermissionDeniedException;
import com.basariatpos.service.exception.SalesOrderNotFoundException;
import com.basariatpos.service.exception.SalesOrderValidationException;
import com.basariatpos.ui.utilui.AlertUtil;
import com.basariatpos.util.AppLogger;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class AbandonOrderDialogController {

    private static final Logger logger = AppLogger.getLogger(AbandonOrderDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private Label orderInfoLabel;
    @FXML private TableView<SalesOrderItemWrapper> itemsTable;
    @FXML private TableColumn<SalesOrderItemWrapper, String> itemNameColumn;
    @FXML private TableColumn<SalesOrderItemWrapper, Integer> itemQuantityColumn;
    @FXML private TableColumn<SalesOrderItemWrapper, Boolean> restockItemColumn;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;
    @FXML private BorderPane abandonOrderDialogPane; // For RTL

    private SalesOrderService salesOrderService;
    private Stage dialogStage;
    private SalesOrderDTO orderToAbandon;
    private boolean abandonConfirmed = false;

    private ObservableList<SalesOrderItemWrapper> itemWrappers = FXCollections.observableArrayList();

    public void initialize() {
        dialogTitleLabel.setText(MessageProvider.getString("abandonorder.dialog.title", "")); // Placeholder

        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemDescriptionForDisplay"));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        restockItemColumn.setCellValueFactory(cellData -> cellData.getValue().restockProperty());
        restockItemColumn.setCellFactory(column -> {
            CheckBoxTableCell<SalesOrderItemWrapper, Boolean> cell = new CheckBoxTableCell<>(index -> {
                // This callback provides the index of the row for which the checkbox is being created.
                // We use this to determine if the checkbox should be disabled.
                SalesOrderItemWrapper wrapper = itemsTable.getItems().get(index);
                return wrapper.restockProperty(); // Return the boolean property itself
            });

            // To disable checkboxes, we need to customize the cell.
            // CheckBoxTableCell doesn't have a direct "disableIf" callback.
            // We can override updateItem.
            return new CheckBoxTableCell<SalesOrderItemWrapper, Boolean>(
                index -> itemsTable.getItems().get(index).restockProperty()
            ) {
                @Override
                public void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setDisable(true);
                        setGraphic(null);
                    } else {
                        SalesOrderItemWrapper currentWrapper = (SalesOrderItemWrapper) getTableRow().getItem();
                        // Only enable checkbox if it's a stockable item
                        boolean isStockable = currentWrapper.getOrderItem().getInventoryItemId() != null &&
                                              currentWrapper.getOrderItem().getInventoryItemId() > 0;
                        setDisable(!isStockable);
                        if (!isStockable) {
                            // Clear the checkbox if not stockable, and ensure property is false
                            currentWrapper.setRestock(false);
                        }
                    }
                }
            };
        });
        restockItemColumn.setEditable(true);
        itemsTable.setEditable(true);
    }

    public void initializeDialog(SalesOrderDTO order, SalesOrderService service, Stage stage) {
        this.orderToAbandon = order;
        this.salesOrderService = service;
        this.dialogStage = stage;

        dialogTitleLabel.setText(MessageProvider.getString("abandonorder.dialog.title", String.valueOf(order.getSalesOrderId())));
        orderInfoLabel.setText(
            String.format("Order ID: %d | Patient: %s | Status: %s | Total: %.2f",
                order.getSalesOrderId(),
                order.getPatientFullName() != null ? order.getPatientFullName() : "N/A",
                order.getStatus(),
                order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO
            )
        );

        itemWrappers.clear();
        if (order.getItems() != null) {
            for (SalesOrderItemDTO item : order.getItems()) {
                itemWrappers.add(new SalesOrderItemWrapper(item));
            }
        }
        itemsTable.setItems(itemWrappers);
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (abandonOrderDialogPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                abandonOrderDialogPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                abandonOrderDialogPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("abandonOrderDialogPane is null. Cannot set RTL/LTR orientation.");
        }
        if (dialogStage != null && dialogStage.getScene() != null && abandonOrderDialogPane != null) {
             dialogStage.getScene().setNodeOrientation(abandonOrderDialogPane.getNodeOrientation());
        }
    }


    @FXML
    void handleConfirmAbandonmentButtonAction(ActionEvent event) {
        List<Integer> itemIdsToRestock = itemWrappers.stream()
            .filter(SalesOrderItemWrapper::isRestock)
            .map(wrapper -> wrapper.getOrderItem().getSoItemId())
            .collect(Collectors.toList());

        try {
            salesOrderService.abandonOrder(orderToAbandon.getSalesOrderId(), itemIdsToRestock);
            AlertUtil.showSuccess(
                MessageProvider.getString("abandonorder.success.title"),
                MessageProvider.getString("abandonorder.success.message", String.valueOf(orderToAbandon.getSalesOrderId()))
            );
            abandonConfirmed = true;
            closeDialog();
        } catch (SalesOrderNotFoundException e) {
            logger.error("Order not found while trying to abandon: {}", orderToAbandon.getSalesOrderId(), e);
            AlertUtil.showError(MessageProvider.getString("abandonorder.error.title"), e.getMessage());
        } catch (SalesOrderValidationException e) {
            logger.warn("Validation error abandoning order {}: {}", orderToAbandon.getSalesOrderId(), e.getErrors().toString(), e);
            AlertUtil.showError(MessageProvider.getString("abandonorder.error.title"), String.join("\n", e.getErrors()));
        } catch (PermissionDeniedException e) {
            logger.warn("Permission denied for abandoning order {}: {}", orderToAbandon.getSalesOrderId(), e.getMessage(), e);
            AlertUtil.showError(MessageProvider.getString("abandonorder.error.title"), e.getMessage());
        } catch (NoActiveShiftException e) {
            logger.warn("No active shift for abandoning order {}: {}", orderToAbandon.getSalesOrderId(), e.getMessage(), e);
            AlertUtil.showError(MessageProvider.getString("abandonorder.error.title"), e.getMessage());
        } catch (Exception e) {
            logger.error("Generic error abandoning order {}: {}", orderToAbandon.getSalesOrderId(), e.getMessage(), e);
            AlertUtil.showError(MessageProvider.getString("abandonorder.error.title"), MessageProvider.getString("abandonorder.error.generic"));
        }
    }

    @FXML
    void handleCancelButtonAction(ActionEvent event) {
        closeDialog();
    }

    public boolean isAbandonConfirmed() {
        return abandonConfirmed;
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // Wrapper class to add a boolean property for the checkbox
    public static class SalesOrderItemWrapper {
        private final SalesOrderItemDTO orderItem;
        private final SimpleBooleanProperty restock;

        public SalesOrderItemWrapper(SalesOrderItemDTO orderItem) {
            this.orderItem = orderItem;
            // Default restock to true if it's a stockable item, false otherwise.
            boolean isStockable = orderItem.getInventoryItemId() != null && orderItem.getInventoryItemId() > 0;
            this.restock = new SimpleBooleanProperty(isStockable);
        }

        public SalesOrderItemDTO getOrderItem() {
            return orderItem;
        }

        public String getItemDescriptionForDisplay() {
            // Logic from SalesOrderFormDialogController for display name
            if (orderItem.isCustomLenses()) {
                 return orderItem.getDescription() != null && !orderItem.getDescription().isEmpty() ?
                        orderItem.getDescription() :
                        MessageProvider.getString("salesorder.itemtype.customlens");
            } else if (MessageProvider.getString("salesorder.itemtype.customquote").equals(orderItem.getItemTypeDisplay())) {
                return orderItem.getDescription() != null && !orderItem.getDescription().isEmpty() ?
                       orderItem.getDescription() :
                       MessageProvider.getString("salesorder.itemtype.customquote");
            } else if (orderItem.getItemDisplaySpecificNameEn() != null && !orderItem.getItemDisplaySpecificNameEn().isEmpty()) {
                return (orderItem.getItemDisplayNameEn() != null ? orderItem.getItemDisplayNameEn() : "") +
                       " - " + orderItem.getItemDisplaySpecificNameEn();
            } else if (orderItem.getItemDisplayNameEn() != null) {
                return orderItem.getItemDisplayNameEn();
            }
            return orderItem.getDescription() != null ? orderItem.getDescription() : "N/A";
        }

        public int getQuantity() {
            return orderItem.getQuantity();
        }

        public boolean isRestock() {
            return restock.get();
        }

        public void setRestock(boolean restock) {
            // Only allow setting restock to true if it's a stockable item
            boolean isStockable = orderItem.getInventoryItemId() != null && orderItem.getInventoryItemId() > 0;
            if (isStockable) {
                this.restock.set(restock);
            } else {
                this.restock.set(false); // Force false if not stockable
            }
        }

        public SimpleBooleanProperty restockProperty() {
            return restock;
        }
    }
}
