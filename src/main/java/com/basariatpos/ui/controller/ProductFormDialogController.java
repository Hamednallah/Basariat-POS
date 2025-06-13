package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.service.ProductCategoryService;
import com.basariatpos.service.ProductService;
import com.basariatpos.service.exception.ProductAlreadyExistsException;
import com.basariatpos.service.exception.ProductServiceException;
import com.basariatpos.service.exception.ProductValidationException;
import com.basariatpos.service.exception.CategoryNotFoundException; // From ProductService interface
import com.basariatpos.util.AppLogger;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ProductFormDialogController {

    private static final Logger logger = AppLogger.getLogger(ProductFormDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private TextField productCodeField;
    @FXML private TextField nameEnField;
    @FXML private TextField nameArField;
    @FXML private TextArea descriptionEnArea;
    @FXML private TextArea descriptionArArea;
    @FXML private ComboBox<ProductCategoryDTO> categoryComboBox;
    @FXML private CheckBox isServiceCheckBox;
    @FXML private CheckBox isStockItemCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private ProductService productService;
    private ProductCategoryService productCategoryService;
    private ProductDTO editableProduct;
    private ProductDTO savedProduct; // To hold the successfully saved product
    private boolean isEditMode = false;
    private boolean saved = false;

    public void initializeDialog(ProductService productService, ProductCategoryService productCategoryService,
                                 Stage dialogStage, ProductDTO productToEdit) {
        this.productService = productService;
        this.productCategoryService = productCategoryService;
        this.dialogStage = dialogStage;

        loadCategoryComboBox();

        if (productToEdit != null) {
            this.editableProduct = productToEdit;
            this.isEditMode = true;
            dialogTitleLabel.setText(MessageProvider.getString("product.dialog.edit.title"));
            populateFormFields();
        } else {
            this.editableProduct = new ProductDTO(); // Create new DTO for add mode
            this.isEditMode = false;
            dialogTitleLabel.setText(MessageProvider.getString("product.dialog.add.title"));
            isStockItemCheckBox.setSelected(true); // Default for new physical item
            isServiceCheckBox.setSelected(false);
        }

        // Logic for isService and isStockItem interaction
        isServiceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) { // If isService is checked
                isStockItemCheckBox.setSelected(false);
                isStockItemCheckBox.setDisable(true);
            } else { // If isService is unchecked
                isStockItemCheckBox.setDisable(false);
                 isStockItemCheckBox.setSelected(true); // Default to stock item if not service
            }
        });
        // Initial state based on isService for an existing product
        if(isEditMode && editableProduct.isService()){
            isStockItemCheckBox.setSelected(false);
            isStockItemCheckBox.setDisable(true);
        } else if (isEditMode && !editableProduct.isService()){
             isStockItemCheckBox.setDisable(false);
             isStockItemCheckBox.setSelected(editableProduct.isStockItem());
        }

    }

    private void loadCategoryComboBox() {
        try {
            List<ProductCategoryDTO> categories = productCategoryService.getAllProductCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            categoryComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(ProductCategoryDTO category) {
                    if (category == null) return null;
                    // Display based on current UI locale if possible, or default to EN/AR
                    // For simplicity, using EN / AR. A more advanced converter could use LocaleManager.
                    return category.getCategoryNameEn() + " / " + category.getCategoryNameAr();
                }
                @Override
                public ProductCategoryDTO fromString(String string) { return null; /* Not needed for non-editable ComboBox */ }
            });
        } catch (Exception e) {
            logger.error("Failed to load product categories for ComboBox: {}", e.getMessage(), e);
            showErrorAlert("Error Loading Categories", "Could not load product categories: " + e.getMessage());
        }
    }

    private void populateFormFields() {
        if (editableProduct != null) {
            productCodeField.setText(editableProduct.getProductCode());
            nameEnField.setText(editableProduct.getProductNameEn());
            nameArField.setText(editableProduct.getProductNameAr());
            descriptionEnArea.setText(editableProduct.getDescriptionEn());
            descriptionArArea.setText(editableProduct.getDescriptionAr());

            // Select category in ComboBox
            if (editableProduct.getCategoryId() > 0) {
                categoryComboBox.getItems().stream()
                    .filter(cat -> cat.getCategoryId() == editableProduct.getCategoryId())
                    .findFirst()
                    .ifPresent(categoryComboBox::setValue);
            }

            isServiceCheckBox.setSelected(editableProduct.isService());
            isStockItemCheckBox.setSelected(editableProduct.isStockItem());
        }
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        editableProduct.setProductCode(productCodeField.getText().trim());
        editableProduct.setProductNameEn(nameEnField.getText().trim());
        editableProduct.setProductNameAr(nameArField.getText().trim());
        editableProduct.setCategoryId(categoryComboBox.getValue().getCategoryId());
        editableProduct.setDescriptionEn(descriptionEnArea.getText());
        editableProduct.setDescriptionAr(descriptionArArea.getText());
        editableProduct.setService(isServiceCheckBox.isSelected());
        editableProduct.setStockItem(isStockItemCheckBox.isSelected());

        try {
            savedProduct = productService.saveProduct(editableProduct); // Service handles create vs update based on ID
            saved = true;
            closeDialog();
        } catch (ProductValidationException e) {
            showValidationErrorAlert(e.getErrors());
        } catch (ProductAlreadyExistsException e) {
            showErrorAlert(MessageProvider.getString("product.error.codeExists", editableProduct.getProductCode()), e.getMessage());
        } catch (CategoryNotFoundException e){
            showErrorAlert(MessageProvider.getString("product.error.categoryRequired"), e.getMessage());
        }
        catch (ProductServiceException e) {
            showErrorAlert(MessageProvider.getString("product.error.generic"), e.getMessage());
        }
    }

    @FXML
    private void handleIsServiceToggle(ActionEvent event) {
        if (isServiceCheckBox.isSelected()) {
            isStockItemCheckBox.setSelected(false);
            isStockItemCheckBox.setDisable(true);
        } else {
            isStockItemCheckBox.setDisable(false);
            isStockItemCheckBox.setSelected(true); // Default to stock item if not a service
        }
    }


    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();
        if (productCodeField.getText() == null || productCodeField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("product.label.code") + " " + MessageProvider.getString("validation.general.required", "")); // crude
        }
        if (nameEnField.getText() == null || nameEnField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("product.validation.nameEn.required"));
        }
        if (nameArField.getText() == null || nameArField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("product.validation.nameAr.required"));
        }
        if (categoryComboBox.getValue() == null) {
            errors.add(MessageProvider.getString("product.error.categoryRequired"));
        }
        if (isServiceCheckBox.isSelected() && isStockItemCheckBox.isSelected()){
            errors.add("A product cannot be both a service and a stock item."); // TODO: i18n
        }


        if (!errors.isEmpty()) {
            showValidationErrorAlert(errors);
            return false;
        }
        return true;
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
    public ProductDTO getSavedProduct() { return savedProduct; }

    private void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }
}
