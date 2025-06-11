package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.CenterProfileDTO;
import com.basariatpos.service.CenterProfileService;
import com.basariatpos.service.ProfileServiceException;
import com.basariatpos.service.ProfileValidationException;
import com.basariatpos.util.AppLogger;
// Assuming AppLauncher provides the service instance, or it's injected otherwise.
import com.basariatpos.main.AppLauncher;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CenterProfileEditorController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(CenterProfileEditorController.class);

    @FXML private TextField centerNameField;
    @FXML private TextField addressLine1Field;
    @FXML private TextField addressLine2Field;
    @FXML private TextField cityField;
    @FXML private TextField countryField;
    @FXML private TextField postalCodeField;
    @FXML private TextField phonePrimaryField;
    @FXML private TextField phoneSecondaryField;
    @FXML private TextField emailAddressField;
    @FXML private TextField websiteField;
    @FXML private TextField logoImagePathField;
    @FXML private Button browseLogoButton;
    @FXML private TextField taxIdentifierField;
    @FXML private TextField currencySymbolField;
    @FXML private TextField currencyCodeField;
    @FXML private TextArea receiptFooterMessageArea;
    @FXML private Button saveChangesButton;

    private CenterProfileService centerProfileService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Service should be injected by the mechanism that loads this view (e.g., MainFrameController)
        // For now, attempting to get it from AppLauncher as a fallback for direct FXML loading if needed,
        // but proper injection is preferred.
        if (this.centerProfileService == null) {
             this.centerProfileService = AppLauncher.getCenterProfileService(); // Ensure AppLauncher has this static getter
        }

        if (this.centerProfileService == null) {
            logger.error("CenterProfileService is null. Cannot perform operations.");
            showErrorAlert(MessageProvider.getString("centerprofile.editor.error.loadFailed"), "Critical service not available.");
            saveChangesButton.setDisable(true); // Disable save if service is missing
            return;
        }

        loadProfileData();
        logger.info("CenterProfileEditorController initialized.");
    }

    // Setter for dependency injection by the calling controller (e.g., MainFrameController)
    public void setCenterProfileService(CenterProfileService service) {
        this.centerProfileService = service;
        // If view is already initialized, reload data with the new service instance
        if (centerNameField != null) { // Check if FXML fields are populated
             loadProfileData();
        }
    }

    private void loadProfileData() {
        if (centerProfileService == null) {
            logger.warn("CenterProfileService not available. Profile data cannot be loaded.");
            // This state should ideally be prevented by ensuring service is injected before/during init.
            return;
        }
        try {
            Optional<CenterProfileDTO> existingProfileOpt = centerProfileService.getCenterProfile();
            if (existingProfileOpt.isPresent()) {
                populateForm(existingProfileOpt.get());
                logger.info("Center profile data loaded into editor form.");
            } else {
                logger.warn("No center profile found in the database.");
                showErrorAlert(MessageProvider.getString("centerprofile.editor.title"),
                               MessageProvider.getString("centerprofile.editor.error.noProfileFound"));
                // Optionally disable form fields if no profile exists, as this editor is for editing.
                // The wizard should be used for initial setup.
                disableFormFields(true);
            }
        } catch (ProfileServiceException e) {
            logger.error("Error loading center profile: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("centerprofile.editor.error.loadFailed"), e.getMessage());
            disableFormFields(true);
        }
    }

    private void populateForm(CenterProfileDTO profile) {
        centerNameField.setText(profile.getCenterName());
        addressLine1Field.setText(profile.getAddressLine1());
        addressLine2Field.setText(profile.getAddressLine2());
        cityField.setText(profile.getCity());
        countryField.setText(profile.getCountry());
        postalCodeField.setText(profile.getPostalCode());
        phonePrimaryField.setText(profile.getPhonePrimary());
        phoneSecondaryField.setText(profile.getPhoneSecondary());
        emailAddressField.setText(profile.getEmailAddress());
        websiteField.setText(profile.getWebsite());
        logoImagePathField.setText(profile.getLogoImagePath());
        taxIdentifierField.setText(profile.getTaxIdentifier());
        currencySymbolField.setText(profile.getCurrencySymbol());
        currencyCodeField.setText(profile.getCurrencyCode());
        receiptFooterMessageArea.setText(profile.getReceiptFooterMessage());
        disableFormFields(false); // Ensure fields are enabled if they were previously disabled
    }

    private void disableFormFields(boolean disable) {
        centerNameField.setDisable(disable);
        addressLine1Field.setDisable(disable);
        // ... disable all other form fields ...
        cityField.setDisable(disable);
        countryField.setDisable(disable);
        // ...
        saveChangesButton.setDisable(disable);
        browseLogoButton.setDisable(disable);
    }


    @FXML
    private void handleSaveChanges(ActionEvent event) {
        if (centerProfileService == null) {
             logger.error("Save changes failed: CenterProfileService is not available.");
             showErrorAlert("Service Error", "Cannot save profile, service unavailable.");
             return;
        }

        CenterProfileDTO dto = collectFormData();
        List<String> validationErrors = validateFormData(dto);

        if (!validationErrors.isEmpty()) {
            String errorMsg = validationErrors.stream().collect(Collectors.joining("\n"));
            showErrorAlert(MessageProvider.getString("validation.general.errorTitle"), errorMsg);
            return;
        }

        try {
            centerProfileService.saveProfile(dto);
            showSuccessAlert(MessageProvider.getString("centerprofile.editor.success.profileUpdated"));
            logger.info("Center profile updated successfully.");
            // If this were a dialog, could close it here.
            // If it's a view in MainFrame, just confirmation is fine.
        } catch (ProfileValidationException e) { // Should be caught by validateFormData, but as safety
            logger.warn("Validation errors after initial check during save: {}", e.getValidationErrors());
            showErrorAlert(MessageProvider.getString("validation.general.errorTitle"),
                           e.getValidationErrors().stream().collect(Collectors.joining("\n")));
        }
        catch (ProfileServiceException e) {
            logger.error("Service exception while saving profile: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("centerprofile.editor.error.loadFailed"), e.getMessage()); // Reusing loadFailed for generic save error
        }
    }

    private CenterProfileDTO collectFormData() {
        CenterProfileDTO dto = new CenterProfileDTO();
        dto.setCenterName(centerNameField.getText());
        dto.setAddressLine1(addressLine1Field.getText());
        dto.setAddressLine2(addressLine2Field.getText());
        dto.setCity(cityField.getText());
        dto.setCountry(countryField.getText());
        dto.setPostalCode(postalCodeField.getText());
        dto.setPhonePrimary(phonePrimaryField.getText());
        dto.setPhoneSecondary(phoneSecondaryField.getText());
        dto.setEmailAddress(emailAddressField.getText());
        dto.setWebsite(websiteField.getText());
        dto.setLogoImagePath(logoImagePathField.getText());
        dto.setTaxIdentifier(taxIdentifierField.getText());
        dto.setCurrencySymbol(currencySymbolField.getText());
        dto.setCurrencyCode(currencyCodeField.getText());
        dto.setReceiptFooterMessage(receiptFooterMessageArea.getText());
        return dto;
    }

    private List<String> validateFormData(CenterProfileDTO dto) {
        List<String> errors = new ArrayList<>();
        // Reusing wizard.centerprofile.* keys which map to validation.centerprofile.* keys for actual messages
        if (isBlank(dto.getCenterName())) errors.add(MessageProvider.getString("validation.centerprofile.centerName.required"));
        if (isBlank(dto.getPhonePrimary())) errors.add(MessageProvider.getString("validation.centerprofile.phonePrimary.required"));
        if (isBlank(dto.getCurrencySymbol())) errors.add(MessageProvider.getString("validation.centerprofile.currencySymbol.required"));
        if (isBlank(dto.getCurrencyCode())) errors.add(MessageProvider.getString("validation.centerprofile.currencyCode.required"));
        // Add other field validations as necessary
        return errors;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @FXML
    private void handleBrowseLogo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(MessageProvider.getString("wizard.centerprofile.logoImagePath"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) browseLogoButton.getScene().getWindow(); // Get current stage
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            logoImagePathField.setText(selectedFile.getAbsolutePath());
            logger.info("Logo image selected: {}", selectedFile.getAbsolutePath());
        }
    }

    private Stage getStage() {
        if(centerNameField != null && centerNameField.getScene() != null) {
            return (Stage) centerNameField.getScene().getWindow();
        }
        return null;
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("centerprofile.editor.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(getStage());
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(getStage());
        alert.showAndWait();
    }
}
