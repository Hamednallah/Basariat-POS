package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;

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

import java.math.BigDecimal;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StartShiftDialogControllerTest {

    @Mock private TextField openingFloatFld;
    @Mock private VBox startShiftDialogRootPane;
    @Mock private Stage mockDialogStage;

    @InjectMocks
    private StartShiftDialogController controller;

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
        controller.openingFloatFld = openingFloatFld;
        controller.startShiftDialogRootPane = startShiftDialogRootPane;

        controller.initialize();
        controller.setDialogStage(mockDialogStage);
    }

    @Test
    void initialize_setsUpOrientationAndListener() {
        verify(startShiftDialogRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
        verify(openingFloatFld).textProperty();
    }

    @Test
    void handleStartButtonAction_validInput_savesAndCloses() {
        when(openingFloatFld.getText()).thenReturn("100.50");

        controller.handleStartButtonAction(null);

        assertTrue(controller.isSaved());
        assertEquals(new BigDecimal("100.50"), controller.getOpeningFloat());
        verify(mockDialogStage).close();
    }

    @Test
    void handleStartButtonAction_validInputWithComma_savesAndCloses() {
        when(openingFloatFld.getText()).thenReturn("100,50");

        controller.handleStartButtonAction(null);

        assertTrue(controller.isSaved());
        assertEquals(new BigDecimal("100.50"), controller.getOpeningFloat());
        verify(mockDialogStage).close();
    }


    @Test
    void handleStartButtonAction_emptyInput_showsError() {
        when(openingFloatFld.getText()).thenReturn("");
        when(openingFloatFld.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(openingFloatFld.getScene().getWindow()).thenReturn(mockDialogStage);

        controller.handleStartButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void handleStartButtonAction_invalidNumber_showsError() {
        when(openingFloatFld.getText()).thenReturn("abc");
        when(openingFloatFld.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(openingFloatFld.getScene().getWindow()).thenReturn(mockDialogStage);

        controller.handleStartButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void handleStartButtonAction_negativeNumber_showsError() {
        when(openingFloatFld.getText()).thenReturn("-10");
        when(openingFloatFld.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(openingFloatFld.getScene().getWindow()).thenReturn(mockDialogStage);

        controller.handleStartButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);

        controller.initialize();

        verify(startShiftDialogRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH);
    }
}
