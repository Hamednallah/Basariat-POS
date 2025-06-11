package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ShiftDTO;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class InterruptedShiftDialogController {

    private static final Logger logger = AppLogger.getLogger(InterruptedShiftDialogController.class);

    @FXML private Label messageLabel;
    @FXML private Button resumeButton;
    @FXML private Button forciblyEndButton;
    @FXML private Button logoutButton;

    private Stage dialogStage;
    private InterruptedShiftAction result = InterruptedShiftAction.CANCEL; // Default to cancel/logout

    public enum InterruptedShiftAction {
        RESUME,
        FORCIBLY_END,
        CANCEL // Or LOGOUT
    }

    public void initializeDialog(ShiftDTO interruptedShift, Stage stage) {
        this.dialogStage = stage;
        if (interruptedShift != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                             .withLocale(MessageProvider.getBundle().getLocale());
            String formattedStartTime = interruptedShift.getStartTime() != null ?
                                        interruptedShift.getStartTime().format(formatter) : "N/A";
            String username = interruptedShift.getStartedByUsername() != null ?
                              interruptedShift.getStartedByUsername() : "Unknown User";

            messageLabel.setText(MessageProvider.getString(
                "interruptedshift.dialog.message",
                String.valueOf(interruptedShift.getShiftId()),
                formattedStartTime,
                username
            ));
        } else {
            // Should not happen if dialog is only shown when an interrupted shift exists
            messageLabel.setText("Error: Interrupted shift data not available.");
            resumeButton.setDisable(true);
            forciblyEndButton.setDisable(true);
        }
    }

    @FXML
    private void handleResumeButtonAction(ActionEvent event) {
        result = InterruptedShiftAction.RESUME;
        closeDialog();
    }

    @FXML
    private void handleForciblyEndButtonAction(ActionEvent event) {
        result = InterruptedShiftAction.FORCIBLY_END;
        closeDialog();
    }

    @FXML
    private void handleLogoutButtonAction(ActionEvent event) {
        result = InterruptedShiftAction.CANCEL; // Or LOGOUT
        closeDialog();
    }

    public InterruptedShiftAction getResult() {
        return result;
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
