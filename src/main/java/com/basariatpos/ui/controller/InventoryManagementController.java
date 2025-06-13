package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.service.InventoryItemService;
import com.basariatpos.service.ProductService;
import com.basariatpos.service.exception.InventoryItemServiceException;
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
import javafx.util.StringConverter;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class InventoryManagementController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(InventoryManagementController.class);
    private static final String ALL_PRODUCTS_FILTER_KEY = "inventoryitem.filter.allProducts";


    @FXML private ComboBox<ProductDTO> productFilterComboBox;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private CheckBox lowStockOnlyCheckBox;
    @FXML private CheckBox showInactiveCheckBox;
    @FXML private TableView<InventoryItemDTO> inventoryItemsTable;

    // Table Columns
    @FXML private TableColumn<InventoryItemDTO, String> productNameColumn;
    @FXML private TableColumn<InventoryItemDTO, String> specificNameEnColumn;
    @FXML private TableColumn<InventoryItemDTO, String> brandColumn;
    @FXML private TableColumn<InventoryItemDTO, String> attributesColumn;
    @FXML private TableColumn<InventoryItemDTO, Integer> qohColumn;
    @FXML private TableColumn<InventoryItemDTO, BigDecimal> sellingPriceColumn;
    @FXML private TableColumn<InventoryItemDTO, BigDecimal> costPriceColumn;
    @FXML private TableColumn<InventoryItemDTO, Integer> minStockColumn;
    @FXML private TableColumn<InventoryItemDTO, String> unitColumn;
    @FXML private TableColumn<InventoryItemDTO, String> activeColumn;

    @FXML private Button addItemButton;
    @FXML private Button editItemButton;
    @FXML private Button toggleActiveButton;

    private InventoryItemService inventoryItemService;
    private ProductService productService;
    private final ObservableList<InventoryItemDTO> itemObservableList = FXCollections.observableArrayList();
    private final ObservableList<ProductDTO> productFilterList = FXCollections.observableArrayList();

    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0.00");


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.inventoryItemService = AppLauncher.getInventoryItemService();
        this.productService = AppLauncher.getProductService();

        if (this.inventoryItemService == null || this.productService == null) {
            logger.error("InventoryItemService or ProductService is null. UI will be disabled.");
            // Disable relevant controls
            if(productFilterComboBox != null) productFilterComboBox.setDisable(true);
            if(searchField != null) searchField.setDisable(true);
            // ... disable all other controls ...
            return;
        }

        setupProductFilterComboBox();
        setupTableColumns();
        loadInventoryItems();

        inventoryItemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean itemSelected = newSel != null;
            editItemButton.setDisable(!itemSelected);
            toggleActiveButton.setDisable(!itemSelected);
        });

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleFilterOrSearchAction(null);
        });

        // Add listeners to checkboxes and product filter to trigger combined search/filter
        lowStockOnlyCheckBox.setOnAction(this::handleFilterOrSearchAction);
        showInactiveCheckBox.setOnAction(this::handleFilterOrSearchAction);
        productFilterComboBox.setOnAction(this::handleFilterOrSearchAction);


        logger.info("InventoryManagementController initialized.");
    }

    public void setInventoryItemService(InventoryItemService service) { this.inventoryItemService = service; }
    public void setProductService(ProductService service) { this.productService = service; }


    private void setupProductFilterComboBox() {
        try {
            List<ProductDTO> products = productService.getAllProducts();
            productFilterList.add(null); // For "All Products" option
            productFilterList.addAll(products);
            productFilterComboBox.setItems(productFilterList);
            productFilterComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(ProductDTO product) {
                    if (product == null) return MessageProvider.getString(ALL_PRODUCTS_FILTER_KEY); // Localized "All Products"
                    return product.getDisplayFullNameEn(); // Using DTO's display name
                }
                @Override
                public ProductDTO fromString(String string) { return null; }
            });
            productFilterComboBox.getSelectionModel().selectFirst(); // Select "All Products"
        } catch (Exception e) {
            logger.error("Failed to load products for filter ComboBox: {}", e.getMessage(), e);
            showErrorAlert("Error Loading Products", "Could not load product filter options.");
        }
    }

    private void setupTableColumns() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productNameEn"));
        specificNameEnColumn.setCellValueFactory(new PropertyValueFactory<>("itemSpecificNameEn"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        attributesColumn.setCellValueFactory(new PropertyValueFactory<>("attributes"));
        qohColumn.setCellValueFactory(new PropertyValueFactory<>("quantityOnHand"));
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        costPriceColumn.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        minStockColumn.setCellValueFactory(new PropertyValueFactory<>("minStockLevel"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
        activeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().isActive() ? MessageProvider.getString("usermanagement.status.active") : MessageProvider.getString("usermanagement.status.inactive")
        ));

        // Currency formatting for price columns
        formatCurrencyColumn(sellingPriceColumn);
        formatCurrencyColumn(costPriceColumn);
    }

    private void formatCurrencyColumn(TableColumn<InventoryItemDTO, BigDecimal> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(price));
                }
            }
        });
    }


    private void loadInventoryItems() {
        handleFilterOrSearchAction(null); // Use combined filter/search logic
    }

    @FXML
    private void handleFilterOrSearchAction(ActionEvent event) {
        String searchQuery = searchField.getText() != null ? searchField.getText().trim() : "";
        ProductDTO selectedProduct = productFilterComboBox.getValue();
        boolean lowStockOnly = lowStockOnlyCheckBox.isSelected();
        boolean showInactive = showInactiveCheckBox.isSelected();

        List<InventoryItemDTO> items = new ArrayList<>();
        try {
            if (lowStockOnly) {
                items = inventoryItemService.getLowStockItemsReport();
                // Further filter low stock items by product and search query if needed (client-side or enhance service)
                if (selectedProduct != null) {
                    items.removeIf(item -> item.getProductId() != selectedProduct.getProductId());
                }
                if (!searchQuery.isEmpty()) {
                    String lowerQuery = searchQuery.toLowerCase();
                    items.removeIf(item ->
                        !(item.getItemSpecificNameEn() != null && item.getItemSpecificNameEn().toLowerCase().contains(lowerQuery)) &&
                        !(item.getBrandName() != null && item.getBrandName().toLowerCase().contains(lowerQuery)) &&
                        !(item.getProductNameEn() != null && item.getProductNameEn().toLowerCase().contains(lowerQuery))
                        // Add productNameAr, itemSpecificNameAr if needed for search
                    );
                }
                // Low stock report already considers active status usually, but if not:
                 if (!showInactive) {
                    items.removeIf(item -> !item.isActive());
                }

            } else {
                if (selectedProduct != null) {
                    items = inventoryItemService.getInventoryItemsByProduct(selectedProduct.getProductId(), showInactive);
                    // Further filter by search query if provided
                    if (!searchQuery.isEmpty()) {
                         String lowerQuery = searchQuery.toLowerCase();
                         items.removeIf(item ->
                            !(item.getItemSpecificNameEn() != null && item.getItemSpecificNameEn().toLowerCase().contains(lowerQuery)) &&
                            !(item.getBrandName() != null && item.getBrandName().toLowerCase().contains(lowerQuery))
                            // Product name already filtered by getInventoryItemsByProduct, no need to re-filter productNameEn here
                        );
                    }
                } else if (!searchQuery.isEmpty()) {
                    items = inventoryItemService.searchInventoryItems(searchQuery, showInactive);
                } else {
                    items = inventoryItemService.getAllInventoryItems(showInactive);
                }
            }
            itemObservableList.setAll(items);
            inventoryItemsTable.setItems(itemObservableList);
        } catch (InventoryItemServiceException e) {
            logger.error("Error fetching inventory items: {}", e.getMessage(), e);
            showErrorAlert("Load Error", "Failed to load inventory items: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearFiltersAction(ActionEvent event) {
        searchField.clear();
        productFilterComboBox.getSelectionModel().selectFirst(); // Select "All Products"
        lowStockOnlyCheckBox.setSelected(false);
        showInactiveCheckBox.setSelected(false); // Or whatever default is desired
        loadInventoryItems(); // This will call handleFilterOrSearchAction with cleared filters
    }


    @FXML
    private void handleAddInventoryItemButtonAction(ActionEvent event) {
        showInventoryItemFormDialog(null);
    }

    @FXML
    private void handleEditInventoryItemButtonAction(ActionEvent event) {
        InventoryItemDTO selectedItem = inventoryItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showErrorAlert("No Item Selected", "Please select an item to edit.");
            return;
        }
        showInventoryItemFormDialog(selectedItem);
    }

    private void showInventoryItemFormDialog(InventoryItemDTO itemToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/InventoryItemFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            InventoryItemFormDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(itemToEdit == null ?
                                 MessageProvider.getString("inventoryitem.dialog.add.title") :
                                 MessageProvider.getString("inventoryitem.dialog.edit.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner((Stage) addItemButton.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogRoot));

            controller.initializeDialog(this.inventoryItemService, this.productService, dialogStage, itemToEdit);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadInventoryItems();
                showSuccessAlert(MessageProvider.getString("inventoryitem.success.saved"));
            }
        } catch (IOException e) {
            logger.error("Failed to load InventoryItemFormDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the inventory item form.");
        }
    }

    @FXML
    private void handleToggleActiveButtonAction(ActionEvent event) {
        InventoryItemDTO selectedItem = inventoryItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showErrorAlert("No Item Selected", "Please select an item to toggle its status.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                                       MessageProvider.getString("bankname.confirm.toggleActive.content"), // Reusing general key
                                       ButtonType.YES, ButtonType.NO);
        confirmation.setTitle(MessageProvider.getString("bankname.confirm.toggleActive.title"));
        confirmation.setHeaderText(null);
        confirmation.initOwner((Stage) toggleActiveButton.getScene().getWindow());
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                inventoryItemService.toggleActiveStatus(selectedItem.getInventoryItemId());
                showSuccessAlert(MessageProvider.getString("inventoryitem.success.statusChanged"));
                loadInventoryItems();
            } catch (InventoryItemServiceException e) {
                logger.error("Error toggling item status for ID {}: {}", selectedItem.getInventoryItemId(), e.getMessage(), e);
                showErrorAlert("Error", e.getMessage());
            }
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("inventoryitem.management.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner((Stage) inventoryItemsTable.getScene().getWindow());
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner((Stage) inventoryItemsTable.getScene().getWindow());
        alert.showAndWait();
    }
}
