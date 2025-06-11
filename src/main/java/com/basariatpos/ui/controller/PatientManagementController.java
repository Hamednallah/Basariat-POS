package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.PatientDTO;
import com.basariatpos.service.PatientService;
import com.basariatpos.service.exception.PatientServiceException;
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
import javafx.scene.control.TextField;
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

public class PatientManagementController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(PatientManagementController.class);

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private TableView<PatientDTO> patientsTable;
    @FXML private TableColumn<PatientDTO, String> systemIdColumn;
    @FXML private TableColumn<PatientDTO, String> fullNameColumn;
    @FXML private TableColumn<PatientDTO, String> phoneNumberColumn;
    @FXML private TableColumn<PatientDTO, String> whatsappConsentColumn;

    @FXML private Button addPatientButton;
    @FXML private Button editPatientButton;
    @FXML private Button viewDiagnosticsButton; // Placeholder button

    private PatientService patientService;
    private final ObservableList<PatientDTO> patientObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.patientService = AppLauncher.getPatientService();
        if (this.patientService == null) {
            logger.error("PatientService is null. Cannot perform operations.");
            showErrorAlert("Critical Error", "Patient Service is not available.");
            // Disable UI elements
            searchField.setDisable(true);
            searchButton.setDisable(true);
            clearSearchButton.setDisable(true);
            addPatientButton.setDisable(true);
            editPatientButton.setDisable(true);
            viewDiagnosticsButton.setDisable(true);
            return;
        }

        setupTableColumns();
        loadInitialTableData(); // Load all patients initially or based on default criteria

        patientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean patientSelected = newSelection != null;
            editPatientButton.setDisable(!patientSelected);
            viewDiagnosticsButton.setDisable(!patientSelected); // Enable if a patient is selected
        });

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSearchButtonAction(null); // Pass null or new ActionEvent()
            }
        });
        logger.info("PatientManagementController initialized.");
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
        if (patientsTable != null) { // If UI already initialized
            loadInitialTableData();
        }
    }

    private void setupTableColumns() {
        systemIdColumn.setCellValueFactory(new PropertyValueFactory<>("systemPatientId"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        whatsappConsentColumn.setCellValueFactory(cellData -> {
            boolean hasConsent = cellData.getValue().isWhatsappOptIn();
            String consentKey = hasConsent ? "patientmanagement.consent.yes" : "patientmanagement.consent.no";
            return new SimpleStringProperty(MessageProvider.getString(consentKey));
        });
    }

    private void loadInitialTableData() {
        refreshTableData(patientService.getAllPatients()); // Load all initially
    }

    private void refreshTableData(List<PatientDTO> patients) {
        patientObservableList.setAll(patients);
        patientsTable.setItems(patientObservableList);
        logger.info("Patients table refreshed. Displaying {} patients.", patients.size());
    }


    @FXML
    private void handleSearchButtonAction(ActionEvent event) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadInitialTableData(); // Show all if search is cleared
            return;
        }
        try {
            List<PatientDTO> searchResults = patientService.searchPatientsByNameOrPhone(query);
            refreshTableData(searchResults);
        } catch (PatientServiceException e) {
            logger.error("Error during patient search for query '{}': {}", query, e.getMessage(), e);
            showErrorAlert("Search Error", "Could not perform search: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearSearchButtonAction(ActionEvent event) {
        searchField.clear();
        loadInitialTableData();
    }


    @FXML
    private void handleAddPatientButtonAction(ActionEvent event) {
        showPatientFormDialog(null, (Stage) addPatientButton.getScene().getWindow());
    }

    @FXML
    private void handleEditPatientButtonAction(ActionEvent event) {
        PatientDTO selectedPatient = patientsTable.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showErrorAlert("No Patient Selected", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }
        showPatientFormDialog(selectedPatient, (Stage) editPatientButton.getScene().getWindow());
    }

    private void showPatientFormDialog(PatientDTO patient, Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/PatientFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            PatientFormDialogController controller = loader.getController();
            controller.setPatientService(this.patientService); // Pass service
            controller.initializeDialog(patient); // Pass patient for edit, or null for add

            Stage dialogStage = new Stage();
            dialogStage.setTitle(patient == null ?
                                 MessageProvider.getString("patientmanagement.dialog.add.title") :
                                 MessageProvider.getString("patientmanagement.dialog.edit.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(ownerStage);
            dialogStage.setScene(new Scene(dialogRoot));
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                handleSearchButtonAction(null); // Refresh table (or use loadInitialTableData if search was cleared)
                String successKey = (patient == null) ? "patient.success.added" : "patient.success.updated";
                String message = (patient == null && controller.getSavedPatient() != null) ?
                                 MessageProvider.getString(successKey, controller.getSavedPatient().getSystemPatientId()) :
                                 MessageProvider.getString(successKey);
                showSuccessAlert(message);
            }
        } catch (IOException e) {
            logger.error("Failed to load PatientFormDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the patient form.");
        }
    }

    @FXML
    private void handleViewDiagnosticsButtonAction(ActionEvent event) {
        PatientDTO selectedPatient = patientsTable.getSelectionModel().getSelectedItem();
        if (selectedPatient != null) {
            logger.info("View Diagnostics button clicked for patient ID: {}. This feature is not yet implemented.", selectedPatient.getSystemPatientId());
            showInfoAlert("Feature Not Implemented", "Viewing patient diagnostics will be available in a future update.");
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("patientmanagement.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner((Stage) patientsTable.getScene().getWindow());
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner((Stage) patientsTable.getScene().getWindow());
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner((Stage) patientsTable.getScene().getWindow());
        alert.showAndWait();
    }
}
