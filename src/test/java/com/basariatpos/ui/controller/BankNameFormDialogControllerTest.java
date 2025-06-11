package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.service.BankNameAlreadyExistsException;
import com.basariatpos.service.BankNameService;
import com.basariatpos.service.ValidationException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class BankNameFormDialogControllerTest {

    @Mock
    private BankNameService mockBankNameService;

    private BankNameFormDialogController controller;
    private Stage stage;
    private Parent root;

    private final String NAME_EN_FIELD = "#nameEnField";
    private final String NAME_AR_FIELD = "#nameArField";
    private final String ACTIVE_CHECKBOX = "#activeCheckBox";
    private final String SAVE_BUTTON = "#saveButton";
    private final String DIALOG_TITLE_LABEL = "#dialogTitleLabel";


    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/BankNameFormDialog.fxml"));
        loader.setResources(bundle);

        root = loader.load();
        controller = loader.getController();

        // Manual injection of mocks and stage
        controller.setBankNameService(mockBankNameService);
        controller.setDialogStage(stage); // Pass the stage to the controller

        Scene scene = new Scene(root);
        stage.setScene(scene);
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
    void addMode_initializesFieldsCorrectly(FxRobot robot) {
        // Controller's initialize sets default title, this test verifies it if dialog is for add
        // Default title in FXML is "Bank Name Form", controller should update it.
        // Let's assume controller sets title to "Add Bank Name" if editableBankName is null
        // This requires controller to have logic for setting title in initialize or a dedicated method.
        // For now, the test will reflect the current FXML default or what controller sets.
        // The current BankNameFormDialogController doesn't set title in initialize, relies on caller.

        assertTrue(robot.lookup(ACTIVE_CHECKBOX).queryAs(CheckBox.class).isSelected(), "Active checkbox should be selected by default.");
        assertEquals("", robot.lookup(NAME_EN_FIELD).queryAs(TextField.class).getText());
    }

    @Test
    void editMode_populatesFieldsCorrectly(FxRobot robot) {
        BankNameDTO bankToEdit = new BankNameDTO(1, "Existing Bank EN", "بنك قائم عربي", false);

        robot.interact(() -> controller.setEditableBankName(bankToEdit));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Existing Bank EN", robot.lookup(NAME_EN_FIELD).queryAs(TextField.class).getText());
        assertEquals("بنك قائم عربي", robot.lookup(NAME_AR_FIELD).queryAs(TextField.class).getText());
        assertFalse(robot.lookup(ACTIVE_CHECKBOX).queryAs(CheckBox.class).isSelected());
        assertEquals(MessageProvider.getString("bankname.dialog.edit.title"), robot.lookup(DIALOG_TITLE_LABEL).queryAs(Label.class).getText());
    }

    @Test
    void saveButton_addMode_validInput_callsServiceAndCloses(FxRobot robot) throws Exception {
        BankNameDTO savedBank = new BankNameDTO(1, "New Bank EN", "بنك جديد عربي", true);
        when(mockBankNameService.saveBankName(any(BankNameDTO.class))).thenReturn(savedBank);

        robot.clickOn(NAME_EN_FIELD).write("New Bank EN");
        robot.clickOn(NAME_AR_FIELD).write("بنك جديد عربي");
        // Active is true by default

        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        ArgumentCaptor<BankNameDTO> captor = ArgumentCaptor.forClass(BankNameDTO.class);
        verify(mockBankNameService).saveBankName(captor.capture());
        assertEquals("New Bank EN", captor.getValue().getBankNameEn());
        assertTrue(captor.getValue().isActive());

        assertTrue(controller.isSaved(), "Dialog should be marked as saved.");
        assertFalse(stage.isShowing(), "Dialog should close on successful save.");
    }

    @Test
    void saveButton_editMode_validInput_callsServiceAndCloses(FxRobot robot) throws Exception {
        BankNameDTO existingBank = new BankNameDTO(1, "Old EN", "قديم عربي", true);
        robot.interact(() -> controller.setEditableBankName(existingBank));
        WaitForAsyncUtils.waitForFxEvents();

        BankNameDTO updatedBank = new BankNameDTO(1, "Updated EN", "محدث عربي", false);
        when(mockBankNameService.saveBankName(any(BankNameDTO.class))).thenReturn(updatedBank);

        robot.clickOn(NAME_EN_FIELD).eraseText(existingBank.getBankNameEn().length()).write("Updated EN");
        robot.clickOn(NAME_AR_FIELD).eraseText(existingBank.getBankNameAr().length()).write("محدث عربي");
        robot.clickOn(ACTIVE_CHECKBOX); // Toggle to false

        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        ArgumentCaptor<BankNameDTO> captor = ArgumentCaptor.forClass(BankNameDTO.class);
        verify(mockBankNameService).saveBankName(captor.capture());
        assertEquals(1, captor.getValue().getBankNameId());
        assertEquals("Updated EN", captor.getValue().getBankNameEn());
        assertFalse(captor.getValue().isActive());

        assertTrue(controller.isSaved());
        assertFalse(stage.isShowing());
    }


    @Test
    void saveButton_emptyEnglishName_showsValidationError(FxRobot robot) {
        robot.clickOn(NAME_AR_FIELD).write("اسم عربي");
        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify alert is shown
        assertNotNull(robot.lookup(".alert.error").tryQuery().orElse(null), "Error alert should be shown.");
        assertFalse(controller.isSaved());
        assertTrue(stage.isShowing()); // Dialog should remain open

        // Close alert
        robot.targetWindow(robot.listWindows().stream().filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL).findFirst().get()).close();

    }

    @Test
    void saveButton_serviceThrowsBankNameAlreadyExistsException_showsErrorAlert(FxRobot robot) throws Exception {
        when(mockBankNameService.saveBankName(any(BankNameDTO.class)))
            .thenThrow(new BankNameAlreadyExistsException("Test Bank EN"));

        robot.clickOn(NAME_EN_FIELD).write("Test Bank EN");
        robot.clickOn(NAME_AR_FIELD).write("بنك اختباري");
        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(robot.lookup(".alert.error").tryQuery().orElse(null), "Error alert for existing name should be shown.");
        assertFalse(controller.isSaved());
        assertTrue(stage.isShowing());

        robot.targetWindow(robot.listWindows().stream().filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL).findFirst().get()).close();
    }


    @Test
    void cancelButton_closesDialog_andIsSavedReturnsFalse(FxRobot robot) {
        robot.clickOn("#cancelButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(controller.isSaved(), "Dialog should not be marked as saved on cancel.");
        assertFalse(stage.isShowing(), "Dialog should close on cancel.");
    }
}
