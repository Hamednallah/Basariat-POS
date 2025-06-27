package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.service.CategoryException;
import com.basariatpos.service.ExpenseCategoryService;
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
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ExpenseCategoryManagementController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(ExpenseCategoryManagementController.class);

    @FXML private TableView<ExpenseCategoryDTO> categoriesTable;
    @FXML private TableColumn<ExpenseCategoryDTO, String> nameEnColumn;
    @FXML private TableColumn<ExpenseCategoryDTO, String> nameArColumn;
    @FXML private TableColumn<ExpenseCategoryDTO, String> statusColumn;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button toggleActiveButton;
    @FXML private BorderPane expenseCategoryManagementRootPane; // For RTL

    private ExpenseCategoryService expenseCategoryService;
    private final ObservableList<ExpenseCategoryDTO> categoryObservableList = FXCollections.observableArrayList();
    private Stage currentStage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.expenseCategoryService = AppLauncher.getExpenseCategoryService();
        if (this.expenseCategoryService == null) {
            logger.error("ExpenseCategoryService is null. Cannot perform operations.");
            showErrorAlert("Critical Error", "Expense Category Service is not available.");
            if(addButton!=null) addButton.setDisable(true);
            if(editButton!=null) editButton.setDisable(true);
            if(toggleActiveButton!=null) toggleActiveButton.setDisable(true);
            return;
        }

        updateNodeOrientation();
        setupTableColumns();
        loadCategories();

        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean itemSelected = newSelection != null;
            editButton.setDisable(!itemSelected);
            // Disable toggle for protected categories (e.g., "Loss on Abandoned Orders")
            toggleActiveButton.setDisable(!itemSelected || (newSelection != null && expenseCategoryService.isProtectedCategory(newSelection)));
        });
        logger.info("ExpenseCategoryManagementController initialized.");
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (expenseCategoryManagementRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                expenseCategoryManagementRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                expenseCategoryManagementRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("expenseCategoryManagementRootPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    public void setExpenseCategoryService(ExpenseCategoryService expenseCategoryService) {
        this.expenseCategoryService = expenseCategoryService;
        if (categoriesTable != null) {
            loadCategories();
        }
    }

    private void setupTableColumns() {
        nameEnColumn.setCellValueFactory(new PropertyValueFactory<>("categoryNameEn"));
        nameArColumn.setCellValueFactory(new PropertyValueFactory<>("categoryNameAr"));
        statusColumn.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            String statusKey = isActive ? "usermanagement.status.active" : "usermanagement.status.inactive";
            return new SimpleStringProperty(MessageProvider.getString(statusKey));
        });
    }

    private void loadCategories() {
        try {
            List<ExpenseCategoryDTO> categories = expenseCategoryService.getAllExpenseCategories(true);
            categoryObservableList.setAll(categories);
            categoriesTable.setItems(categoryObservableList);
            logger.info("Expense categories loaded. Count: {}", categories.size());
        } catch (CategoryException e) {
            logger.error("Failed to load expense categories: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("expensecategory.management.title"), "Could not load categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddButtonAction(ActionEvent event) {
        showCategoryFormDialog(null, (Stage) addButton.getScene().getWindow());
    }

    @FXML
    private void handleEditButtonAction(ActionEvent event) {
        ExpenseCategoryDTO selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showErrorAlert("No Selection", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }
        showCategoryFormDialog(selectedCategory, (Stage) editButton.getScene().getWindow());
    }

    private void showCategoryFormDialog(ExpenseCategoryDTO category, Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ExpenseCategoryFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            ExpenseCategoryFormDialogController controller = loader.getController();
            controller.setExpenseCategoryService(this.expenseCategoryService);
            if (category != null) {
                controller.setEditableCategory(category);
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle(category == null ?
                                 MessageProvider.getString("expensecategory.dialog.add.title") :
                                 MessageProvider.getString("expensecategory.dialog.edit.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(ownerStage);
            dialogStage.setScene(new Scene(dialogRoot));
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadCategories();
                showSuccessAlert(MessageProvider.getString("expensecategory.success.saved"));
            }
        } catch (IOException e) {
            logger.error("Failed to load ExpenseCategoryFormDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the expense category form.");
        }
    }

    @FXML
    private void handleToggleActiveButtonAction(ActionEvent event) {
        ExpenseCategoryDTO selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showErrorAlert("No Selection", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                                       MessageProvider.getString("expensecategory.confirm.toggleActive.content"),
                                       ButtonType.YES, ButtonType.NO);
        confirmation.setTitle(MessageProvider.getString("expensecategory.confirm.toggleActive.title"));
        confirmation.setHeaderText(null);
        confirmation.initOwner((Stage) toggleActiveButton.getScene().getWindow());
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                expenseCategoryService.toggleExpenseCategoryStatus(selectedCategory.getExpenseCategoryId());
                showSuccessAlert(MessageProvider.getString("expensecategory.success.statusChanged"));
                loadCategories();
            } catch (CategoryException e) {
                logger.error("Error toggling category status for ID {}: {}", selectedCategory.getExpenseCategoryId(), e.getMessage(), e);
                showErrorAlert("Error", e.getMessage());
            }
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("expensecategory.management.title"));
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
