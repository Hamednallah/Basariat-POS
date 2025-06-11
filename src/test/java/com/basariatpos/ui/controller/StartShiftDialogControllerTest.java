package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class StartShiftDialogControllerTest {

    private StartShiftDialogController controller;
    private Stage stage;

    private final String OPENING_FLOAT_FLD = "#openingFloatFld";
    private final String START_BUTTON = "#startButton";
    private final String CANCEL_BUTTON = "#cancelButton";

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE); // Ensure consistent locale
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/StartShiftDialog.fxml"));
        loader.setResources(bundle);
        Parent root = loader.load();
        controller = loader.getController();
        controller.setDialogStage(stage); // Pass the stage to the controller

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(MessageProvider.getString("startshiftdialog.title"));
        stage.show();
        stage.toFront();
    }

    @AfterEach
    void tearDown(FxRobot robot) {
        // Close any alert dialogs that might be open
        robot.listWindows().stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL && w != stage)
            .forEach(w -> robot.targetWindow(w).close());
        WaitForAsyncUtils.waitForFxEvents();
    }


    @Test
    void validOpeningFloat_saveAndClose(FxRobot robot) {
        robot.clickOn(OPENING_FLOAT_FLD).write("150.75");
        robot.clickOn(START_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(controller.isSaved());
        assertEquals(new BigDecimal("150.75"), controller.getOpeningFloat());
        assertFalse(stage.isShowing(), "Dialog should close on successful save.");
    }

    @Test
    void validOpeningFloat_withComma_saveAndClose(FxRobot robot) {
        robot.clickOn(OPENING_FLOAT_FLD).write("150,75"); // Comma as decimal separator
        robot.clickOn(START_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(controller.isSaved());
        assertEquals(new BigDecimal("150.75"), controller.getOpeningFloat());
        assertFalse(stage.isShowing());
    }


    @Test
    void emptyOpeningFloat_showsError_dialogRemains(FxRobot robot) {
        robot.clickOn(START_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(robot.lookup(".alert.error").tryQuery().orElse(null), "Error alert should be shown.");
        assertFalse(controller.isSaved());
        assertTrue(stage.isShowing(), "Dialog should remain open after validation error.");

        robot.targetWindow(robot.listWindows().stream().filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL).findFirst().get()).close();
    }

    @Test
    void negativeOpeningFloat_showsError_dialogRemains(FxRobot robot) {
        robot.clickOn(OPENING_FLOAT_FLD).write("-100");
        robot.clickOn(START_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(robot.lookup(".alert.error").tryQuery().orElse(null), "Error alert for negative float should be shown.");
        assertFalse(controller.isSaved());
        assertTrue(stage.isShowing());
        robot.targetWindow(robot.listWindows().stream().filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL).findFirst().get()).close();
    }

    @Test
    void nonNumericOpeningFloat_showsError_dialogRemains(FxRobot robot) {
        robot.clickOn(OPENING_FLOAT_FLD).write("abc");
        robot.clickOn(START_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(robot.lookup(".alert.error").tryQuery().orElse(null), "Error alert for non-numeric float should be shown.");
        assertFalse(controller.isSaved());
        assertTrue(stage.isShowing());
        robot.targetWindow(robot.listWindows().stream().filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL).findFirst().get()).close();
    }

    @Test
    void cancelButton_closesDialog_isSavedFalse(FxRobot robot) {
        robot.clickOn(CANCEL_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(controller.isSaved());
        assertFalse(stage.isShowing(), "Dialog should close on cancel.");
    }
}
