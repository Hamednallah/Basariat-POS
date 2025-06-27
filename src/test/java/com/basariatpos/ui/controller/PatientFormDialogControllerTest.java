package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.PatientDTO;
import com.basariatpos.service.PatientService;
import com.basariatpos.service.exception.PatientAlreadyExistsException;
import com.basariatpos.service.exception.PatientServiceException;
import com.basariatpos.service.exception.PatientValidationException;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientFormDialogControllerTest {

    @Mock private Label dialogTitleLabel;
    @Mock private Label systemIdLabel;
    @Mock private TextField systemIdField;
    @Mock private TextField fullNameField;
    @Mock private TextField phoneNumberField;
    @Mock private TextArea addressArea;
    @Mock private CheckBox whatsappOptInCheckBox;
    @Mock private VBox patientFormRootPane; // For RTL
    @Mock private Stage mockDialogStage;
    @Mock private PatientService mockPatientService;

    @InjectMocks
    private PatientFormDialogController controller;

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
        // Manual injection of @FXML mocks
        controller.dialogTitleLabel = dialogTitleLabel;
        controller.systemIdLabel = systemIdLabel;
        controller.systemIdField = systemIdField;
        controller.fullNameField = fullNameField;
        controller.phoneNumberField = phoneNumberField;
        controller.addressArea = addressArea;
        controller.whatsappOptInCheckBox = whatsappOptInCheckBox;
        controller.patientFormRootPane = patientFormRootPane;

        controller.setPatientService(mockPatientService);
        // initializeDialog is called by parent, here we simulate for add mode initially
        // controller.initializeDialog(null); // This will also call updateNodeOrientation
        // setDialogStage needs to be called to ensure nodeOrientation logic within it also runs if scene is available
        controller.setDialogStage(mockDialogStage);
    }

    @Test
    void initializeDialog_addMode_setsTitleAndOrientation() {
        controller.initializeDialog(null); // Explicitly call for this test case
        verify(dialogTitleLabel).setText(MessageProvider.getString("patientmanagement.dialog.add.title"));
        verify(systemIdLabel).setVisible(false);
        verify(patientFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void initializeDialog_editMode_populatesFieldsAndSetsOrientation() {
        PatientDTO patient = new PatientDTO();
        patient.setSystemPatientId("PAT-001");
        patient.setFullName("Existing Patient");
        patient.setPhoneNumber("1234567890");
        patient.setAddress("Some Address");
        patient.setWhatsappOptIn(true);

        controller.initializeDialog(patient);

        verify(dialogTitleLabel).setText(MessageProvider.getString("patientmanagement.dialog.edit.title"));
        verify(systemIdField).setText("PAT-001");
        verify(fullNameField).setText("Existing Patient");
        verify(phoneNumberField).setText("1234567890");
        verify(addressArea).setText("Some Address");
        verify(whatsappOptInCheckBox).setSelected(true);
        verify(patientFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void handleSaveButtonAction_addMode_validInput_createsPatient() throws PatientServiceException {
        controller.initializeDialog(null); // Ensure add mode
        when(fullNameField.getText()).thenReturn("New Patient");
        when(phoneNumberField.getText()).thenReturn("0987654321");
        when(addressArea.getText()).thenReturn("New Address");
        when(whatsappOptInCheckBox.isSelected()).thenReturn(true);

        PatientDTO createdPatient = new PatientDTO(); // Mock returned patient
        when(mockPatientService.createPatient(any(PatientDTO.class))).thenReturn(createdPatient);

        controller.handleSaveButtonAction(null);

        assertTrue(controller.isSaved());
        assertEquals(createdPatient, controller.getSavedPatient());
        verify(mockDialogStage).close();
        ArgumentCaptor<PatientDTO> captor = ArgumentCaptor.forClass(PatientDTO.class);
        verify(mockPatientService).createPatient(captor.capture());
        assertEquals("New Patient", captor.getValue().getFullName());
        assertTrue(captor.getValue().isWhatsappOptIn());
    }

    @Test
    void handleSaveButtonAction_editMode_validInput_updatesPatient() throws PatientServiceException {
        PatientDTO existingPatient = new PatientDTO();
        existingPatient.setPatientId(1L);
        existingPatient.setSystemPatientId("PAT-001");
        controller.initializeDialog(existingPatient); // Ensure edit mode

        when(fullNameField.getText()).thenReturn("Updated Patient");
        when(phoneNumberField.getText()).thenReturn("1122334455");
        when(addressArea.getText()).thenReturn("Updated Address");
        when(whatsappOptInCheckBox.isSelected()).thenReturn(false);

        PatientDTO updatedPatient = new PatientDTO(); // Mock returned patient
        when(mockPatientService.updatePatient(any(PatientDTO.class))).thenReturn(updatedPatient);


        controller.handleSaveButtonAction(null);

        assertTrue(controller.isSaved());
        assertEquals(updatedPatient, controller.getSavedPatient());
        verify(mockDialogStage).close();
        ArgumentCaptor<PatientDTO> captor = ArgumentCaptor.forClass(PatientDTO.class);
        verify(mockPatientService).updatePatient(captor.capture());
        assertEquals("Updated Patient", captor.getValue().getFullName());
        assertFalse(captor.getValue().isWhatsappOptIn());
        assertEquals(1L, captor.getValue().getPatientId());
    }


    @Test
    void handleSaveButtonAction_invalidFullName_showsError() {
        controller.initializeDialog(null);
        when(fullNameField.getText()).thenReturn(""); // Invalid
        when(phoneNumberField.getText()).thenReturn("1234567890");
         // For showErrorAlert to get owner stage
        when(mockDialogStage.getOwner()).thenReturn(null);
        when(fullNameField.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(fullNameField.getScene().getWindow()).thenReturn(mockDialogStage);

        controller.handleSaveButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void initializeDialog_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        // ResourceBundle isn't used by this controller's initializeDialog directly for text, but for node orientation

        controller.initializeDialog(null); // Call with Arabic locale set

        verify(patientFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }

}
