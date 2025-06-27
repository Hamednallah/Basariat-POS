package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.OpticalDiagnosticDTO;
import com.basariatpos.service.DiagnosticException;
import com.basariatpos.service.OpticalDiagnosticService;
import com.basariatpos.service.exception.DiagnosticValidationException;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;


public class OpticalDiagnosticFormDialogController {

    private static final Logger logger = AppLogger.getLogger(OpticalDiagnosticFormDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private DatePicker diagnosticDateField;
    @FXML private CheckBox isContactLensRxCheckBox;
    @FXML private VBox contactLensDetailsBox;
    @FXML private TextArea contactLensDetailsArea;

    @FXML private TextField odSphField;
    @FXML private TextField odCylField;
    @FXML private TextField odAxisField;
    @FXML private TextField odAddField;
    @FXML private TextField osSphField;
    @FXML private TextField osCylField;
    @FXML private TextField osAxisField;
    @FXML private TextField osAddField;
    @FXML private TextField ipdField;
    @FXML private TextArea remarksArea;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private OpticalDiagnosticService diagnosticService;
    private OpticalDiagnosticDTO editableDiagnostic;
    private int currentPatientId; // Required for adding new diagnostic
    private boolean isEditMode = false;
    private boolean saved = false;
    @FXML private VBox diagnosticFormRootPane; // For RTL

    private NumberFormat numberFormat;


    public void initialize() { // Called by FXML loader
        numberFormat = NumberFormat.getNumberInstance(Locale.US);
        if (numberFormat instanceof DecimalFormat) {
            ((DecimalFormat) numberFormat).setParseBigDecimal(true);
        }

        setupFieldFormatters();

        contactLensDetailsBox.visibleProperty().bind(isContactLensRxCheckBox.selectedProperty());
        contactLensDetailsBox.managedProperty().bind(isContactLensRxCheckBox.selectedProperty());

        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (diagnosticFormRootPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                diagnosticFormRootPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                diagnosticFormRootPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("diagnosticFormRootPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    private void setupFieldFormatters() {
        // Allow decimal, minus, plus. Max 3 digits before decimal, 2 after.
        String rxPattern = "[-+]?\\d{0,3}([\\.,]\\d{0,2})?";
        // Axis: 0-180 integers
        String axisPattern = "\\d{0,3}";
        // IPD: positive decimal
        String ipdPattern = "\\d{0,3}([\\.,]\\d{0,2})?";

        applyFormatter(odSphField, rxPattern); applyFormatter(osSphField, rxPattern);
        applyFormatter(odCylField, rxPattern); applyFormatter(osCylField, rxPattern);
        applyFormatter(odAddField, rxPattern); applyFormatter(osAddField, rxPattern);
        applyFormatter(ipdField, ipdPattern);
        applyFormatter(odAxisField, axisPattern); applyFormatter(osAxisField, axisPattern);
    }

    private void applyFormatter(TextField field, String pattern) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches(pattern) || newText.isEmpty()) {
                return change;
            }
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }


    public void initializeDialog(OpticalDiagnosticService service, Stage currentDialogStage, OpticalDiagnosticDTO diagnostic, int patientId) {
        this.diagnosticService = service;
        this.dialogStage = currentDialogStage;
        this.currentPatientId = patientId; // Always need this for context, even if editing

        if (diagnostic != null) {
            this.editableDiagnostic = diagnostic;
            this.isEditMode = true;
            dialogTitleLabel.setText(MessageProvider.getString("opticaldiagnostics.form.edit.title"));
            populateFormFields();
        } else {
            this.editableDiagnostic = new OpticalDiagnosticDTO();
            this.isEditMode = false;
            dialogTitleLabel.setText(MessageProvider.getString("opticaldiagnostics.form.add.title"));
            diagnosticDateField.setValue(LocalDate.now()); // Default to today for new records
        }
    }

    private void populateFormFields() {
        if (editableDiagnostic != null) {
            diagnosticDateField.setValue(editableDiagnostic.getDiagnosticDate());
            isContactLensRxCheckBox.setSelected(editableDiagnostic.isContactLensRx());
            contactLensDetailsArea.setText(editableDiagnostic.getContactLensDetails());

            setBigDecimalField(odSphField, editableDiagnostic.getOdSphDist());
            setBigDecimalField(odCylField, editableDiagnostic.getOdCylDist());
            setIntegerField(odAxisField, editableDiagnostic.getOdAxisDist());
            setBigDecimalField(odAddField, editableDiagnostic.getOdAdd());

            setBigDecimalField(osSphField, editableDiagnostic.getOsSphDist());
            setBigDecimalField(osCylField, editableDiagnostic.getOsCylDist());
            setIntegerField(osAxisField, editableDiagnostic.getOsAxisDist());
            setBigDecimalField(osAddField, editableDiagnostic.getOsAdd());

            setBigDecimalField(ipdField, editableDiagnostic.getIpd());
            remarksArea.setText(editableDiagnostic.getRemarks());
        }
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        List<String> validationErrors = new ArrayList<>();
        if (!validateAndParseFields(validationErrors)) {
            showValidationErrorAlert(validationErrors);
            return;
        }
        // editableDiagnostic DTO is now populated by validateAndParseFields

        try {
            if (isEditMode) {
                diagnosticService.updateDiagnostic(editableDiagnostic);
            } else {
                editableDiagnostic.setPatientId(currentPatientId); // Ensure patient ID is set for new records
                diagnosticService.recordDiagnostic(editableDiagnostic);
            }
            saved = true;
            closeDialog();
        } catch (DiagnosticValidationException e) {
            logger.warn("Validation error saving diagnostic: {}", e.getErrors());
            showValidationErrorAlert(e.getErrors());
        } catch (DiagnosticException e) {
            logger.error("Error saving diagnostic: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("opticaldiagnostics.error.generic"), e.getMessage());
        }
    }

    @FXML
    private void handleContactLensRxToggle(ActionEvent event) {
        // Visibility is bound, this is for any additional logic if needed
    }


    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateAndParseFields(List<String> errors) {
        // Date
        if (diagnosticDateField.getValue() == null) {
            errors.add(MessageProvider.getString("opticaldiagnostics.validation.dateRequired"));
        } else {
            editableDiagnostic.setDiagnosticDate(diagnosticDateField.getValue());
        }

        // Contact Lens
        editableDiagnostic.setContactLensRx(isContactLensRxCheckBox.isSelected());
        if (editableDiagnostic.isContactLensRx() && (contactLensDetailsArea.getText() == null || contactLensDetailsArea.getText().trim().isEmpty())) {
            errors.add(MessageProvider.getString("opticaldiagnostics.validation.clDetailsRequired"));
        } else {
            editableDiagnostic.setContactLensDetails(contactLensDetailsArea.getText());
        }

        // Spectacle Rx
        editableDiagnostic.setOdSphDist(parseBigDecimalField(odSphField, "OD SPH", errors));
        editableDiagnostic.setOdCylDist(parseBigDecimalField(odCylField, "OD CYL", errors));
        editableDiagnostic.setOdAxisDist(parseIntegerField(odAxisField, "OD AXIS", 0, 180, errors));
        editableDiagnostic.setOdAdd(parseBigDecimalField(odAddField, "OD ADD", errors));

        editableDiagnostic.setOsSphDist(parseBigDecimalField(osSphField, "OS SPH", errors));
        editableDiagnostic.setOsCylDist(parseBigDecimalField(osCylField, "OS CYL", errors));
        editableDiagnostic.setOsAxisDist(parseIntegerField(osAxisField, "OS AXIS", 0, 180, errors));
        editableDiagnostic.setOsAdd(parseBigDecimalField(osAddField, "OS ADD", errors));

        editableDiagnostic.setIpd(parseBigDecimalField(ipdField, "IPD", errors));
        editableDiagnostic.setRemarks(remarksArea.getText());

        return errors.isEmpty();
    }

    private BigDecimal parseBigDecimalField(TextField field, String fieldNameKeySuffix, List<String> errors) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            return null; // Allow null for non-mandatory Rx fields
        }
        try {
            // Normalize comma to dot from user input before parsing
            String normalizedText = text.trim().replace(',', '.');
            if (normalizedText.equals("+") || normalizedText.equals("-")) return null; // Partial input
            return new BigDecimal(normalizedText);
        } catch (NumberFormatException e) {
            errors.add(MessageProvider.getString("opticaldiagnostics.error.numericFormat", MessageProvider.getString("opticaldiagnostics.label." + fieldNameKeySuffix.toLowerCase().replace(" ", ""))));
            return null;
        }
    }

    private Integer parseIntegerField(TextField field, String fieldNameKeySuffix, int min, int max, List<String> errors) {
        String text = field.getText();
        if (text == null || text.trim().isEmpty()) {
            return null; // Allow null for non-mandatory Rx fields like Axis if Cyl is also null/zero
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value < min || value > max) {
                 errors.add(MessageProvider.getString("opticaldiagnostics.error.axisRange", MessageProvider.getString("opticaldiagnostics.label." + fieldNameKeySuffix.toLowerCase().replace(" ", ""))));
            }
            return value;
        } catch (NumberFormatException e) {
             errors.add(MessageProvider.getString("opticaldiagnostics.error.numericFormat", MessageProvider.getString("opticaldiagnostics.label." + fieldNameKeySuffix.toLowerCase().replace(" ", ""))));
            return null;
        }
    }

    private void setBigDecimalField(TextField field, BigDecimal value) {
        field.setText(value != null ? value.toPlainString() : "");
    }

    private void setIntegerField(TextField field, Integer value) {
        field.setText(value != null ? value.toString() : "");
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

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
