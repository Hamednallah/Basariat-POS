package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.service.CategoryAlreadyExistsException;
import com.basariatpos.service.CategoryException;
import com.basariatpos.service.ProductCategoryService;
import com.basariatpos.service.ValidationException;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProductCategoryFormDialogController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(ProductCategoryFormDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private TextField nameEnField;
    @FXML private TextField nameArField;
    // No Active CheckBox for product categories
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private ProductCategoryService productCategoryService;
    private ProductCategoryDTO editableCategory;
    private boolean isEditMode = false;
    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nothing specific to initialize here for product categories form beyond FXML loading
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setProductCategoryService(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    public void setEditableCategory(ProductCategoryDTO category) {
        this.editableCategory = category;
        this.isEditMode = true;

        dialogTitleLabel.setText(MessageProvider.getString("productcategory.dialog.edit.title"));
        nameEnField.setText(category.getCategoryNameEn());
        nameArField.setText(category.getCategoryNameAr());
        // No active status to set
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        String nameEn = nameEnField.getText().trim();
        String nameAr = nameArField.getText().trim();

        ProductCategoryDTO categoryToSave;
        if (isEditMode) {
            categoryToSave = editableCategory;
            categoryToSave.setCategoryNameEn(nameEn);
            categoryToSave.setCategoryNameAr(nameAr);
        } else {
            categoryToSave = new ProductCategoryDTO(nameEn, nameAr);
        }

        try {
            productCategoryService.saveProductCategory(categoryToSave);
            saved = true;
            closeDialog();
        } catch (ValidationException e) {
            logger.warn("Validation error saving product category: {}", e.getErrors());
            showValidationErrorAlert(e.getErrors());
        } catch (CategoryAlreadyExistsException e) {
            logger.warn("Product category already exists: {}", e.getMessage());
            List<String> errors = new ArrayList<>();
             if (e.getMessage().toLowerCase().contains("english")) {
                errors.add(MessageProvider.getString("productcategory.validation.nameEn.exists"));
            } else if (e.getMessage().toLowerCase().contains("arabic")) {
                errors.add(MessageProvider.getString("productcategory.validation.nameAr.exists"));
            } else {
                errors.add(e.getMessage());
            }
            showValidationErrorAlert(errors);
        } catch (CategoryException e) {
            logger.error("Error saving product category: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("productcategory.error.generic"), e.getMessage());
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();
        if (nameEnField.getText() == null || nameEnField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("productcategory.validation.nameEn.required"));
        }
        if (nameArField.getText() == null || nameArField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("productcategory.validation.nameAr.required"));
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

    public boolean isSaved() {
        return saved;
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        }
    }
}
