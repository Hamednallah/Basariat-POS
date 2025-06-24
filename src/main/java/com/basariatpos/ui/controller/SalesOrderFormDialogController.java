package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.*;
import com.basariatpos.service.*;
import com.basariatpos.service.exception.*;
import com.basariatpos.ui.utilui.AlertUtil;
import com.basariatpos.ui.utilui.DateTimeUtil;
import com.basariatpos.ui.utilui.DialogUtil;
import com.basariatpos.ui.utilui.TextFormatters;
import com.basariatpos.util.AppLogger;
import com.basariatpos.util.DesktopActions;

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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SalesOrderFormDialogController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(SalesOrderFormDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private TextField patientDisplayField;
    @FXML private Button findPatientButton;
    @FXML private DatePicker orderDateField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea remarksArea;
    @FXML private TableView<SalesOrderItemDTO> salesOrderItemsTable;
    @FXML private TableColumn<SalesOrderItemDTO, String> itemTypeColumn;
    @FXML private TableColumn<SalesOrderItemDTO, SalesOrderItemDTO> itemSelectionColumn;
    @FXML private TableColumn<SalesOrderItemDTO, String> itemDescriptionColumn;
    @FXML private TableColumn<SalesOrderItemDTO, Integer> itemQtyColumn;
    @FXML private TableColumn<SalesOrderItemDTO, BigDecimal> itemUnitPriceColumn;
    @FXML private TableColumn<SalesOrderItemDTO, BigDecimal> itemSubtotalColumn;
    @FXML private Button addItemButton;
    @FXML private Button removeItemButton;
    @FXML private Button configureLensButton;
    @FXML private Label subtotalAmountLabel;
    @FXML private TextField discountField;
    @FXML private Label totalAmountLabel;
    @FXML private TextField amountPaidField;
    @FXML private Label balanceDueLabel;
    @FXML private Button notifyOrderReadyButton; // Added
    @FXML private Button saveOrderButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private SalesOrderService salesOrderService;
    private PatientService patientService;
    private InventoryItemService inventoryItemService;
    private ProductService productService;
    private UserSessionService userSessionService;
    private WhatsAppNotificationService whatsAppNotificationService; // Added
    private CenterProfileService centerProfileService; // Added

    private SalesOrderDTO currentOrder;
    private ObservableList<SalesOrderItemDTO> currentOrderItems = FXCollections.observableArrayList();
    private PatientDTO selectedPatient;

    private boolean saved = false;

    private final String ITEM_TYPE_STOCK = MessageProvider.getString("salesorder.itemtype.stock");
    private final String ITEM_TYPE_SERVICE = MessageProvider.getString("salesorder.itemtype.service");
    private final String ITEM_TYPE_CUSTOM_LENS = MessageProvider.getString("salesorder.itemtype.customlens");
    private final String ITEM_TYPE_CUSTOM_QUOTE = MessageProvider.getString("salesorder.itemtype.customquote");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupItemTableColumns();
        salesOrderItemsTable.setItems(currentOrderItems);
        populateStatusComboBox();

        discountField.textProperty().addListener((obs, oldVal, newVal) -> recalculateTotals());
        amountPaidField.textProperty().addListener((obs, oldVal, newVal) -> recalculateTotals());

        TextFormatters.applyBigDecimalFormatter(discountField);
        TextFormatters.applyBigDecimalFormatter(amountPaidField);

        statusComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldStatus, newStatus) -> {
            if (currentOrder != null) {
                currentOrder.setStatus(newStatus);
            }
            updateNotifyButtonState();
        });
    }

    public void setServices(SalesOrderService salesOrderService, PatientService patientService,
                            InventoryItemService inventoryItemService, ProductService productService,
                            UserSessionService userSessionService,
                            WhatsAppNotificationService whatsAppNotificationService,
                            CenterProfileService centerProfileService) {
        this.salesOrderService = salesOrderService;
        this.patientService = patientService;
        this.inventoryItemService = inventoryItemService;
        this.productService = productService;
        this.userSessionService = userSessionService;
        this.whatsAppNotificationService = whatsAppNotificationService;
        this.centerProfileService = centerProfileService;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    public void initializeDialogData(SalesOrderDTO orderToEdit) {
        if (orderToEdit == null) {
            this.currentOrder = new SalesOrderDTO();
            dialogTitleLabel.setText(MessageProvider.getString("salesorder.form.add.title"));
            orderDateField.setValue(LocalDate.now());
            statusComboBox.setValue(this.currentOrder.getStatus());
            selectedPatient = null;
            patientDisplayField.setText("");
            this.currentOrder.setDiscountAmount(BigDecimal.ZERO);
            this.currentOrder.setAmountPaid(BigDecimal.ZERO);
        } else {
            this.currentOrder = orderToEdit; // SalesOrderDTO from list view (summary)
            dialogTitleLabel.setText(MessageProvider.getString("salesorder.form.edit.title", String.valueOf(orderToEdit.getSalesOrderId())));

            // If patient details (phone, opt-in) are not on the summary DTO, fetch them.
            if (this.currentOrder.getPatientId() != null &&
                (this.currentOrder.getPatientPhoneNumber() == null ||
                 this.currentOrder.getPatientFullName() == null || // Check if full name also needs refresh
                 !this.currentOrder.getPatientFullName().equals(orderToEdit.getPatientFullName()) )) { // If summary DTO had a name, but we want full
                 try {
                    Optional<PatientDTO> patientOpt = patientService.getPatientById(this.currentOrder.getPatientId());
                    if (patientOpt.isPresent()) {
                        selectedPatient = patientOpt.get();
                        // Update currentOrder with full patient details
                        this.currentOrder.setPatientFullName(selectedPatient.getFullNameEn());
                        this.currentOrder.setPatientSystemId(selectedPatient.getSystemPatientId());
                        this.currentOrder.setPatientPhoneNumber(selectedPatient.getPhoneNumber());
                        this.currentOrder.setPatientWhatsappOptIn(selectedPatient.isWhatsappOptIn());
                        patientDisplayField.setText(selectedPatient.getDisplayFullNameWithId());
                    } else {
                         patientDisplayField.setText("Patient ID: " + this.currentOrder.getPatientId() + " (Not Found)");
                         this.currentOrder.setPatientPhoneNumber(null); // Clear if patient not found
                         this.currentOrder.setPatientWhatsappOptIn(false);
                    }
                } catch (PatientServiceException e) {
                     patientDisplayField.setText("Error loading patient");
                     logger.error("Error loading patient for order ID {}: {}", this.currentOrder.getSalesOrderId(), e.getMessage());
                     this.currentOrder.setPatientPhoneNumber(null);
                     this.currentOrder.setPatientWhatsappOptIn(false);
                }
            } else if (this.currentOrder.getPatientId() != null) { // Patient ID exists, and summary DTO had details
                 patientDisplayField.setText(this.currentOrder.getPatientFullName() +
                    (this.currentOrder.getPatientSystemId() != null ? " (" + this.currentOrder.getPatientSystemId() + ")" : ""));
                 // Construct a selectedPatient from DTO if needed for consistency
                 selectedPatient = new PatientDTO();
                 selectedPatient.setPatientId(this.currentOrder.getPatientId());
                 selectedPatient.setFullNameEn(this.currentOrder.getPatientFullName());
                 selectedPatient.setSystemPatientId(this.currentOrder.getPatientSystemId());
                 selectedPatient.setPhoneNumber(this.currentOrder.getPatientPhoneNumber());
                 selectedPatient.setWhatsappOptIn(this.currentOrder.isPatientWhatsappOptIn());
            } else { // No patient ID on order
                 patientDisplayField.setText("");
                 this.currentOrder.setPatientPhoneNumber(null);
                 this.currentOrder.setPatientWhatsappOptIn(false);
            }

            orderDateField.setValue(this.currentOrder.getOrderDate().toLocalDate());
            statusComboBox.setValue(this.currentOrder.getStatus());
            remarksArea.setText(this.currentOrder.getRemarks());
            currentOrderItems.setAll(this.currentOrder.getItems());
            discountField.setText(this.currentOrder.getDiscountAmount() != null ? this.currentOrder.getDiscountAmount().toPlainString() : "0.00");
            amountPaidField.setText(this.currentOrder.getAmountPaid() != null ? this.currentOrder.getAmountPaid().toPlainString() : "0.00");
        }

        if (userSessionService != null) {
            boolean canGiveDiscount = userSessionService.hasPermission("CAN_GIVE_DISCOUNT");
            discountField.setEditable(canGiveDiscount);
            discountField.setDisable(!canGiveDiscount);
            if (!canGiveDiscount) {
                discountField.setTooltip(new Tooltip(MessageProvider.getString("salesorder.tooltip.discountNoPermission")));
            } else {
                discountField.setTooltip(null);
            }
        } else {
            discountField.setEditable(false);
            discountField.setDisable(true);
            discountField.setTooltip(new Tooltip("Permission service not available."));
        }

        updateNotifyButtonState();
        recalculateTotals();
    }

    private void updateNotifyButtonState() {
        boolean canNotify = false;
        if (currentOrder != null && "Ready for Pickup".equalsIgnoreCase(currentOrder.getStatus())) {
            if (currentOrder.getPatientId() != null &&
                (currentOrder.getPatientPhoneNumber() == null || currentOrder.getPatientPhoneNumber().trim().isEmpty() ||
                 !currentOrder.isPatientWhatsappOptIn() || // Check opt-in status from DTO
                 (selectedPatient == null || !selectedPatient.getPatientId().equals(currentOrder.getPatientId())) )) {
                try {
                    Optional<PatientDTO> patientOpt = patientService.getPatientById(currentOrder.getPatientId());
                    if (patientOpt.isPresent()) {
                        PatientDTO p = patientOpt.get();
                        // Update currentOrder with the latest patient details
                        currentOrder.setPatientPhoneNumber(p.getPhoneNumber());
                        currentOrder.setPatientWhatsappOptIn(p.isWhatsappOptIn());
                        currentOrder.setPatientFullName(p.getFullNameEn());
                        currentOrder.setPatientSystemId(p.getSystemPatientId());
                        if (selectedPatient == null || !selectedPatient.getPatientId().equals(p.getPatientId())) {
                           selectedPatient = p;
                           patientDisplayField.setText(selectedPatient.getDisplayFullNameWithId());
                        }
                    } else {
                         currentOrder.setPatientPhoneNumber(null);
                         currentOrder.setPatientWhatsappOptIn(false);
                    }
                } catch (PatientServiceException e) {
                    logger.error("Failed to fetch patient details for notify button state update for patient ID {}.", currentOrder.getPatientId(), e);
                    currentOrder.setPatientPhoneNumber(null);
                    currentOrder.setPatientWhatsappOptIn(false);
                }
            }

            if (currentOrder.isPatientWhatsappOptIn() &&
                currentOrder.getPatientPhoneNumber() != null &&
                !currentOrder.getPatientPhoneNumber().trim().isEmpty()) {
                Pattern phonePattern = Pattern.compile("^\\+?[0-9]{7,15}$");
                if (phonePattern.matcher(currentOrder.getPatientPhoneNumber().trim()).matches()) {
                    canNotify = true;
                } else {
                     logger.warn("Cannot enable WhatsApp notification: Phone number '{}' for patient ID {} is invalid.", currentOrder.getPatientPhoneNumber(), currentOrder.getPatientId());
                }
            }
        }
        if (notifyOrderReadyButton != null) {
            notifyOrderReadyButton.setVisible(canNotify);
            notifyOrderReadyButton.setManaged(canNotify);
            notifyOrderReadyButton.setDisable(!canNotify);
        }
    }

    private void setupItemTableColumns() {
        itemTypeColumn.setCellValueFactory(new PropertyValueFactory<>("itemTypeDisplay"));
        itemSelectionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));

        itemDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        itemDescriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        itemDescriptionColumn.setOnEditCommit(event -> {
            SalesOrderItemDTO item = event.getRowValue();
            if (ITEM_TYPE_CUSTOM_QUOTE.equals(item.getItemTypeDisplay()) || item.isCustomLenses()) {
                 item.setDescription(event.getNewValue());
            } else {
                 item.setDescription(event.getNewValue());
            }
            recalculateTotals();
        });

        itemQtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemQtyColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>() {
            @Override public String toString(Integer object) { return object == null ? "0" : object.toString(); }
            @Override public Integer fromString(String string) { try { int val = Integer.parseInt(string); return val > 0 ? val : 0; } catch (NumberFormatException e) { return 0; } }
        }));
        itemQtyColumn.setOnEditCommit(event -> {
            SalesOrderItemDTO item = event.getRowValue();
            item.setQuantity(event.getNewValue() > 0 ? event.getNewValue() : 1);
            recalculateTotals();
        });

        itemUnitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        itemUnitPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<BigDecimal>() {
            @Override public String toString(BigDecimal object) { return object == null ? "0.00" : object.toPlainString(); }
            @Override public BigDecimal fromString(String string) { try { return new BigDecimal(string); } catch (NumberFormatException e) { return BigDecimal.ZERO; } }
        }));
        itemUnitPriceColumn.setOnEditCommit(event -> {
            SalesOrderItemDTO item = event.getRowValue();
            item.setUnitPrice(event.getNewValue().compareTo(BigDecimal.ZERO) >= 0 ? event.getNewValue() : BigDecimal.ZERO);
            recalculateTotals();
        });

        itemSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("itemSubtotal"));

        itemTypeColumn.setCellFactory(javafx.scene.control.cell.ComboBoxTableCell.forTableColumn(
            new StringConverter<String>() {
                @Override public String toString(String object) { return object == null ? "" : object; }
                @Override public String fromString(String string) { return string; }
            },
            FXCollections.observableArrayList(ITEM_TYPE_STOCK, ITEM_TYPE_SERVICE, ITEM_TYPE_CUSTOM_LENS, ITEM_TYPE_CUSTOM_QUOTE)
        ));
        itemTypeColumn.setOnEditCommit(event -> {
            SalesOrderItemDTO item = event.getRowValue();
            String newItemType = event.getNewValue();
            item.setInventoryItemId(null);
            item.setServiceProductId(null);
            item.setPrescriptionDetails(null);
            item.setIsCustomLenses(false);
            item.setItemDisplayNameEn(null);
            item.setItemDisplaySpecificNameEn(null);
            item.setDescription("");

            if (ITEM_TYPE_CUSTOM_LENS.equals(newItemType)) {
                item.setIsCustomLenses(true);
                item.setItemDisplayNameEn(MessageProvider.getString("salesorder.itemtype.customlens"));
                configureLensButton.setVisible(true);
                handleConfigureLensButtonActionForRow(item);
            } else if (ITEM_TYPE_CUSTOM_QUOTE.equals(newItemType)) {
                item.setItemDisplayNameEn(MessageProvider.getString("salesorder.itemtype.customquote"));
                configureLensButton.setVisible(false);
            } else {
                configureLensButton.setVisible(false);
            }
            salesOrderItemsTable.refresh();
            recalculateTotals();
        });

        itemSelectionColumn.setCellValueFactory(cellData -> {
            SalesOrderItemDTO item = cellData.getValue();
            String display = "";
            if (item.isCustomLenses()) {
                display = item.getItemDisplayNameEn() != null ? item.getItemDisplayNameEn() : ITEM_TYPE_CUSTOM_LENS;
            } else if (ITEM_TYPE_CUSTOM_QUOTE.equals(item.getItemTypeDisplay())) {
                 display = ITEM_TYPE_CUSTOM_QUOTE;
            } else if (item.getItemDisplaySpecificNameEn() != null && !item.getItemDisplaySpecificNameEn().isEmpty()) {
                display = item.getItemDisplayNameEn() + " - " + item.getItemDisplaySpecificNameEn();
            } else if (item.getItemDisplayNameEn() != null) {
                display = item.getItemDisplayNameEn();
            }
            return new javafx.beans.property.SimpleStringProperty(display);
        });
    }

    private void populateStatusComboBox() {
        List<String> statuses = Arrays.asList("Pending", "Confirmed", "Processing", "Ready for Pickup", "Completed", "Cancelled", "Abandoned");
        statusComboBox.setItems(FXCollections.observableArrayList(statuses));
    }

    @FXML
    void handleFindPatientButtonAction(ActionEvent event) {
        try {
            PatientDTO foundPatient = DialogUtil.showPatientSearchDialog(this.dialogStage, patientService);
            if (foundPatient != null) {
                this.selectedPatient = foundPatient;
                patientDisplayField.setText(selectedPatient.getDisplayFullNameWithId());
                if(currentOrder != null) {
                    currentOrder.setPatientId(selectedPatient.getPatientId());
                    currentOrder.setPatientFullName(selectedPatient.getFullNameEn());
                    currentOrder.setPatientPhoneNumber(selectedPatient.getPhoneNumber());
                    currentOrder.setPatientWhatsappOptIn(selectedPatient.isWhatsappOptIn());
                    updateNotifyButtonState();
                }
            }
        } catch (IOException e) {
            logger.error("Failed to open patient search dialog: {}", e.getMessage(), e);
            AlertUtil.showError("UI Error", "Could not open patient search.");
        }
    }

    @FXML
    void handleAddItemButtonAction(ActionEvent event) {
        SalesOrderItemDTO newItem = new SalesOrderItemDTO();
        newItem.setQuantity(1);
        newItem.setUnitPrice(BigDecimal.ZERO);
        currentOrderItems.add(newItem);
        salesOrderItemsTable.edit(currentOrderItems.size() -1, itemTypeColumn);
    }

    @FXML
    void handleRemoveItemButtonAction(ActionEvent event) {
        SalesOrderItemDTO selected = salesOrderItemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentOrderItems.remove(selected);
            recalculateTotals();
        } else {
            AlertUtil.showWarning("No Selection", "Please select an item to remove.");
        }
    }

    @FXML
    void handleConfigureLensButtonAction(ActionEvent event) {
        SalesOrderItemDTO selectedItem = salesOrderItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null && (selectedItem.isCustomLenses() || ITEM_TYPE_CUSTOM_LENS.equals(selectedItem.getItemTypeDisplay())) ) {
            handleConfigureLensButtonActionForRow(selectedItem);
        } else {
            AlertUtil.showInfo("Item Type", "Lens configuration is only for 'Custom Lens' items. Please change item type or select a custom lens item.");
        }
    }

    private void handleConfigureLensButtonActionForRow(SalesOrderItemDTO item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/CustomLensConfigDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            DialogPane pane = loader.load();

            CustomLensConfigDialogController controller = loader.getController();
            Stage tempDialogStage = new Stage();
            tempDialogStage.setTitle(MessageProvider.getString("customlens.dialog.title"));
            tempDialogStage.initModality(Modality.WINDOW_MODAL);
            tempDialogStage.initOwner(this.dialogStage);

            controller.initializeDialog(tempDialogStage, item);

            Scene scene = new Scene(pane);
            tempDialogStage.setScene(scene);

            tempDialogStage.showAndWait();

            SalesOrderItemDTO updatedLensItem = controller.getUpdatedLensItem();
            if (updatedLensItem != null) {
                int index = currentOrderItems.indexOf(item);
                if (index != -1) {
                    currentOrderItems.set(index, updatedLensItem);
                } else {
                    currentOrderItems.add(updatedLensItem);
                }
                salesOrderItemsTable.refresh();
                recalculateTotals();
            }
        } catch (IOException e) {
            logger.error("Failed to load Custom Lens Config dialog: {}", e.getMessage(), e);
            AlertUtil.showError("UI Error", "Could not open lens configuration form.");
        }
    }

    private void recalculateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalesOrderItemDTO item : currentOrderItems) {
            BigDecimal itemSub = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            item.setItemSubtotal(itemSub.setScale(2, RoundingMode.HALF_UP));
            subtotal = subtotal.add(item.getItemSubtotal());
        }
        subtotalAmountLabel.setText(subtotal.toPlainString());

        BigDecimal discount = BigDecimal.ZERO;
        try {
            discount = TextFormatters.parseBigDecimal(discountField.getText(), BigDecimal.ZERO);
        } catch (NumberFormatException e) { /* ignore */ }
        if (discount.compareTo(BigDecimal.ZERO) < 0) discount = BigDecimal.ZERO;

        BigDecimal total = subtotal.subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
        totalAmountLabel.setText(total.toPlainString());

        BigDecimal paid = BigDecimal.ZERO;
        try {
            paid = TextFormatters.parseBigDecimal(amountPaidField.getText(), BigDecimal.ZERO);
        } catch (NumberFormatException e) { /* ignore */ }
         if (paid.compareTo(BigDecimal.ZERO) < 0) paid = BigDecimal.ZERO;

        BigDecimal balance = total.subtract(paid);
        balanceDueLabel.setText(balance.toPlainString());

        salesOrderItemsTable.refresh();
    }

    @FXML
    void handleSaveOrderButtonAction(ActionEvent event) {
        List<String> errors = new ArrayList<>();
        if (currentOrderItems.isEmpty()) {
            errors.add(MessageProvider.getString("salesorder.validation.itemRequired"));
        }
        for(SalesOrderItemDTO item : currentOrderItems) {
            boolean isCustomType = ITEM_TYPE_CUSTOM_QUOTE.equals(item.getItemTypeDisplay()) || item.isCustomLenses();
            if(!isCustomType && (item.getInventoryItemId() == null || item.getInventoryItemId() == 0) &&
               (item.getServiceProductId() == null || item.getServiceProductId() == 0)) {
                errors.add("One or more standard items are incomplete. Please select an item/service.");
                break;
            }
            if(item.getQuantity() <= 0) errors.add(MessageProvider.getString("salesorder.validation.qtyPositive") + (item.getItemDisplayNameEn() != null ? " for " + item.getItemDisplayNameEn() : ""));
            if(item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) errors.add(MessageProvider.getString("salesorder.validation.priceNonNegative") + (item.getItemDisplayNameEn() != null ? " for " + item.getItemDisplayNameEn() : ""));
        }

        BigDecimal discount = BigDecimal.ZERO;
        try {
            discount = TextFormatters.parseBigDecimal(discountField.getText(), BigDecimal.ZERO);
            if (discount.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(MessageProvider.getString("salesorder.validation.discountNonNegative"));
            }
            BigDecimal currentSubtotal = BigDecimal.ZERO;
            for (SalesOrderItemDTO item : currentOrderItems) {
                 currentSubtotal = currentSubtotal.add(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())).setScale(2, RoundingMode.HALF_UP));
            }
            if (discount.compareTo(currentSubtotal) > 0) {
                errors.add(MessageProvider.getString("salesorder.validation.discountExceedsSubtotal"));
            }
        } catch (NumberFormatException e) {
             errors.add(MessageProvider.getString("salesorder.validation.discountInvalidFormat"));
        }

        if (!errors.isEmpty()) {
            AlertUtil.showValidationError(errors);
            return;
        }

        currentOrder.setPatientId(selectedPatient != null ? selectedPatient.getPatientId() : null);
        currentOrder.setOrderDate(DateTimeUtil.atStartOfDayWithUTC(orderDateField.getValue()));
        currentOrder.setStatus(statusComboBox.getValue());
        currentOrder.setRemarks(remarksArea.getText());
        currentOrder.setItems(new ArrayList<>(currentOrderItems));

        currentOrder.setDiscountAmount(discount);
        try {
             currentOrder.setAmountPaid(TextFormatters.parseBigDecimal(amountPaidField.getText(), BigDecimal.ZERO));
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Amount Paid is not a valid number.");
            return;
        }

        try {
            SalesOrderDTO savedOrder;
            if (currentOrder.getSalesOrderId() == 0) {
                savedOrder = salesOrderService.createSalesOrder(currentOrder);
            } else {
                salesOrderService.updateSalesOrderHeader(currentOrder);
                SalesOrderDTO orderAfterHeaderUpdate = salesOrderService.getSalesOrderDetails(currentOrder.getSalesOrderId())
                    .orElseThrow(() -> new SalesOrderServiceException("Failed to refetch order after header update."));

                if (!orderAfterHeaderUpdate.getStatus().equals(statusComboBox.getValue())) {
                     orderAfterHeaderUpdate = salesOrderService.changeOrderStatus(currentOrder.getSalesOrderId(), statusComboBox.getValue());
                }

                BigDecimal formDiscount = TextFormatters.parseBigDecimal(discountField.getText(), BigDecimal.ZERO);
                if (orderAfterHeaderUpdate.getDiscountAmount() == null || orderAfterHeaderUpdate.getDiscountAmount().compareTo(formDiscount) != 0) {
                    orderAfterHeaderUpdate = salesOrderService.applyDiscountToOrder(currentOrder.getSalesOrderId(), formDiscount);
                }
                savedOrder = salesOrderService.getSalesOrderDetails(currentOrder.getSalesOrderId()).orElse(null);
            }
            this.currentOrder = savedOrder;
            if (savedOrder != null) {
                saved = true;
                 AlertUtil.showSuccess("Order Saved", MessageProvider.getString("salesorder.success.saved", String.valueOf(savedOrder.getSalesOrderId())));
                updateNotifyButtonState();
                if (!"Ready for Pickup".equalsIgnoreCase(currentOrder.getStatus())) { // Don't close if ready, so user can notify
                     closeDialog();
                }
            } else {
                AlertUtil.showError("Save Error", "Failed to save or retrieve the order after saving.");
            }
        } catch (SalesOrderValidationException | NoActiveShiftException | PatientNotFoundException |
                 InventoryItemNotFoundException | ProductNotFoundException | PermissionDeniedException |
                 SalesOrderServiceException | InventoryItemServiceException e) {
            logger.error("Error saving sales order: {}", e.getMessage(), e);
            AlertUtil.showError("Save Failed", MessageProvider.getString("salesorder.error.saveFailed") + "\n" + e.getMessage());
        }
    }

    @FXML
    void handleNotifyOrderReadyButtonAction(ActionEvent event) {
        if (currentOrder == null || currentOrder.getPatientId() == null ) {
             AlertUtil.showError(MessageProvider.getString("salesorder.notify.confirmation.title"),
                                MessageProvider.getString("salesorder.notify.error.noConsentOrPhone"));
            return;
        }
        updateNotifyButtonState(); // Re-check and refresh patient details in currentOrder DTO
        if (!currentOrder.isPatientWhatsappOptIn() || currentOrder.getPatientPhoneNumber() == null || currentOrder.getPatientPhoneNumber().trim().isEmpty()) {
            AlertUtil.showError(MessageProvider.getString("salesorder.notify.confirmation.title"),
                                MessageProvider.getString("salesorder.notify.error.noConsentOrPhone"));
            return;
        }

        try {
            CenterProfileDTO centerProfile = centerProfileService.getCenterProfile()
                .orElseThrow(() -> new WhatsAppNotificationException("Center profile not configured. Cannot send notification."));

            Map<String, Object> contextData = new HashMap<>();
            contextData.put("[PatientName]", currentOrder.getPatientFullName() != null ? currentOrder.getPatientFullName() : "Valued Customer");
            contextData.put("[OrderID]", String.valueOf(currentOrder.getSalesOrderId()));
            contextData.put("[CenterName]", centerProfile.getCenterName());
            if (currentOrder.getBalanceDue() != null && currentOrder.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
                 contextData.put("[BalanceDue]", currentOrder.getBalanceDue().toPlainString());
            }

            String link = whatsAppNotificationService.generateClickToChatLink(
                currentOrder.getPatientPhoneNumber(),
                "ORDER_READY",
                contextData
            );

            DesktopActions.openWebLink(link);
            AlertUtil.showInfo(
                MessageProvider.getString("salesorder.notify.confirmation.title"),
                MessageProvider.getString("salesorder.notify.confirmation.message")
            );

        } catch (WhatsAppNotificationException e) {
            logger.error("WhatsApp Notification Error for order ID {}: {}", currentOrder.getSalesOrderId(), e.getMessage(), e);
            String errorMessage = e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("Template not found")) {
                String[] parts = e.getMessage().split(" ");
                String localeStr = parts.length > 0 ? parts[parts.length-1] : LocaleManager.getInstance().getCurrentLocale().getLanguage();
                errorMessage = MessageProvider.getString("salesorder.notify.error.templateMissing", localeStr);
            } else if (e.getMessage() != null) {
                 errorMessage = MessageProvider.getString("salesorder.notify.error.linkGenerationFailed", e.getMessage());
            }
            AlertUtil.showError(MessageProvider.getString("salesorder.notify.confirmation.title"), errorMessage);
        } catch (SalesOrderServiceException | NullPointerException e) {
             logger.error("Service Exception for WhatsApp Notify for order ID {}: {}", currentOrder.getSalesOrderId(), e.getMessage(), e);
            AlertUtil.showError(MessageProvider.getString("salesorder.notify.confirmation.title"), e.getMessage());
        }
         catch (IOException e) {
            logger.error("Failed to open web browser for WhatsApp for order ID {}: {}", currentOrder.getSalesOrderId(), e.getMessage(), e);
            AlertUtil.showError(MessageProvider.getString("salesorder.notify.confirmation.title"),
                                MessageProvider.getString("salesorder.notify.error.browserOpenFailed", e.getMessage()));
        }
    }

    @FXML
    void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void applyDecimalFormatter(TextField field) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        }));
    }
}
