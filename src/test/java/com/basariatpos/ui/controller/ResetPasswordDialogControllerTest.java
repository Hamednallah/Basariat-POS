package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;

import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResetPasswordDialogControllerTest {

    @Mock private PasswordField newPasswordField;
    @Mock private PasswordField confirmNewPasswordField;
    @Mock private VBox resetPasswordRootPane; // For RTL
    @Mock private Stage mockDialogStage;

    @InjectMocks
    private ResetPasswordDialogController controller;

    private static ResourceBundle resourceBundle;

    @BeforeAll
    static void setUpClass() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        resourceBundle = MessageProvider.getBundle(); // Though not directly used by this controller's init
         try {
            new javafx.embed.swing.JFXPanel();
        } catch (Exception e) { /* Ignore */ }
    }

    @BeforeEach
    void setUp() {
        // Manual injection of @FXML mocks
        controller.newPasswordField = newPasswordField;
        controller.confirmNewPasswordField = confirmNewPasswordField;
        controller.resetPasswordRootPane = resetPasswordRootPane;

        controller.setDialogStage(mockDialogStage); // Set the mocked stage, which also calls updateNodeOrientation
    }

    @Test
    void setDialogStage_updatesNodeOrientation_LTR() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Ensure LTR
        controller.setDialogStage(mockDialogStage); // Call again to trigger updateNodeOrientation with correct locale
        verify(resetPasswordRootPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void setDialogStage_updatesNodeOrientation_RTL() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC); // Ensure RTL
        controller.setDialogStage(mockDialogStage); // Call again to trigger updateNodeOrientation
        verify(resetPasswordRootPane).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }


    @Test
    void handleSavePasswordButtonAction_validPasswords_savesAndCloses() {
        when(newPasswordField.getText()).thenReturn("newValidPass123");
        when(confirmNewPasswordField.getText()).thenReturn("newValidPass123");

        controller.handleSavePasswordButtonAction(null);

        assertTrue(controller.isPasswordSaved());
        assertEquals("newValidPass123", controller.getNewPassword());
        verify(mockDialogStage).close();
    }

    @Test
    void handleSavePasswordButtonAction_emptyPassword_showsError() {
        when(newPasswordField.getText()).thenReturn("");
        when(confirmNewPasswordField.getText()).thenReturn("");

        controller.handleSavePasswordButtonAction(null);

        assertFalse(controller.isPasswordSaved());
        verify(mockDialogStage, never()).close();
        // Verification of showValidationErrorAlert is tricky without TestFX
    }

    @Test
    void handleSavePasswordButtonAction_mismatchedPasswords_showsError() {
        when(newPasswordField.getText()).thenReturn("newValidPass123");
        when(confirmNewPasswordField.getText()).thenReturn("mismatchedPass");

        controller.handleSavePasswordButtonAction(null);

        assertFalse(controller.isPasswordSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void handleSavePasswordButtonAction_passwordTooShort_showsError() {
        when(newPasswordField.getText()).thenReturn("short");
        when(confirmNewPasswordField.getText()).thenReturn("short");

        controller.handleSavePasswordButtonAction(null);

        assertFalse(controller.isPasswordSaved());
        verify(mockDialogStage, never()).close();
    }


    @Test
    void handleCancelPasswordButtonAction_closesDialog() {
        controller.handleCancelPasswordButtonAction(null);
        assertFalse(controller.isPasswordSaved());
        verify(mockDialogStage).close();
    }
}
