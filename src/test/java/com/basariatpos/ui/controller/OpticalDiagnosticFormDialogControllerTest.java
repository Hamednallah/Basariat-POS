package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.OpticalDiagnosticDTO;
import com.basariatpos.service.DiagnosticException;
import com.basariatpos.service.OpticalDiagnosticService;
import com.basariatpos.service.exception.DiagnosticValidationException;

import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter; // For verifying applyFormatter
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OpticalDiagnosticFormDialogControllerTest {

    @Mock private Label dialogTitleLabel;
    @Mock private DatePicker diagnosticDateField;
    @Mock private CheckBox isContactLensRxCheckBox;
    @Mock private VBox contactLensDetailsBox;
    @Mock private TextArea contactLensDetailsArea;
    @Mock private TextField odSphField, odCylField, odAxisField, odAddField;
    @Mock private TextField osSphField, osCylField, osAxisField, osAddField;
    @Mock private TextField ipdField;
    @Mock private TextArea remarksArea;
    @Mock private VBox diagnosticFormRootPane; // For RTL
    @Mock private Stage mockDialogStage;
    @Mock private OpticalDiagnosticService mockDiagnosticService;

    @InjectMocks
    private OpticalDiagnosticFormDialogController controller;

    private static ResourceBundle resourceBundle;

    @BeforeAll
    static void setUpClass() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        resourceBundle = MessageProvider.getBundle();
        try {
            new javafx.embed.swing.JFXPanel();
        } catch (Exception e) { /* Ignore */ }
    }

    @BeforeEach
    void setUp() {
        // Manual FXML field injection
        controller.dialogTitleLabel = dialogTitleLabel;
        controller.diagnosticDateField = diagnosticDateField;
        controller.isContactLensRxCheckBox = isContactLensRxCheckBox;
        controller.contactLensDetailsBox = contactLensDetailsBox;
        controller.contactLensDetailsArea = contactLensDetailsArea;
        controller.odSphField = odSphField; controller.odCylField = odCylField; controller.odAxisField = odAxisField; controller.odAddField = odAddField;
        controller.osSphField = osSphField; controller.osCylField = osCylField; controller.osAxisField = osAxisField; controller.osAddField = osAddField;
        controller.ipdField = ipdField;
        controller.remarksArea = remarksArea;
        controller.diagnosticFormRootPane = diagnosticFormRootPane;

        // Mock property bindings for contactLensDetailsBox visibility
        when(contactLensDetailsBox.visibleProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(contactLensDetailsBox.managedProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(isContactLensRxCheckBox.selectedProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));


        controller.initialize(); // Calls updateNodeOrientation and setupFieldFormatters
        // initializeDialog is called by parent controller, simulate it here for tests
        // controller.initializeDialog(mockDiagnosticService, mockDialogStage, null, 1); // Example for add mode
    }

    @Test
    void initialize_setsUpFormattersAndOrientation() {
        verify(odSphField).setTextFormatter(any(TextFormatter.class));
        // ... verify for other formatted fields ...
        verify(diagnosticFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void initializeDialog_addMode_setsTitleAndDefaults() {
        controller.initializeDialog(mockDiagnosticService, mockDialogStage, null, 123);
        verify(dialogTitleLabel).setText(MessageProvider.getString("opticaldiagnostics.form.add.title"));
        verify(diagnosticDateField).setValue(LocalDate.now());
    }

    @Test
    void initializeDialog_editMode_populatesFields() {
        OpticalDiagnosticDTO diag = new OpticalDiagnosticDTO();
        diag.setDiagnosticDate(LocalDate.of(2023, 1, 1));
        diag.setOdSphDist(new BigDecimal("-1.25"));
        // ... set other fields ...

        controller.initializeDialog(mockDiagnosticService, mockDialogStage, diag, 123);

        verify(dialogTitleLabel).setText(MessageProvider.getString("opticaldiagnostics.form.edit.title"));
        verify(diagnosticDateField).setValue(LocalDate.of(2023, 1, 1));
        verify(odSphField).setText("-1.25");
    }

    @Test
    void handleSaveButtonAction_addMode_validInput_recordsDiagnostic() throws DiagnosticException {
        controller.initializeDialog(mockDiagnosticService, mockDialogStage, null, 123);

        when(diagnosticDateField.getValue()).thenReturn(LocalDate.now());
        when(odSphField.getText()).thenReturn("-1.50");
        when(osSphField.getText()).thenReturn("-1.75");
        when(ipdField.getText()).thenReturn("62.5");
        // Mock other fields as needed for a valid record

        controller.handleSaveButtonAction(null);

        assertTrue(controller.isSaved());
        ArgumentCaptor<OpticalDiagnosticDTO> captor = ArgumentCaptor.forClass(OpticalDiagnosticDTO.class);
        verify(mockDiagnosticService).recordDiagnostic(captor.capture());
        assertEquals(123, captor.getValue().getPatientId());
        assertEquals(new BigDecimal("-1.50"), captor.getValue().getOdSphDist());
        verify(mockDialogStage).close();
    }

    @Test
    void handleSaveButtonAction_invalidDate_showsError() {
        controller.initializeDialog(mockDiagnosticService, mockDialogStage, null, 123);
        when(diagnosticDateField.getValue()).thenReturn(null); // Invalid
        // For showErrorAlert to get owner stage
        when(mockDialogStage.getOwner()).thenReturn(null);
        when(diagnosticDateField.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(diagnosticDateField.getScene().getWindow()).thenReturn(mockDialogStage);


        controller.handleSaveButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initialize();

        verify(diagnosticFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
