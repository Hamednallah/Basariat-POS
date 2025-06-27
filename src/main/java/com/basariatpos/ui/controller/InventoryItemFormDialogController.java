package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.service.InventoryItemService;
import com.basariatpos.service.ProductService;
import com.basariatpos.service.exception.InventoryItemValidationException;
import com.basariatpos.service.exception.ProductNotFoundException;
import com.basariatpos.service.exception.InventoryItemServiceException;
import com.basariatpos.util.AppLogger;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InventoryItemFormDialogController {

    private static final Logger logger = AppLogger.getLogger(InventoryItemFormDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private ComboBox<ProductDTO> productComboBox;
    @FXML private TextField brandNameField;
    @FXML private TextField specificNameEnField;
    @FXML private TextField specificNameArField;
    @FXML private TextField unitOfMeasureField;
    @FXML private TextArea attributesArea;
    @FXML private TextField quantityOnHandField;
    @FXML private TextField sellingPriceField;
    @FXML private TextField costPriceField;
    @FXML private TextField minStockLevelField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private VBox inventoryItemFormRootPane; // For RTL

    private Stage dialogStage;
    private InventoryItemService itemService;
    private ProductService productService;
    private InventoryItemDTO editableItem;
    private InventoryItemDTO savedItem;
    private boolean isEditMode = false;
    private boolean saved = false;

    private final NumberFormat currencyFormat = new DecimalFormat("#,##0.00"); // For display formatting if needed
    private final NumberFormat generalNumberFormat = NumberFormat.getNumberInstance(Locale.US); // For parsing

    public void initialize() { // Called by FXML loader
         activeCheckBox.setSelected(true); // Default for new item
         setupNumericFieldFormatters();
         updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (inventoryItemFormRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                inventoryItemFormRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                inventoryItemFormRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("inventoryItemFormRootPane is null. Cannot set RTL/LTR orientation.");
        }
    }


    private void setupNumericFieldFormatters() {
        // Allow digits, and optionally one dot or comma for decimals
        String decimalPattern = "\\d*([\\.,]\\d{0,2})?";
        // Allow only digits for integers
        String integerPattern = "\\d*";

        applyFormatter(quantityOnHandField, integerPattern);
        applyFormatter(minStockLevelField, integerPattern);
        applyFormatter(sellingPriceField, decimalPattern);
        applyFormatter(costPriceField, decimalPattern);
    }

    private void applyFormatter(TextField field, String pattern) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches(pattern) || newText.isEmpty()) {
                return change;
            }
            return null; // Reject change
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }


    public void initializeDialog(InventoryItemService itemService, ProductService productService,
                                 Stage dialogStage, InventoryItemDTO itemToEdit) {
        this.itemService = itemService;
        this.productService = productService;
        this.dialogStage = dialogStage;

        loadProductComboBox();

        if (itemToEdit != null) {
            this.editableItem = itemToEdit;
            this.isEditMode = true;
            dialogTitleLabel.setText(MessageProvider.getString("inventoryitem.dialog.edit.title"));
            populateFormFields();
            productComboBox.setDisable(true); // Product cannot be changed for an existing item
        } else {
            this.editableItem = new InventoryItemDTO();
            this.isEditMode = false;
            dialogTitleLabel.setText(MessageProvider.getString("inventoryitem.dialog.add.title"));
            productComboBox.setDisable(false);
        }
    }

    private void loadProductComboBox() {
        try {
            List<ProductDTO> products = productService.getAllProducts(); // Assuming this gets active, non-service products suitable for inventory
            productComboBox.setItems(FXCollections.observableArrayList(products));
            productComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(ProductDTO product) {
                    return product != null ? product.getDisplayFullNameEn() : null;
                }
                @Override
                public ProductDTO fromString(String string) { return null; }
            });
        } catch (Exception e) {
            logger.error("Failed to load products for ComboBox: {}", e.getMessage(), e);
            showErrorAlert("Error Loading Products", "Could not load products: " + e.getMessage());
        }
    }

    private void populateFormFields() {
        if (editableItem != null) {
            // Select product in ComboBox
            if (editableItem.getProductId() > 0) {
                productComboBox.getItems().stream()
                    .filter(p -> p.getProductId() == editableItem.getProductId())
                    .findFirst()
                    .ifPresent(productComboBox::setValue);
            }
            brandNameField.setText(editableItem.getBrandName());
            specificNameEnField.setText(editableItem.getItemSpecificNameEn());
            specificNameArField.setText(editableItem.getItemSpecificNameAr());
            unitOfMeasureField.setText(editableItem.getUnitOfMeasure());
            attributesArea.setText(editableItem.getAttributes());
            quantityOnHandField.setText(String.valueOf(editableItem.getQuantityOnHand()));
            sellingPriceField.setText(editableItem.getSellingPrice() != null ? editableItem.getSellingPrice().toPlainString() : "");
            costPriceField.setText(editableItem.getCostPrice() != null ? editableItem.getCostPrice().toPlainString() : "");
            minStockLevelField.setText(String.valueOf(editableItem.getMinStockLevel()));
            activeCheckBox.setSelected(editableItem.isActive());
        }
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        List<String> validationErrors = new ArrayList<>();
        if (!validateAndPopulateDto(validationErrors)) {
            showValidationErrorAlert(validationErrors);
            return;
        }

        try {
            savedItem = itemService.saveInventoryItem(editableItem);
            saved = true;
            closeDialog();
        } catch (InventoryItemValidationException e) {
            showValidationErrorAlert(e.getErrors());
        } catch (ProductNotFoundException e) { // Should be caught by productComboBox selection ideally
            showErrorAlert("Validation Error", e.getMessage());
        } catch (InventoryItemServiceException e) {
            showErrorAlert(MessageProvider.getString("inventoryitem.error.generic"), e.getMessage());
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateAndPopulateDto(List<String> errors) {
        ProductDTO selectedProduct = productComboBox.getValue();
        if (selectedProduct == null) {
            errors.add(MessageProvider.getString("inventoryitem.error.productRequired"));
        } else {
            editableItem.setProductId(selectedProduct.getProductId());
            // Product names are for display, not saved on InventoryItem directly
        }

        editableItem.setBrandName(brandNameField.getText()); // Optional typically
        editableItem.setItemSpecificNameEn(specificNameEnField.getText());
        editableItem.setItemSpecificNameAr(specificNameArField.getText());
        editableItem.setUnitOfMeasure(unitOfMeasureField.getText());

        if (editableItem.getItemSpecificNameEn() == null || editableItem.getItemSpecificNameEn().trim().isEmpty()) {
            errors.add(MessageProvider.getString("inventoryitem.label.specificNameEn") + " " + MessageProvider.getString("validation.general.required", ""));
        }
        if (editableItem.getUnitOfMeasure() == null || editableItem.getUnitOfMeasure().trim().isEmpty()) {
            errors.add(MessageProvider.getString("inventoryitem.label.unitOfMeasure") + " " + MessageProvider.getString("validation.general.required", ""));
        }

        String attributesText = attributesArea.getText();
        if (attributesText != null && !attributesText.trim().isEmpty()) {
            try {
                JsonParser.parseString(attributesText); // Basic JSON validation
                editableItem.setAttributes(attributesText);
            } catch (JsonSyntaxException e) {
                errors.add(MessageProvider.getString("inventoryitem.error.invalidJson"));
            }
        } else {
            editableItem.setAttributes(null); // Ensure it's null if empty
        }

        editableItem.setQuantityOnHand(parseInteger(quantityOnHandField, "inventoryitem.label.qoh", errors, true));
        editableItem.setSellingPrice(parseBigDecimal(sellingPriceField, "inventoryitem.label.sellingPrice", errors, true));
        editableItem.setCostPrice(parseBigDecimal(costPriceField, "inventoryitem.label.costPrice", errors, false)); // Cost price can be optional/null
        editableItem.setMinStockLevel(parseInteger(minStockLevelField, "inventoryitem.label.minStock", errors, true));

        editableItem.setActive(activeCheckBox.isSelected());

        return errors.isEmpty();
    }

    private BigDecimal parseBigDecimal(TextField field, String fieldKey, List<String> errors, boolean required) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            if (required) errors.add(MessageProvider.getString(fieldKey) + " " + MessageProvider.getString("validation.general.required", ""));
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(text.replace(',', '.'));
            if (value.compareTo(BigDecimal.ZERO) < 0 && (field == sellingPriceField || field == costPriceField || field == quantityOnHandField || field == minStockLevelField) ) {
                 errors.add(MessageProvider.getString("inventoryitem.error.pricePositive")); // Or more specific
            }
            return value;
        } catch (NumberFormatException e) {
            errors.add(MessageProvider.getString("inventoryitem.error.numericRequired", MessageProvider.getString(fieldKey)));
            return null;
        }
    }

    private int parseInteger(TextField field, String fieldKey, List<String> errors, boolean required) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            if (required) errors.add(MessageProvider.getString(fieldKey) + " " + MessageProvider.getString("validation.general.required", ""));
            return 0; // Default for int if not required and empty
        }
        try {
            int value = Integer.parseInt(text.trim());
             if (value < 0 && (field == quantityOnHandField || field == minStockLevelField)) {
                 errors.add(MessageProvider.getString("inventoryitem.error.qohPositive")); // Or more specific for minStock
            }
            return value;
        } catch (NumberFormatException e) {
            errors.add(MessageProvider.getString("inventoryitem.error.integerRequired", MessageProvider.getString(fieldKey)));
            return 0;
        }
    }


    private void showValidationErrorAlert(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(MessageProvider.getString("validation.general.errorTitle"));
        alert.setHeaderText(null);
        alert.setContentText(String.join("\n", errors));
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }

    public boolean isSaved() { return saved; }
    public InventoryItemDTO getSavedItem() { return savedItem; }

    private void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }
}
