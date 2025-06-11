package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ApplicationSettingDTO;
import com.basariatpos.service.ApplicationSettingsService;
import com.basariatpos.service.SettingException;
import com.basariatpos.service.SettingNotFoundException;
import com.basariatpos.service.ValidationException;
import com.basariatpos.util.AppLogger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AppSettingsManagementController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(AppSettingsManagementController.class);

    @FXML private TableView<ApplicationSettingDTO> settingsTable;
    @FXML private TableColumn<ApplicationSettingDTO, String> keyColumn;
    @FXML private TableColumn<ApplicationSettingDTO, String> valueColumn;
    @FXML private TableColumn<ApplicationSettingDTO, String> descriptionColumn;

    private ApplicationSettingsService applicationSettingsService;
    private final ObservableList<ApplicationSettingDTO> settingsObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.applicationSettingsService = AppLauncher.getApplicationSettingsService();
        if (this.applicationSettingsService == null) {
            logger.error("ApplicationSettingsService is null. Cannot perform operations.");
            showErrorAlert("Critical Error", "Application Settings Service is not available.", null);
            return;
        }

        setupTableColumns();
        loadSettings();
        logger.info("AppSettingsManagementController initialized.");
    }

    public void setApplicationSettingsService(ApplicationSettingsService service) {
        this.applicationSettingsService = service;
        if (settingsTable != null) { // If UI is already initialized
            loadSettings();
        }
    }


    private void setupTableColumns() {
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("settingKey"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        valueColumn.setCellValueFactory(new PropertyValueFactory<>("settingValue"));
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setOnEditCommit(event -> {
            ApplicationSettingDTO setting = event.getRowValue();
            String oldValue = setting.getSettingValue(); // For logging or revert on failure
            String newValue = event.getNewValue();

            // Prevent editing key of "app.version" if it's special
            if ("app.version".equals(setting.getSettingKey()) && !newValue.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?")) {
                 showErrorAlert(MessageProvider.getString("appsettings.management.title"),
                               "Version format must be like X.Y.Z or X.Y.Z-SNAPSHOT.",
                               (Stage) settingsTable.getScene().getWindow());
                settingsTable.getItems().set(event.getTablePosition().getRow(), setting); //Revert change in UI
                return;
            }


            try {
                applicationSettingsService.updateSettingValue(setting.getSettingKey(), newValue);
                setting.setSettingValue(newValue); // Update DTO in table
                settingsTable.refresh(); // Refresh row to show new value
                showSuccessAlert(MessageProvider.getString("appsettings.success.valueUpdated"));
                logger.info("Setting '{}' updated from '{}' to '{}'", setting.getSettingKey(), oldValue, newValue);
            } catch (SettingNotFoundException e) {
                logger.error("Setting key '{}' not found during edit commit: {}", setting.getSettingKey(), e.getMessage(), e);
                showErrorAlert(MessageProvider.getString("appsettings.error.updateFailed"),
                               MessageProvider.getString("appsettings.error.notFound"),
                               (Stage) settingsTable.getScene().getWindow());
                setting.setSettingValue(oldValue); // Revert DTO
                settingsTable.refresh();
            } catch (ValidationException e) {
                 logger.warn("Validation error updating setting '{}': {}", setting.getSettingKey(), e.getErrors());
                 showErrorAlert(MessageProvider.getString("validation.general.errorTitle"), String.join("\n", e.getErrors()), (Stage) settingsTable.getScene().getWindow());
                 setting.setSettingValue(oldValue); // Revert DTO
                 settingsTable.refresh();
            }
            catch (SettingException e) {
                logger.error("Failed to update setting '{}': {}", setting.getSettingKey(), e.getMessage(), e);
                showErrorAlert(MessageProvider.getString("appsettings.error.updateFailed"), e.getMessage(), (Stage) settingsTable.getScene().getWindow());
                setting.setSettingValue(oldValue); // Revert DTO
                settingsTable.refresh();
            }
        });
    }

    private void loadSettings() {
        try {
            List<ApplicationSettingDTO> settings = applicationSettingsService.getAllApplicationSettings();
            settingsObservableList.setAll(settings);
            settingsTable.setItems(settingsObservableList);
            logger.info("Application settings loaded. Count: {}", settings.size());
        } catch (SettingException e) {
            logger.error("Failed to load application settings: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("appsettings.management.title"),
                           "Could not load settings: " + e.getMessage(),
                           (Stage) settingsTable.getScene().getWindow());
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("appsettings.management.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(getStage());
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content, Stage owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(owner != null ? owner : getStage());
        alert.showAndWait();
    }

    private Stage getStage() {
        if (settingsTable != null && settingsTable.getScene() != null) {
            return (Stage) settingsTable.getScene().getWindow();
        }
        return null;
    }
}
