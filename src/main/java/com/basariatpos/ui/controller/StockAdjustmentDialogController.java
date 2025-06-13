package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.service.InventoryItemService;
import com.basariatpos.service.exception.InventoryItemNotFoundException;
import com.basariatpos.service.exception.InventoryItemServiceException;
import com.basariatpos.service.exception.InventoryItemValidationException;
import com.basariatpos.util.AppLogger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox; // For controlling visibility of otherReasonArea container if needed
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StockAdjustmentDialogController {

    private static final Logger logger = AppLogger.getLogger(StockAdjustmentDialogController.class);

    @FXML private ComboBox<InventoryItemDTO> itemComboBox;
    @FXML private TextField currentQohField;
    @FXML private TextField adjustmentQuantityField;
    @FXML private ComboBox<String> reasonComboBox;
    @FXML private Label otherReasonLabel;
    @FXML private TextArea otherReasonArea;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private InventoryItemService itemService;
    private boolean saved = false;

    private List<String> predefinedReasonKeys = Arrays.asList(
        "stockadjustment.reason.damaged", "stockadjustment.reason.correction",
        "stockadjustment.reason.initial", "stockadjustment.reason.promotional",
        "stockadjustment.reason.expired", "stockadjustment.reason.other"
    );


    public void initializeDialog(InventoryItemService itemService, Stage stage) {
        this.itemService = itemService;
        this.dialogStage = stage;

        loadInventoryItems();
        populateReasonComboBox();

        itemComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentQohField.setText(String.valueOf(newVal.getQuantityOnHand()));
            } else {
                currentQohField.clear();
            }
        });

        reasonComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOther = MessageProvider.getString("stockadjustment.reason.other").equals(newVal);
            otherReasonLabel.setVisible(isOther); otherReasonLabel.setManaged(isOther);
            otherReasonArea.setVisible(isOther); otherReasonArea.setManaged(isOther);
        });
        otherReasonLabel.setVisible(false); otherReasonLabel.setManaged(false);
        otherReasonArea.setVisible(false); otherReasonArea.setManaged(false);

        // Numeric input formatter for adjustment quantity
        applyIntegerFormatter(adjustmentQuantityField);
    }

    private void applyIntegerFormatter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            // Allow empty, a minus sign, or minus sign followed by digits, or just digits
            if (newText.isEmpty() || newText.equals("-") || newText.matches("-?\\d*")) {
                return change;
            }
            return null; // Reject change
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }


    private void loadInventoryItems() {
        try {
            // Load only active, stockable items for adjustment
            List<InventoryItemDTO> items = itemService.getAllInventoryItems(false)
                .stream()
                .filter(item -> item.isActive() && item.isStockItem())
                .collect(Collectors.toList());
            itemComboBox.setItems(FXCollections.observableArrayList(items));
            itemComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(InventoryItemDTO item) {
                    return item != null ? item.getDisplayFullNameEn() : null;
                }
                @Override
                public InventoryItemDTO fromString(String string) { return null; }
            });
        } catch (InventoryItemServiceException e) {
            logger.error("Failed to load inventory items for adjustment dialog: {}", e.getMessage(), e);
            showErrorAlert("Load Error", "Could not load items: " + e.getMessage());
        }
    }

    private void populateReasonComboBox() {
        List<String> localizedReasons = predefinedReasonKeys.stream()
                                          .map(MessageProvider::getString)
                                          .collect(Collectors.toList());
        reasonComboBox.setItems(FXCollections.observableArrayList(localizedReasons));
    }

    @FXML
    private void handleSubmitAdjustmentButtonAction(ActionEvent event) {
        List<String> errors = new ArrayList<>();
        InventoryItemDTO selectedItem = itemComboBox.getValue();
        String adjQtyText = adjustmentQuantityField.getText();
        String selectedReasonDisplay = reasonComboBox.getValue();
        String actualReason = selectedReasonDisplay;

        if (selectedItem == null) {
            errors.add(MessageProvider.getString("stockadjustment.validation.itemRequired"));
        }
        if (adjQtyText == null || adjQtyText.trim().isEmpty()) {
            errors.add(MessageProvider.getString("stockadjustment.validation.qtyRequired"));
        }
        if (selectedReasonDisplay == null || selectedReasonDisplay.trim().isEmpty()) {
            errors.add(MessageProvider.getString("stockadjustment.validation.reasonRequired"));
        } else if (MessageProvider.getString("stockadjustment.reason.other").equals(selectedReasonDisplay)) {
            actualReason = otherReasonArea.getText().trim();
            if (actualReason.isEmpty()) {
                errors.add(MessageProvider.getString("stockadjustment.validation.otherReasonRequired"));
            }
        }

        int quantityChange = 0;
        try {
            if (!adjQtyText.trim().isEmpty()) quantityChange = Integer.parseInt(adjQtyText);
        } catch (NumberFormatException e) {
            errors.add(MessageProvider.getString("stockadjustment.validation.qtyNumeric"));
        }

        if (selectedItem != null && (selectedItem.getQuantityOnHand() + quantityChange < 0) ) {
            errors.add(MessageProvider.getString("stockadjustment.validation.qohNegative"));
        }


        if (!errors.isEmpty()) {
            showValidationErrorAlert(errors);
            return;
        }

        try {
            itemService.performStockAdjustment(selectedItem.getInventoryItemId(), quantityChange, actualReason);
            saved = true;
            showSuccessAlert(MessageProvider.getString("stockadjustment.success", selectedItem.getDisplayFullNameEn()));
            closeDialog();
        } catch (InventoryItemNotFoundException | InventoryItemValidationException | InventoryItemServiceException e) {
            logger.error("Error performing stock adjustment for item ID {}: {}", selectedItem.getInventoryItemId(), e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("stockadjustment.error.generic"), e.getMessage());
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    public boolean isSaved() { return saved; }

    private void showValidationErrorAlert(List<String> errors) { /* ... as in other controllers ... */ }
    private void showErrorAlert(String title, String content) { /* ... as in other controllers ... */ }
    private void showSuccessAlert(String message) { /* ... as in other controllers ... */ }
    private void closeDialog() { if (dialogStage != null) dialogStage.close(); }
}
