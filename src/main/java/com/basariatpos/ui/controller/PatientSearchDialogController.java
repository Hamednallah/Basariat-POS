package com.basariatpos.ui.controller;

import com.basariatpos.model.PatientDTO;
import com.basariatpos.service.PatientService;
import com.basariatpos.service.exception.PatientServiceException;
import com.basariatpos.ui.utilui.AlertUtil;
import com.basariatpos.util.AppLogger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PatientSearchDialogController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(PatientSearchDialogController.class);

    @FXML private TextField patientSearchInput;
    @FXML private Button searchPatientBtn;
    @FXML private ListView<PatientDTO> patientResultsList;
    @FXML private Button selectPatientButton;
    @FXML private Button cancelSearchButton;

    private PatientService patientService;
    private Stage dialogStage;
    private PatientDTO selectedPatient = null;
    private ObservableList<PatientDTO> patientData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientResultsList.setItems(patientData);
        patientResultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PatientDTO patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) {
                    setText(null);
                } else {
                    setText(patient.getDisplayFullNameWithId()); // Assumes PatientDTO has this method
                }
            }
        });

        // Double click on list item to select
        patientResultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleSelectPatientButtonAction(null);
            }
        });
    }

    public void setServices(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public PatientDTO getSelectedPatient() {
        return selectedPatient;
    }

    @FXML
    void handleSearchPatientButtonAction(ActionEvent event) {
        String query = patientSearchInput.getText();
        if (query == null || query.trim().isEmpty()) {
            AlertUtil.showWarning("Search Query Empty", "Please enter a name or phone number to search.");
            return;
        }
        if (patientService == null) {
            AlertUtil.showError("Service Error", "Patient Service not available.");
            return;
        }

        try {
            List<PatientDTO> results = patientService.searchPatients(query);
            patientData.setAll(results);
            if (results.isEmpty()) {
                AlertUtil.showInfo("Search Results", "No patients found matching your query: " + query);
            }
        } catch (PatientServiceException e) {
            logger.error("Error searching patients with query '{}': {}", query, e.getMessage(), e);
            AlertUtil.showError("Search Error", "Failed to retrieve patient data: " + e.getMessage());
        }
    }

    @FXML
    void handleSelectPatientButtonAction(ActionEvent event) {
        selectedPatient = patientResultsList.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            AlertUtil.showWarning("No Selection", "Please select a patient from the list.");
            return;
        }
        closeDialog();
    }

    @FXML
    void handleCancelButtonAction(ActionEvent event) {
        selectedPatient = null;
        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
