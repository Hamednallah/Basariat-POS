package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ResetPasswordDialogController {

    private static final Logger logger = AppLogger.getLogger(ResetPasswordDialogController.class);

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;
    @FXML private Button savePasswordButton;
    @FXML private Button cancelPasswordButton;

    private Stage dialogStage;
    private boolean passwordSaved = false;
    private String newPassword;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleSavePasswordButtonAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }
        newPassword = newPasswordField.getText();
        passwordSaved = true;
        closeDialog();
    }

    @FXML
    private void handleCancelPasswordButtonAction(ActionEvent event) {
        passwordSaved = false;
        closeDialog();
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();
        String pass1 = newPasswordField.getText();
        String pass2 = confirmNewPasswordField.getText();

        if (pass1 == null || pass1.isEmpty()) {
            errors.add(MessageProvider.getString("usermanagement.validation.password.required"));
        } else if (pass1.length() < 8) { // Example length validation
            errors.add(MessageProvider.getString("usermanagement.validation.password.length"));
        }
        // Add more password complexity rules if needed (e.g., using UserService's internal validation logic)

        if (pass2 == null || pass2.isEmpty()) {
            errors.add(MessageProvider.getString("usermanagement.validation.confirmPassword.required"));
        }

        if (!errors.isEmpty()) { // Show errors before checking mismatch if primary validations fail
             showValidationErrorAlert(errors);
             return false;
        }

        if (!pass1.equals(pass2)) {
            errors.add(MessageProvider.getString("usermanagement.error.passwordMismatch"));
        }

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
        alert.initOwner(dialogStage); // Ensure alert is modal to this dialog
        alert.showAndWait();
    }

    public boolean isPasswordSaved() {
        return passwordSaved;
    }

    public String getNewPassword() {
        return newPassword;
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            Stage stage = (Stage) savePasswordButton.getScene().getWindow();
            stage.close();
        }
    }
}
