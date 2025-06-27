package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class StartShiftDialogController {

    private static final Logger logger = AppLogger.getLogger(StartShiftDialogController.class);

    @FXML private TextField openingFloatFld;
    @FXML private Button startButton;
    @FXML private Button cancelButton;
    @FXML private VBox startShiftDialogRootPane;

    private Stage dialogStage;
    private boolean saved = false;
    private BigDecimal openingFloat;
    private NumberFormat numberFormat;


    public void initialize() {
        numberFormat = NumberFormat.getNumberInstance(Locale.US);
        numberFormat.setGroupingUsed(false);

        openingFloatFld.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.,]?\\d*)?")) {
                 openingFloatFld.setText(newValue.replaceAll("[^\\d\\.,]", ""));
            }
        });
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (startShiftDialogRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                startShiftDialogRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                startShiftDialogRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("startShiftDialogRootPane is null during initialize. RTL/LTR might not be set initially.");
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        updateNodeOrientation();
        if (this.dialogStage != null && startShiftDialogRootPane != null && this.dialogStage.getScene() != null) {
            this.dialogStage.getScene().setNodeOrientation(startShiftDialogRootPane.getNodeOrientation());
        }
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
            showErrorAlert(MessageProvider.getString("startshiftdialog.error.openingFloat.invalid"),
                           MessageProvider.getString("validation.general.cannotBeEmpty"));
            return false;
        }
        try {
            String normalizedText = floatText.replace(',', '.');
            openingFloat = new BigDecimal(normalizedText);
            if (openingFloat.compareTo(BigDecimal.ZERO) < 0) {
                showErrorAlert(MessageProvider.getString("startshiftdialog.error.openingFloat.invalid"),
                               MessageProvider.getString("validation.general.mustBeZeroOrPositive"));
                return false;
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid number format for opening float: {}", floatText, e);
            showErrorAlert(MessageProvider.getString("startshiftdialog.error.openingFloat.invalid"),
                           MessageProvider.getString("validation.general.invalidNumberFormat"));
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
        if (dialogStage != null && dialogStage.getScene() != null && dialogStage.getScene().getRoot() != null) {
             alert.getDialogPane().setNodeOrientation(dialogStage.getScene().getRoot().getNodeOrientation());
        }
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
            if (startButton != null && startButton.getScene() != null) {
                 Stage stage = (Stage) startButton.getScene().getWindow();
                 stage.close();
            } else {
                logger.warn("Could not close StartShiftDialog as dialogStage and button scene are null.");
            }
        }
    }
}
