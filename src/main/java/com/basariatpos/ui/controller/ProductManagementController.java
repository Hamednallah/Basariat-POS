package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ProductCategoryDTO; // For category display
import com.basariatpos.model.ProductDTO;
import com.basariatpos.service.ProductService;
import com.basariatpos.service.ProductCategoryService; // For category combo in dialog
import com.basariatpos.service.exception.ProductServiceException;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductManagementController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(ProductManagementController.class);

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private TableView<ProductDTO> productsTable;
    @FXML private TableColumn<ProductDTO, String> productCodeColumn;
    @FXML private TableColumn<ProductDTO, String> productNameEnColumn;
    @FXML private TableColumn<ProductDTO, String> productNameArColumn;
    @FXML private TableColumn<ProductDTO, String> categoryColumn;
    @FXML private TableColumn<ProductDTO, String> isServiceColumn;
    @FXML private TableColumn<ProductDTO, String> isStockItemColumn;

    @FXML private Button addProductButton;
    @FXML private Button editProductButton;
    @FXML private Button deleteProductButton;
    @FXML private BorderPane productManagementRootPane; // For RTL

    private ProductService productService;
    private ProductCategoryService productCategoryService; // For populating category ComboBox in dialog
    private final ObservableList<ProductDTO> productObservableList = FXCollections.observableArrayList();
    private Stage currentStage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.productService = AppLauncher.getProductService();
        this.productCategoryService = AppLauncher.getProductCategoryService();

        if (this.productService == null || this.productCategoryService == null) {
            logger.error("ProductService or ProductCategoryService is null. Cannot perform operations.");
            showErrorAlert("Critical Error", "Product services are not available.");
            if(searchField!=null) searchField.setDisable(true);
            if(searchButton!=null) searchButton.setDisable(true);
            if(clearSearchButton!=null) clearSearchButton.setDisable(true);
            if(addProductButton!=null) addProductButton.setDisable(true);
            if(editProductButton!=null) editProductButton.setDisable(true);
            if(deleteProductButton!=null) deleteProductButton.setDisable(true);
            return;
        }

        updateNodeOrientation();
        setupTableColumns();
        loadInitialTableData();

        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean productSelected = newSelection != null;
            editProductButton.setDisable(!productSelected);
            deleteProductButton.setDisable(!productSelected);
        });

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSearchButtonAction(null);
            }
        });
        logger.info("ProductManagementController initialized.");
    }

    public void setStage(Stage stage) { // If needed for dialog ownership by MainFrame
        this.currentStage = stage;
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (productManagementRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                productManagementRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                productManagementRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("productManagementRootPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    // Setter for services if needed for external initialization or testing
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
    public void setProductCategoryService(ProductCategoryService productCategoryService){
        this.productCategoryService = productCategoryService;
    }


    private void setupTableColumns() {
        productCodeColumn.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        productNameEnColumn.setCellValueFactory(new PropertyValueFactory<>("productNameEn"));
        productNameArColumn.setCellValueFactory(new PropertyValueFactory<>("productNameAr"));
        categoryColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCategoryNameEn() + " / " + cellData.getValue().getCategoryNameAr())
        );
        isServiceColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isService() ?
                                     MessageProvider.getString("patientmanagement.consent.yes") : // Reusing Yes/No
                                     MessageProvider.getString("patientmanagement.consent.no"))
        );
        isStockItemColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isStockItem() ?
                                     MessageProvider.getString("patientmanagement.consent.yes") :
                                     MessageProvider.getString("patientmanagement.consent.no"))
        );
    }

    private void loadInitialTableData() {
        refreshProductsTable(productService.getAllProducts());
    }

    private void refreshProductsTable(List<ProductDTO> products) {
        productObservableList.setAll(products);
        productsTable.setItems(productObservableList);
        logger.info("Products table refreshed. Displaying {} products.", products.size());
    }

    @FXML
    private void handleSearchButtonAction(ActionEvent event) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadInitialTableData();
            return;
        }
        try {
            List<ProductDTO> searchResults = productService.searchProducts(query);
            refreshProductsTable(searchResults);
        } catch (ProductServiceException e) {
            logger.error("Error during product search for query '{}': {}", query, e.getMessage(), e);
            showErrorAlert("Search Error", "Could not perform search: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearSearchButtonAction(ActionEvent event) {
        searchField.clear();
        loadInitialTableData();
    }

    @FXML
    private void handleAddProductButtonAction(ActionEvent event) {
        showProductFormDialog(null);
    }

    @FXML
    private void handleEditProductButtonAction(ActionEvent event) {
        ProductDTO selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showErrorAlert("No Product Selected", MessageProvider.getString("usermanagement.validation.selection.required")); // Reuse key
            return;
        }
        showProductFormDialog(selectedProduct);
    }

    private void showProductFormDialog(ProductDTO productToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ProductFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            ProductFormDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(productToEdit == null ?
                                 MessageProvider.getString("product.dialog.add.title") :
                                 MessageProvider.getString("product.dialog.edit.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner((Stage) addProductButton.getScene().getWindow()); // Assuming button is on main stage
            dialogStage.setScene(new Scene(dialogRoot));

            // Call initializeDialog on the dialog controller
            controller.initializeDialog(this.productService, this.productCategoryService, dialogStage, productToEdit);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                handleSearchButtonAction(null); // Refresh table (or use loadInitialTableData if search was cleared)
                showSuccessAlert(MessageProvider.getString("product.success.saved", controller.getSavedProduct().getProductNameEn()));
            }
        } catch (IOException e) {
            logger.error("Failed to load ProductFormDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the product form.");
        }
    }

    @FXML
    private void handleDeleteProductButtonAction(ActionEvent event) {
        ProductDTO selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showErrorAlert("No Product Selected", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                                       MessageProvider.getString("product.confirm.delete.content", selectedProduct.getProductNameEn(), selectedProduct.getProductCode()),
                                       ButtonType.YES, ButtonType.NO);
        confirmation.setTitle(MessageProvider.getString("product.confirm.delete.title"));
        confirmation.setHeaderText(null);
        confirmation.initOwner((Stage) deleteProductButton.getScene().getWindow());
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                productService.deleteProduct(selectedProduct.getProductId());
                showSuccessAlert(MessageProvider.getString("product.success.deleted", selectedProduct.getProductNameEn()));
                loadInitialTableData(); // Refresh table
            } catch (ProductServiceException e) {
                logger.error("Error deleting product ID {}: {}", selectedProduct.getProductId(), e.getMessage(), e);
                showErrorAlert("Error Deleting Product", e.getMessage());
            }
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("product.management.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner((Stage) productsTable.getScene().getWindow());
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner((Stage) productsTable.getScene().getWindow());
        alert.showAndWait();
    }
}
