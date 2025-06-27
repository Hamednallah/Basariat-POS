package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.service.CategoryException;
import com.basariatpos.service.ProductCategoryService;
import com.basariatpos.util.AppLogger;

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
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductCategoryManagementController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(ProductCategoryManagementController.class);

    @FXML private TableView<ProductCategoryDTO> categoriesTable;
    @FXML private TableColumn<ProductCategoryDTO, String> nameEnColumn;
    @FXML private TableColumn<ProductCategoryDTO, String> nameArColumn;
    // No status column as per DTO and DB schema

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private BorderPane productCategoryManagementRootPane; // For RTL

    private ProductCategoryService productCategoryService;
    private final ObservableList<ProductCategoryDTO> categoryObservableList = FXCollections.observableArrayList();
    private Stage currentStage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.productCategoryService = AppLauncher.getProductCategoryService();
        if (this.productCategoryService == null) {
            logger.error("ProductCategoryService is null. Cannot perform operations.");
            showErrorAlert("Critical Error", "Product Category Service is not available.");
            if(addButton!=null) addButton.setDisable(true);
            if(editButton!=null) editButton.setDisable(true);
            if(deleteButton!=null) deleteButton.setDisable(true);
            return;
        }

        updateNodeOrientation();
        setupTableColumns();
        loadCategories();

        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean itemSelected = newSelection != null;
            editButton.setDisable(!itemSelected);
            deleteButton.setDisable(!itemSelected);
        });
        logger.info("ProductCategoryManagementController initialized.");
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (productCategoryManagementRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                productCategoryManagementRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                productCategoryManagementRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("productCategoryManagementRootPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    public void setProductCategoryService(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
        if (categoriesTable != null) {
            loadCategories();
        }
    }

    private void setupTableColumns() {
        nameEnColumn.setCellValueFactory(new PropertyValueFactory<>("categoryNameEn"));
        nameArColumn.setCellValueFactory(new PropertyValueFactory<>("categoryNameAr"));
    }

    private void loadCategories() {
        try {
            List<ProductCategoryDTO> categories = productCategoryService.getAllProductCategories();
            categoryObservableList.setAll(categories);
            categoriesTable.setItems(categoryObservableList);
            logger.info("Product categories loaded. Count: {}", categories.size());
        } catch (CategoryException e) {
            logger.error("Failed to load product categories: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("productcategory.management.title"), "Could not load categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddButtonAction(ActionEvent event) {
        showCategoryFormDialog(null, (Stage) addButton.getScene().getWindow());
    }

    @FXML
    private void handleEditButtonAction(ActionEvent event) {
        ProductCategoryDTO selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showErrorAlert("No Selection", MessageProvider.getString("usermanagement.validation.selection.required")); // Reusing user mgmt key
            return;
        }
        showCategoryFormDialog(selectedCategory, (Stage) editButton.getScene().getWindow());
    }

    private void showCategoryFormDialog(ProductCategoryDTO category, Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ProductCategoryFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            ProductCategoryFormDialogController controller = loader.getController();
            controller.setProductCategoryService(this.productCategoryService);
            if (category != null) {
                controller.setEditableCategory(category);
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle(category == null ?
                                 MessageProvider.getString("productcategory.dialog.add.title") :
                                 MessageProvider.getString("productcategory.dialog.edit.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(ownerStage);
            dialogStage.setScene(new Scene(dialogRoot));
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadCategories();
                showSuccessAlert(MessageProvider.getString("productcategory.success.saved"));
            }
        } catch (IOException e) {
            logger.error("Failed to load ProductCategoryFormDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the product category form.");
        }
    }

    @FXML
    private void handleDeleteButtonAction(ActionEvent event) {
        ProductCategoryDTO selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showErrorAlert("No Selection", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                                       MessageProvider.getString("productcategory.confirm.delete.content"),
                                       ButtonType.YES, ButtonType.NO);
        confirmation.setTitle(MessageProvider.getString("productcategory.confirm.delete.title"));
        confirmation.setHeaderText(null);
        confirmation.initOwner((Stage) deleteButton.getScene().getWindow());
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                productCategoryService.deleteProductCategory(selectedCategory.getCategoryId());
                showSuccessAlert(MessageProvider.getString("productcategory.success.deleted"));
                loadCategories();
            } catch (CategoryException e) { // Catches CategoryInUseException, CategoryNotFoundException
                logger.error("Error deleting product category ID {}: {}", selectedCategory.getCategoryId(), e.getMessage(), e);
                showErrorAlert("Error Deleting Category", e.getMessage());
            }
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("productcategory.management.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner((Stage) categoriesTable.getScene().getWindow());
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner((Stage) categoriesTable.getScene().getWindow());
        alert.showAndWait();
    }
}
