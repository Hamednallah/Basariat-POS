package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;
import com.basariatpos.model.UserDTO; // For createdByUserId
import com.basariatpos.service.InventoryItemService;
import com.basariatpos.service.PurchaseOrderService;
import com.basariatpos.service.UserSessionService;
import com.basariatpos.service.exception.*; // All PO related exceptions
import com.basariatpos.util.AppLogger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class PurchaseOrderFormController {

    private static final Logger logger = AppLogger.getLogger(PurchaseOrderFormController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private TextField supplierNameField;
    @FXML private DatePicker orderDateField;
    @FXML private ComboBox<String> statusComboBox; // "Pending", "Partial", "Received", "Cancelled"
    @FXML private Label poIdValueLabel;
    @FXML private Label poIdLabel;


    @FXML private TableView<PurchaseOrderItemDTO> poItemsTable;
    @FXML private TableColumn<PurchaseOrderItemDTO, InventoryItemDTO> inventoryItemColumn;
    @FXML private TableColumn<PurchaseOrderItemDTO, Integer> qtyOrderedColumn;
    @FXML private TableColumn<PurchaseOrderItemDTO, BigDecimal> pricePerUnitColumn;
    @FXML private TableColumn<PurchaseOrderItemDTO, BigDecimal> subtotalColumn;
    @FXML private TableColumn<PurchaseOrderItemDTO, Integer> qtyReceivedColumn;

    @FXML private Button addItemButton;
    @FXML private Button removeItemButton;
    @FXML private Button savePOButton;
    @FXML private Button cancelButton;
    @FXML private Label grandTotalLabel;
    @FXML private BorderPane poFormRootPane; // For RTL

    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0.00");


    private Stage dialogStage;
    private PurchaseOrderService poService;
    private InventoryItemService inventoryItemService;
    private UserSessionService userSessionService;

    private PurchaseOrderDTO currentPO;
    private boolean isEditMode = false;
    private boolean saved = false;

    private ObservableList<PurchaseOrderItemDTO> poItemsList = FXCollections.observableArrayList();
    private ObservableList<InventoryItemDTO> availableInventoryItems = FXCollections.observableArrayList();

    public void initializeDialog(PurchaseOrderService poService, InventoryItemService invService, UserSessionService userSessionService,
                                 Stage stage, PurchaseOrderDTO orderToEdit) {
        this.poService = poService;
        this.inventoryItemService = invService;
        this.userSessionService = userSessionService;
        this.dialogStage = stage;

        updateNodeOrientation();
        loadAvailableInventoryItems();
        setupItemTableColumns();
        setupStatusComboBox();

        if (orderToEdit != null) {
            this.currentPO = orderToEdit; // This should be the fully detailed DTO
            this.isEditMode = true;
            dialogTitleLabel.setText(MessageProvider.getString("po.dialog.edit.title"));
            if(poIdLabel!=null) poIdLabel.setVisible(true); // Null check for safety if FXML changes
            if(poIdValueLabel!=null) poIdValueLabel.setVisible(true);
            populateFormFields();
        } else {
            this.currentPO = new PurchaseOrderDTO(); // For new PO
            this.isEditMode = false;
            dialogTitleLabel.setText(MessageProvider.getString("po.dialog.add.title"));
            orderDateField.setValue(LocalDate.now());
            statusComboBox.setValue("Pending"); // Default status
            if(poIdLabel!=null) poIdLabel.setVisible(false);
            if(poIdValueLabel!=null) poIdValueLabel.setVisible(false);
        }
        poItemsTable.setItems(poItemsList);
        updateGrandTotal();

        removeItemButton.disableProperty().bind(poItemsTable.getSelectionModel().selectedItemProperty().isNull());
    }

    private void updateNodeOrientation() {
        if (poFormRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                poFormRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                poFormRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("poFormRootPane is null. Cannot set RTL/LTR orientation.");
        }
         if (dialogStage != null && dialogStage.getScene() != null && poFormRootPane != null) {
             dialogStage.getScene().setNodeOrientation(poFormRootPane.getNodeOrientation());
        }
    }

    private void loadAvailableInventoryItems() {
        try {
            // Load only active, non-service items suitable for purchasing
            List<InventoryItemDTO> activeItems = inventoryItemService.getAllInventoryItems(false)
                .stream().filter(item -> !item.isService()).collect(Collectors.toList());
            availableInventoryItems.setAll(activeItems);
        } catch (InventoryItemServiceException e) {
            logger.error("Failed to load inventory items for PO form: {}", e.getMessage(), e);
            showErrorAlert("Load Error", "Could not load inventory items: " + e.getMessage());
        }
    }

    private void setupStatusComboBox() {
        // TODO: Localize status strings if needed, or ensure they match DB enum/values exactly
        statusComboBox.setItems(FXCollections.observableArrayList("Pending", "Partial", "Received", "Cancelled"));
    }


    private void setupItemTableColumns() {
        inventoryItemColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(findInventoryItemById(cellData.getValue().getInventoryItemId())));
        inventoryItemColumn.setCellFactory(col -> new ComboBoxTableCellPOItem(availableInventoryItems));
        inventoryItemColumn.setOnEditCommit(event -> {
            PurchaseOrderItemDTO item = event.getRowValue();
            InventoryItemDTO selectedInvItem = event.getNewValue();
            if (selectedInvItem != null) {
                item.setInventoryItemId(selectedInvItem.getInventoryItemId());
                item.setInventoryItemProductCode(selectedInvItem.getProductCode()); // Assuming DTO has this
                item.setInventoryItemProductNameEn(selectedInvItem.getProductNameEn());
                item.setInventoryItemSpecificNameEn(selectedInvItem.getItemSpecificNameEn());
                item.setInventoryItemUnitOfMeasure(selectedInvItem.getUnitOfMeasure());
                item.setPurchasePricePerUnit(selectedInvItem.getCostPrice() !=null ? selectedInvItem.getCostPrice() : BigDecimal.ZERO); // Default to cost price
            }
            updateItemSubtotal(item);
            poItemsTable.refresh();
            updateGrandTotal();
        });


        qtyOrderedColumn.setCellValueFactory(new PropertyValueFactory<>("quantityOrdered"));
        qtyOrderedColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        qtyOrderedColumn.setOnEditCommit(event -> {
            PurchaseOrderItemDTO item = event.getRowValue();
            item.setQuantityOrdered(event.getNewValue() != null && event.getNewValue() >=0 ? event.getNewValue() : 0);
            updateItemSubtotal(item);
            poItemsTable.refresh();
            updateGrandTotal();
        });

        pricePerUnitColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePricePerUnit"));
        pricePerUnitColumn.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        pricePerUnitColumn.setOnEditCommit(event -> {
            PurchaseOrderItemDTO item = event.getRowValue();
            item.setPurchasePricePerUnit(event.getNewValue() != null && event.getNewValue().compareTo(BigDecimal.ZERO) >=0 ? event.getNewValue() : BigDecimal.ZERO);
            updateItemSubtotal(item);
            poItemsTable.refresh();
            updateGrandTotal();
        });

        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        subtotalColumn.setCellFactory(tc -> new TableCell<>() {
             @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(currencyFormatter.format(item));
            }
        });
        qtyReceivedColumn.setCellValueFactory(new PropertyValueFactory<>("quantityReceived"));
    }

    private InventoryItemDTO findInventoryItemById(int id) {
        return availableInventoryItems.stream().filter(i -> i.getInventoryItemId() == id).findFirst().orElse(null);
    }

    private void updateItemSubtotal(PurchaseOrderItemDTO item) {
        if (item.getPurchasePricePerUnit() != null && item.getQuantityOrdered() > 0) {
            item.setSubtotal(item.getPurchasePricePerUnit().multiply(new BigDecimal(item.getQuantityOrdered())));
        } else {
            item.setSubtotal(BigDecimal.ZERO);
        }
    }

    private void updateGrandTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItemDTO item : poItemsList) {
            if (item.getSubtotal() != null) {
                total = total.add(item.getSubtotal());
            }
        }
        currentPO.setTotalAmount(total); // Update the DTO directly
        grandTotalLabel.setText("Total: " + currencyFormatter.format(total));
    }


    private void populateFormFields() {
        if (currentPO != null) {
            poIdValueLabel.setText(String.valueOf(currentPO.getPurchaseOrderId()));
            supplierNameField.setText(currentPO.getSupplierName());
            orderDateField.setValue(currentPO.getOrderDate());
            statusComboBox.setValue(currentPO.getStatus());
            poItemsList.setAll(currentPO.getItems()); // Assuming items are loaded
        }
    }

    @FXML
    private void handleAddItemButtonAction(ActionEvent event) {
        PurchaseOrderItemDTO newItem = new PurchaseOrderItemDTO();
        newItem.setPurchaseOrderId(currentPO.getPurchaseOrderId()); // Link to current PO if ID exists
        newItem.setQuantityOrdered(1); // Default quantity
        newItem.setPurchasePricePerUnit(BigDecimal.ZERO); // Default price
        newItem.setQuantityReceived(0);
        updateItemSubtotal(newItem);
        poItemsList.add(newItem);
        poItemsTable.getSelectionModel().select(newItem);
        poItemsTable.edit(poItemsTable.getItems().size() - 1, inventoryItemColumn); // Start editing new row
        updateGrandTotal();
    }

    @FXML
    private void handleRemoveItemButtonAction(ActionEvent event) {
        PurchaseOrderItemDTO selectedItem = poItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            poItemsList.remove(selectedItem);
            updateGrandTotal();
        } else {
            showErrorAlert("No Item Selected", MessageProvider.getString("po.error.itemNotSelected"));
        }
    }


    @FXML
    private void handleSavePurchaseOrderButtonAction(ActionEvent event) {
        if (!validateInput()) return;

        currentPO.setSupplierName(supplierNameField.getText());
        currentPO.setOrderDate(orderDateField.getValue());
        currentPO.setStatus(statusComboBox.getValue());
        currentPO.setItems(new ArrayList<>(poItemsList)); // Ensure current list is set
        // Total amount updated by updateGrandTotal()

        try {
            if (isEditMode) {
                // For edit, typically we update header, and items are saved individually or through a batch update.
                // The current repository saveNewOrderWithItems is for new orders.
                // We might need a different service method for full update, or update header then items.
                // For simplicity, if we allow full resave of items:
                poService.updatePurchaseOrderHeader(currentPO); // Update header
                for(PurchaseOrderItemDTO item : currentPO.getItems()){ // Save each item
                    item.setPurchaseOrderId(currentPO.getPurchaseOrderId()); // Ensure link
                    poService.addOrUpdateItemOnOrder(item);
                }
                 savedProduct = currentPO; // Assuming updateOrderHeader returns the updated DTO
            } else {
                 UserDTO currentUser = userSessionService.getCurrentUser();
                 if (currentUser == null) throw new PurchaseOrderException("User session not found for creating PO.");
                 currentPO.setCreatedByUserId(currentUser.getUserId());
                 currentPO.setCreatedByName(currentUser.getFullName()); // Or username
                savedProduct = poService.createNewPurchaseOrder(currentPO);
            }
            saved = true;
            closeDialog();
        } catch (PurchaseOrderValidationException | CategoryNotFoundException | ProductNotFoundException | InventoryItemNotFoundException e) {
            showValidationErrorAlert(e instanceof PurchaseOrderValidationException ? ((PurchaseOrderValidationException)e).getErrors() : List.of(e.getMessage()));
        } catch (PurchaseOrderException e) {
            showErrorAlert(MessageProvider.getString("po.error.generic"), e.getMessage());
        }
    }

    private PurchaseOrderDTO savedProduct; // To hold result for parent controller
    public PurchaseOrderDTO getSavedPurchaseOrder() { return savedProduct; }


    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();
        if (supplierNameField.getText() == null || supplierNameField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("po.validation.supplierRequired"));
        }
        if (orderDateField.getValue() == null) {
            errors.add("Order date is required."); // TODO: i18n
        }
        if (poItemsList.isEmpty()) {
            errors.add(MessageProvider.getString("po.validation.itemRequired"));
        }
        for (PurchaseOrderItemDTO item : poItemsList) {
            if (item.getInventoryItemId() <= 0) errors.add("An inventory item must be selected for all rows.");
            if (item.getQuantityOrdered() <= 0) errors.add(MessageProvider.getString("po.validation.qtyOrderedPositive") + " for item: " + item.getInventoryItemDisplayFullName());
            if (item.getPurchasePricePerUnit() == null || item.getPurchasePricePerUnit().compareTo(BigDecimal.ZERO) < 0) {
                errors.add(MessageProvider.getString("po.validation.pricePositive") + " for item: " + item.getInventoryItemDisplayFullName());
            }
        }

        if (!errors.isEmpty()) {
            showValidationErrorAlert(errors);
            return false;
        }
        return true;
    }

    private void showErrorAlert(String title, String content) { /* ... */ }
    private void showValidationErrorAlert(List<String> errors) { /* ... */ }
    public boolean isSaved() { return saved; }
    private void closeDialog() { if (dialogStage != null) dialogStage.close(); }
}
