package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ShiftDTO;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Pair; // For returning two values (cash, notes)
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class EndShiftDialogController {

    private static final Logger logger = AppLogger.getLogger(EndShiftDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private Label shiftIdLabel;
    @FXML private Label startedByLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label openingFloatLabel;
    @FXML private Label closingCashLabel; // To change text if forced
    @FXML private TextField closingCashField;
    @FXML private Label notesLabel; // To change text if forced
    @FXML private TextArea notesArea;
    @FXML private Button endShiftButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private ShiftDTO currentShift;
    private boolean saved = false;
    private Pair<BigDecimal, String> result;
    private boolean isForcedEnd = false;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00"); // Basic currency format


    public void initializeDialog(ShiftDTO shift, Stage stage, boolean forcedEnd) {
        this.currentShift = shift;
        this.dialogStage = stage;
        this.isForcedEnd = forcedEnd;

        if (forcedEnd) {
            dialogTitleLabel.setText(MessageProvider.getString("endshiftdialog.title.forced"));
            notesLabel.setText(MessageProvider.getString("endshiftdialog.label.notes.forced"));
            // Make notes mandatory for forced end in validation logic
        } else {
            dialogTitleLabel.setText(MessageProvider.getString("endshiftdialog.title"));
            notesLabel.setText(MessageProvider.getString("endshiftdialog.label.notes"));
        }

        populateShiftDetails();
    }

    private void populateShiftDetails() {
        if (currentShift != null) {
            shiftIdLabel.setText(String.valueOf(currentShift.getShiftId()));
            startedByLabel.setText(currentShift.getStartedByUsername());
            startTimeLabel.setText(currentShift.getStartTime() != null ? currentShift.getStartTime().format(dateTimeFormatter) : "N/A");
            openingFloatLabel.setText(currentShift.getOpeningFloat() != null ? currencyFormat.format(currentShift.getOpeningFloat()) : "N/A");
        }
    }

    @FXML
    private void handleEndShiftButtonAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }
        BigDecimal closingCash = new BigDecimal(closingCashField.getText().replace(',', '.'));
        String notes = notesArea.getText();
        result = new Pair<>(closingCash, notes);
        saved = true;
        closeDialog();
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();
        String cashText = closingCashField.getText();
        if (cashText == null || cashText.trim().isEmpty()) {
            errors.add(MessageProvider.getString("endshiftdialog.error.closingCash.invalid")); // Or more specific "required"
        } else {
            try {
                BigDecimal cash = new BigDecimal(cashText.replace(',', '.'));
                if (cash.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(MessageProvider.getString("endshiftdialog.error.closingCash.invalid"));
                }
            } catch (NumberFormatException e) {
                errors.add(MessageProvider.getString("endshiftdialog.error.closingCash.invalid"));
            }
        }

        if (isForcedEnd && (notesArea.getText() == null || notesArea.getText().trim().isEmpty())) {
            errors.add(MessageProvider.getString("endshiftdialog.error.notes.requiredForForcedEnd"));
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
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }

    public boolean isSaved() { return saved; }
    public Pair<BigDecimal, String> getResult() { return result; }

    private void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }
}
