package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.PatientDTO;
import com.basariatpos.service.PatientService;
import com.basariatpos.service.exception.PatientAlreadyExistsException;
import com.basariatpos.service.exception.PatientServiceException;
import com.basariatpos.service.exception.PatientValidationException;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.List; // For ValidationException errors

public class PatientFormDialogController { // No Initializable needed unless @FXML fields need it before setters

    private static final Logger logger = AppLogger.getLogger(PatientFormDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private Label systemIdLabel;
    @FXML private TextField systemIdField;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextArea addressArea;
    @FXML private CheckBox whatsappOptInCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private PatientService patientService;
    private PatientDTO editablePatient; // The patient being edited, or null if adding
    private PatientDTO savedPatient;    // To store the successfully saved/created patient
    private boolean isEditMode = false;
    private boolean saved = false;

    // No-args constructor is used by FXMLLoader
    public PatientFormDialogController() {}

    /**
     * Initializes the dialog for either adding a new patient or editing an existing one.
     * This method should be called by the parent controller after loading the FXML.
     * @param patient The patient to edit, or null if adding a new patient.
     */
    public void initializeDialog(PatientDTO patient) {
        if (patient != null) {
            this.editablePatient = patient;
            this.isEditMode = true;
            dialogTitleLabel.setText(MessageProvider.getString("patientmanagement.dialog.edit.title"));
            populateFormFields();
        } else {
            this.editablePatient = new PatientDTO(); // Create new DTO for add mode
            this.isEditMode = false;
            dialogTitleLabel.setText(MessageProvider.getString("patientmanagement.dialog.add.title"));
            // Ensure fields are clear for add mode (or rely on FXML defaults)
            systemIdLabel.setVisible(false); systemIdLabel.setManaged(false);
            systemIdField.setVisible(false); systemIdField.setManaged(false);
            fullNameField.clear();
            phoneNumberField.clear();
            addressArea.clear();
            whatsappOptInCheckBox.setSelected(false); // Default opt-in can be true if desired
        }
    }

    private void populateFormFields() {
        if (editablePatient != null) {
            systemIdField.setText(editablePatient.getSystemPatientId());
            systemIdLabel.setVisible(true); systemIdLabel.setManaged(true);
            systemIdField.setVisible(true); systemIdField.setManaged(true);

            fullNameField.setText(editablePatient.getFullName());
            phoneNumberField.setText(editablePatient.getPhoneNumber());
            addressArea.setText(editablePatient.getAddress());
            whatsappOptInCheckBox.setSelected(editablePatient.isWhatsappOptIn());
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }


    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        // Populate DTO from form fields
        editablePatient.setFullName(fullNameField.getText().trim());
        editablePatient.setPhoneNumber(phoneNumberField.getText().trim());
        editablePatient.setAddress(addressArea.getText() != null ? addressArea.getText().trim() : null);
        editablePatient.setWhatsappOptIn(whatsappOptInCheckBox.isSelected());
        // systemPatientId is not editable / set by service for new patients
        // createdByUserId is set by service for new patients

        try {
            if (isEditMode) {
                savedPatient = patientService.updatePatient(editablePatient);
            } else {
                savedPatient = patientService.createPatient(editablePatient);
            }
            saved = true;
            closeDialog();
        } catch (PatientValidationException e) {
            logger.warn("Validation error saving patient: {}", e.getErrors());
            showValidationErrorAlert(e.getErrors());
        } catch (PatientAlreadyExistsException e) {
            logger.warn("Patient already exists: {}", e.getMessage());
            showErrorAlert(MessageProvider.getString("patient.error.phoneExists"), e.getMessage());
        } catch (PatientServiceException e) {
            logger.error("Error saving patient: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("patient.error.generic"), e.getMessage());
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();
        if (fullNameField.getText() == null || fullNameField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("usermanagement.validation.fullName.required")); // Reusing general key
        }
        if (phoneNumberField.getText() == null || phoneNumberField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("usermanagement.validation.phoneNumber.required")); // Assuming such key exists or create specific
        } else if (!phoneNumberField.getText().matches("\\+?[0-9\\s\\(\\)-]{7,}")) { // Basic phone format validation
            errors.add(MessageProvider.getString("patient.error.validation") + ": Invalid phone number format."); // Needs specific key
        }
        // Add other field validations as necessary

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

    public PatientDTO getSavedPatient() {
        return savedPatient;
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
