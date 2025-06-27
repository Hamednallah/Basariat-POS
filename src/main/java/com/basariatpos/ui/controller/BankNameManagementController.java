package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher; // To get BankNameService instance
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.service.BankNameException;
import com.basariatpos.service.BankNameService;
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

public class BankNameManagementController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(BankNameManagementController.class);

    @FXML private TableView<BankNameDTO> bankNamesTable;
    @FXML private TableColumn<BankNameDTO, String> nameEnColumn;
    @FXML private TableColumn<BankNameDTO, String> nameArColumn;
    @FXML private TableColumn<BankNameDTO, String> statusColumn;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button toggleActiveButton;
    @FXML private BorderPane bankNameManagementRootPane; // For RTL

    private BankNameService bankNameService;
    private final ObservableList<BankNameDTO> bankNameObservableList = FXCollections.observableArrayList();
    private Stage currentStage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.bankNameService = AppLauncher.getBankNameService();
        if (this.bankNameService == null) {
            logger.error("BankNameService is null. Cannot perform operations.");
            showErrorAlert("Critical Error", "Bank Name Service is not available.");
            if (addButton != null) addButton.setDisable(true);
            if (editButton != null) editButton.setDisable(true);
            if (toggleActiveButton != null) toggleActiveButton.setDisable(true);
            return;
        }

        updateNodeOrientation();
        setupTableColumns();
        loadBankNames();

        bankNamesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean itemSelected = newSelection != null;
            editButton.setDisable(!itemSelected);
            toggleActiveButton.setDisable(!itemSelected);
        });
        logger.info("BankNameManagementController initialized.");
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
         // Update node orientation if stage is set after initialize (e.g. view loaded into main frame)
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (bankNameManagementRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                bankNameManagementRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                bankNameManagementRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("bankNameManagementRootPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    // Setter for service if needed by AppLauncher or other setup mechanism
    public void setBankNameService(BankNameService bankNameService) {
        this.bankNameService = bankNameService;
        if (bankNamesTable != null) { // If UI is already initialized
            loadBankNames();
        }
    }

    private void setupTableColumns() {
        nameEnColumn.setCellValueFactory(new PropertyValueFactory<>("bankNameEn"));
        nameArColumn.setCellValueFactory(new PropertyValueFactory<>("bankNameAr"));
        statusColumn.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            String statusKey = isActive ? "usermanagement.status.active" : "usermanagement.status.inactive";
            return new SimpleStringProperty(MessageProvider.getString(statusKey));
        });
    }

    private void loadBankNames() {
        try {
            List<BankNameDTO> bankNames = bankNameService.getAllBankNames(true); // Include inactive
            bankNameObservableList.setAll(bankNames);
            bankNamesTable.setItems(bankNameObservableList);
            logger.info("Bank names loaded. Count: {}", bankNames.size());
        } catch (BankNameException e) {
            logger.error("Failed to load bank names: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("bankname.management.title"), "Could not load bank names: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddButtonAction(ActionEvent event) {
        showBankNameFormDialog(null, (Stage) addButton.getScene().getWindow());
    }

    @FXML
    private void handleEditButtonAction(ActionEvent event) {
        BankNameDTO selectedBankName = bankNamesTable.getSelectionModel().getSelectedItem();
        if (selectedBankName == null) {
            showErrorAlert("No Selection", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }
        showBankNameFormDialog(selectedBankName, (Stage) editButton.getScene().getWindow());
    }

    private void showBankNameFormDialog(BankNameDTO bankName, Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/BankNameFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            BankNameFormDialogController controller = loader.getController();
            controller.setBankNameService(this.bankNameService); // Pass the service
            if (bankName != null) {
                controller.setEditableBankName(bankName);
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle(bankName == null ?
                                 MessageProvider.getString("bankname.dialog.add.title") :
                                 MessageProvider.getString("bankname.dialog.edit.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(ownerStage);
            dialogStage.setScene(new Scene(dialogRoot));
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadBankNames(); // Refresh table
                showSuccessAlert(MessageProvider.getString("bankname.success.saved"));
            }
        } catch (IOException e) {
            logger.error("Failed to load BankNameFormDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the bank name form.");
        }
    }

    @FXML
    private void handleToggleActiveButtonAction(ActionEvent event) {
        BankNameDTO selectedBankName = bankNamesTable.getSelectionModel().getSelectedItem();
        if (selectedBankName == null) {
            showErrorAlert("No Selection", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                                       MessageProvider.getString("bankname.confirm.toggleActive.content"),
                                       ButtonType.YES, ButtonType.NO);
        confirmation.setTitle(MessageProvider.getString("bankname.confirm.toggleActive.title"));
        confirmation.setHeaderText(null);
        confirmation.initOwner((Stage) toggleActiveButton.getScene().getWindow());
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                bankNameService.toggleBankNameStatus(selectedBankName.getBankNameId());
                showSuccessAlert(MessageProvider.getString("bankname.success.statusChanged"));
                loadBankNames(); // Refresh table
            } catch (BankNameException e) {
                logger.error("Error toggling bank name status for ID {}: {}", selectedBankName.getBankNameId(), e.getMessage(), e);
                showErrorAlert("Error", e.getMessage());
            }
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("bankname.management.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner((Stage) bankNamesTable.getScene().getWindow());
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner((Stage) bankNamesTable.getScene().getWindow());
        alert.showAndWait();
    }
}
