package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.CenterProfileDTO;
import com.basariatpos.service.CenterProfileService;
import com.basariatpos.service.ProfileServiceException;
import com.basariatpos.service.ProfileValidationException;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CenterProfileSetupController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(CenterProfileSetupController.class);

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
    @FXML private Button saveButton;

    // Labels that might need manual text setting if not using %key in FXML for them
    // For this FXML, all labels use %key, so direct @FXML for labels is not strictly needed
    // unless we want to manipulate them beyond text (e.g., visibility).
    @FXML private javafx.scene.layout.AnchorPane rootPane; // For RTL
    @FXML private javafx.scene.layout.VBox formVBox; // For RTL, if specific child needs it


    // Assuming CenterProfileService will be injected by a DI framework or manually instantiated.
    // For Sprint 0, manual instantiation is acceptable if no DI framework is set up.
    private CenterProfileService centerProfileService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("CenterProfileSetupController initialized with locale: {}", LocaleManager.getCurrentLocale());

        // Set node orientation based on current locale
        if (rootPane != null) { // Ensure rootPane is injected
            if (LocaleManager.ARABIC.equals(LocaleManager.getCurrentLocale())) {
                rootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                rootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("rootPane is null in CenterProfileSetupController.initialize(). RTL/LTR might not be set correctly.");
        }
        // Example for a specific child if needed, though AnchorPane should propagate
        // if (formVBox != null) {
        //     formVBox.setNodeOrientation(LocaleManager.ARABIC.equals(LocaleManager.getCurrentLocale()) ?
        //             javafx.scene.NodeOrientation.RIGHT_TO_LEFT : javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
        // }


        loadExistingProfileData();
    }

    // Setter for dependency injection (if not using constructor injection)
    public void setCenterProfileService(CenterProfileService service) {
        this.centerProfileService = service;
        // After service is set, try loading data again, or ensure service is set before initialize()
        // For robust init, service should be available at initialize time.
        // If service is set post-construction, call loadExistingProfileData() from here too.
        // loadExistingProfileData();
    }


    private void loadExistingProfileData() {
        if (centerProfileService == null) {
            logger.warn("CenterProfileService not available during load. Profile data cannot be pre-filled.");
            // This might happen if the service is not injected before initialize() is called.
            // Consider how the service will be provided. For now, we'll proceed assuming it might be null.
            // A real app would ensure service availability.
            return;
        }
        try {
            Optional<CenterProfileDTO> existingProfileOpt = centerProfileService.getCenterProfile();
            if (existingProfileOpt.isPresent()) {
                CenterProfileDTO profile = existingProfileOpt.get();
                populateForm(profile);
                logger.info("Existing center profile data loaded into the form.");
            } else {
                logger.info("No existing center profile found. Form is ready for new input.");
            }
        } catch (ProfileServiceException e) {
            logger.error("Error loading existing center profile: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("validation.centerprofile.load.error"), e.getMessage());
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
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        if (centerProfileService == null) {
            logger.error("Save operation failed: CenterProfileService is not initialized.");
            showErrorAlert("Service Error", "Center Profile Service is not available. Cannot save.");
            return;
        }

        CenterProfileDTO dto = collectFormData();
        try {
            if (centerProfileService.saveProfile(dto)) {
                showSuccessAlert(MessageProvider.getString("validation.centerprofile.save.success"));
                closeWizard();
            } else {
                // This path might not be hit if saveProfile throws ProfileValidationException instead of returning false
                showErrorAlert(MessageProvider.getString("validation.general.errorTitle"),
                               MessageProvider.getString("validation.general.fillRequiredFields"));
            }
        } catch (ProfileValidationException e) {
            logger.warn("Validation errors while saving profile: {}", e.getValidationErrors());
            String errors = e.getValidationErrors().stream()
                             .map(errKey -> MessageProvider.getString(errKey)) // Assuming errors are i18n keys
                             .collect(Collectors.joining("\n"));
            showErrorAlert(MessageProvider.getString("validation.general.errorTitle"), errors);
        } catch (ProfileServiceException e) {
            logger.error("Service exception while saving profile: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("validation.centerprofile.save.error"), e.getMessage());
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

    @FXML
    private void handleBrowseLogo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(MessageProvider.getString("wizard.centerprofile.logoImagePath")); // Or a more specific "Select Logo Image"
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(getStage());
        if (selectedFile != null) {
            logoImagePathField.setText(selectedFile.getAbsolutePath());
            logger.info("Logo image selected: {}", selectedFile.getAbsolutePath());
        }
    }

    private void closeWizard() {
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
            logger.info("Center Profile Setup Wizard closed.");
        }
    }

    private Stage getStage() {
        // A bit of a trick to get the stage if the button is part of the scene
        if (saveButton != null && saveButton.getScene() != null && saveButton.getScene().getWindow() instanceof Stage) {
            return (Stage) saveButton.getScene().getWindow();
        }
        // Fallback if the above isn't guaranteed (e.g. controller used outside a typical stage context)
        // This part might need adjustment based on how the wizard is launched.
        // If it's always in its own stage, the above should work.
        logger.warn("Could not determine the Stage for the wizard.");
        return null;
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("wizard.centerprofile.title")); // Re-use general title or make specific success title
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
