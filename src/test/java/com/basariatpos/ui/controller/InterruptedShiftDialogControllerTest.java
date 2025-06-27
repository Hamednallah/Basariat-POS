package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ShiftDTO;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterruptedShiftDialogControllerTest {

    @Mock private Label messageLabel;
    @Mock private VBox interruptedShiftDialogRootPane; // For RTL
    @Mock private Stage mockDialogStage;

    @InjectMocks
    private InterruptedShiftDialogController controller;

    private static ResourceBundle resourceBundle;
    private ShiftDTO testShift;

    @BeforeAll
    static void setUpClass() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Default to English for tests
        resourceBundle = MessageProvider.getBundle();
         try {
            new javafx.embed.swing.JFXPanel();
        } catch (Exception e) { /* Ignore */ }
    }

    @BeforeEach
    void setUp() {
        // Manual FXML injection
        controller.messageLabel = messageLabel;
        controller.interruptedShiftDialogRootPane = interruptedShiftDialogRootPane;

        testShift = new ShiftDTO();
        testShift.setShiftId(101L);
        testShift.setStartedByUsername("testcashier");
        testShift.setStartTime(LocalDateTime.now().minusHours(2));

        // initializeDialog is the entry point, also calls updateNodeOrientation
        // controller.initializeDialog(testShift, mockDialogStage);
    }

    @Test
    void initializeDialog_setsMessageAndOrientation() {
        controller.initializeDialog(testShift, mockDialogStage);

        verify(messageLabel).setText(anyString()); // Check that message is set
        // Verify that the message contains key info (more specific check if needed)
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageLabel).setText(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("101"));
        assertTrue(messageCaptor.getValue().contains("testcashier"));

        verify(interruptedShiftDialogRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void handleResumeButtonAction_setsResultAndCloses() {
        controller.initializeDialog(testShift, mockDialogStage); // Ensure stage is set
        controller.handleResumeButtonAction(null);
        assertEquals(InterruptedShiftDialogController.InterruptedShiftAction.RESUME, controller.getResult());
        verify(mockDialogStage).close();
    }

    @Test
    void handleForciblyEndButtonAction_setsResultAndCloses() {
        controller.initializeDialog(testShift, mockDialogStage);
        controller.handleForciblyEndButtonAction(null);
        assertEquals(InterruptedShiftDialogController.InterruptedShiftAction.FORCIBLY_END, controller.getResult());
        verify(mockDialogStage).close();
    }

    @Test
    void handleLogoutButtonAction_setsResultAndCloses() {
        controller.initializeDialog(testShift, mockDialogStage);
        controller.handleLogoutButtonAction(null);
        assertEquals(InterruptedShiftDialogController.InterruptedShiftAction.CANCEL, controller.getResult());
        verify(mockDialogStage).close();
    }

    @Test
    void initializeDialog_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle(); // Reload for Arabic text

        controller.initializeDialog(testShift, mockDialogStage);

        verify(interruptedShiftDialogRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
        // Check if label alignment is still center (as per controller logic)
        verify(messageLabel, atLeastOnce()).setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
