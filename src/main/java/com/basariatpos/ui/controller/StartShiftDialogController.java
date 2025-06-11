package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class StartShiftDialogController {

    private static final Logger logger = AppLogger.getLogger(StartShiftDialogController.class);

    @FXML private TextField openingFloatFld;
    @FXML private Button startButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private boolean saved = false;
    private BigDecimal openingFloat;
    private NumberFormat numberFormat;


    public void initialize() {
        // Initialize number format based on current locale for parsing
        // This assumes that the input might be locale-specific if not using a strict regex.
        // For stricter control, one might use a regex or ensure input is always in a specific format (e.g., using dots for decimals).
        numberFormat = NumberFormat.getNumberInstance(Locale.US); // Using US locale for dot decimal separator consistency
        numberFormat.setGroupingUsed(false); // No thousands separators expected in input generally

        // Add a listener to validate input as user types, or use a TextFormatter
        openingFloatFld.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.,]\\d*)?")) { // Allow digits, and optionally one dot or comma
                 openingFloatFld.setText(newValue.replaceAll("[^\\d\\.,]", ""));
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleStartButtonAction(ActionEvent event) {
        if (validateInput()) {
            saved = true;
            closeDialog();
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateInput() {
        String floatText = openingFloatFld.getText();
        if (floatText == null || floatText.trim().isEmpty()) {
            showErrorAlert(MessageProvider.getString("startshiftdialog.error.openingFloat.invalid"), "Opening float cannot be empty.");
            return false;
        }
        try {
            // Normalize comma to dot for BigDecimal conversion if using locale-agnostic parsing
            String normalizedText = floatText.replace(',', '.');
            openingFloat = new BigDecimal(normalizedText);
            if (openingFloat.compareTo(BigDecimal.ZERO) < 0) {
                showErrorAlert(MessageProvider.getString("startshiftdialog.error.openingFloat.invalid"), "Opening float must be zero or positive.");
                return false;
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid number format for opening float: {}", floatText, e);
            showErrorAlert(MessageProvider.getString("startshiftdialog.error.openingFloat.invalid"), "Invalid number format. Please use digits and optionally a decimal point.");
            return false;
        }
        return true;
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

    public BigDecimal getOpeningFloat() {
        return openingFloat;
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.close();
        }
    }
}
