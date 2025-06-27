package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.service.BankNameAlreadyExistsException;
import com.basariatpos.service.BankNameException;
import com.basariatpos.service.BankNameService;
import com.basariatpos.service.ValidationException;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankNameFormDialogControllerTest {

    @Mock private Label dialogTitleLabel;
    @Mock private TextField nameEnField;
    @Mock private TextField nameArField;
    @Mock private CheckBox activeCheckBox;
    @Mock private VBox bankNameFormRootPane; // For RTL
    @Mock private Stage mockDialogStage;
    @Mock private BankNameService mockBankNameService;

    @InjectMocks
    private BankNameFormDialogController controller;

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
        controller.nameEnField = nameEnField;
        controller.nameArField = nameArField;
        controller.activeCheckBox = activeCheckBox;
        controller.bankNameFormRootPane = bankNameFormRootPane;

        controller.setBankNameService(mockBankNameService);
        controller.initialize(null, resourceBundle); // Calls updateNodeOrientation
        controller.setDialogStage(mockDialogStage); // Also calls updateNodeOrientation
    }

    @Test
    void initialize_defaultsAndSetsOrientation() {
        verify(activeCheckBox).setSelected(true);
        verify(bankNameFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void setEditableBankName_populatesFormForEdit() {
        BankNameDTO bank = new BankNameDTO("Test Bank EN", "بنك الاختبار", false);
        bank.setBankNameId(1); // Assuming ID is set

        controller.setEditableBankName(bank);

        verify(dialogTitleLabel).setText(MessageProvider.getString("bankname.dialog.edit.title"));
        verify(nameEnField).setText("Test Bank EN");
        verify(nameArField).setText("بنك الاختبار");
        verify(activeCheckBox).setSelected(false);
    }

    @Test
    void handleSaveButtonAction_validInput_addMode_savesAndCloses() throws BankNameException, ValidationException {
        when(nameEnField.getText()).thenReturn("New Bank EN");
        when(nameArField.getText()).thenReturn("بنك جديد");
        when(activeCheckBox.isSelected()).thenReturn(true);

        controller.handleSaveButtonAction(null);

        assertTrue(controller.isSaved());
        ArgumentCaptor<BankNameDTO> captor = ArgumentCaptor.forClass(BankNameDTO.class);
        verify(mockBankNameService).saveBankName(captor.capture());
        assertEquals("New Bank EN", captor.getValue().getBankNameEn());
        assertEquals("بنك جديد", captor.getValue().getBankNameAr());
        assertTrue(captor.getValue().isActive());
        verify(mockDialogStage).close();
    }

    @Test
    void handleSaveButtonAction_validInput_editMode_savesAndCloses() throws BankNameException, ValidationException {
        BankNameDTO existingBank = new BankNameDTO("Old EN", "قديم عربي", true);
        existingBank.setBankNameId(1);
        controller.setEditableBankName(existingBank);

        when(nameEnField.getText()).thenReturn("Updated Bank EN");
        when(nameArField.getText()).thenReturn("بنك محدث");
        when(activeCheckBox.isSelected()).thenReturn(false);

        controller.handleSaveButtonAction(null);

        assertTrue(controller.isSaved());
        ArgumentCaptor<BankNameDTO> captor = ArgumentCaptor.forClass(BankNameDTO.class);
        verify(mockBankNameService).saveBankName(captor.capture());
        assertEquals(1, captor.getValue().getBankNameId());
        assertEquals("Updated Bank EN", captor.getValue().getBankNameEn());
        assertFalse(captor.getValue().isActive());
        verify(mockDialogStage).close();
    }

    @Test
    void handleSaveButtonAction_emptyNameEn_showsValidationError() {
        when(nameEnField.getText()).thenReturn("");
        when(nameArField.getText()).thenReturn("بنك");
        when(activeCheckBox.isSelected()).thenReturn(true);
         // For showErrorAlert to get owner stage
        when(mockDialogStage.getOwner()).thenReturn(null);
        when(nameEnField.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(nameEnField.getScene().getWindow()).thenReturn(mockDialogStage);


        controller.handleSaveButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void handleSaveButtonAction_serviceThrowsValidationException_showsError() throws BankNameException, ValidationException {
        when(nameEnField.getText()).thenReturn("Valid EN");
        when(nameArField.getText()).thenReturn("صالح عربي");
        when(activeCheckBox.isSelected()).thenReturn(true);
        doThrow(new ValidationException("Service validation failed", Collections.singletonList("some.service.error")))
            .when(mockBankNameService).saveBankName(any(BankNameDTO.class));
        when(mockDialogStage.getOwner()).thenReturn(null);
        when(nameEnField.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(nameEnField.getScene().getWindow()).thenReturn(mockDialogStage);

        controller.handleSaveButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void handleSaveButtonAction_serviceThrowsBankNameAlreadyExistsException_showsError() throws BankNameException, ValidationException {
        when(nameEnField.getText()).thenReturn("Existing EN");
        when(nameArField.getText()).thenReturn("موجود عربي");
        when(activeCheckBox.isSelected()).thenReturn(true);
        doThrow(new BankNameAlreadyExistsException("English name exists"))
            .when(mockBankNameService).saveBankName(any(BankNameDTO.class));
        when(mockDialogStage.getOwner()).thenReturn(null);
        when(nameEnField.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(nameEnField.getScene().getWindow()).thenReturn(mockDialogStage);

        controller.handleSaveButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }


    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initialize(null, resourceBundle); // Re-initialize for Arabic

        verify(bankNameFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
