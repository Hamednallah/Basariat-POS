package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.model.ExpenseDTO;
import com.basariatpos.service.BankNameService;
import com.basariatpos.service.ExpenseCategoryService;
import com.basariatpos.service.ExpenseService;
import com.basariatpos.service.exception.NoActiveShiftException;
import com.basariatpos.service.exception.ExpenseValidationException;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class ExpenseFormDialogControllerTest {

    @Mock private ExpenseService mockExpenseService;
    @Mock private ExpenseCategoryService mockExpenseCategoryService;
    @Mock private BankNameService mockBankNameService;

    private ExpenseFormDialogController controller;
    private Stage stage;
    private DialogPane dialogPane; // The root of ExpenseFormDialog.fxml

    private ExpenseCategoryDTO cat1;
    private BankNameDTO bank1;

    @BeforeAll
    static void setUpClass() throws Exception {
        try { Platform.startup(() -> {}); } catch (Exception e) { /* ignore */ }
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        MessageProvider.loadBundle(LocaleManager.getCurrentLocale());
        if (System.getProperty("os.name", "").toLowerCase().startsWith("linux")) {
            System.setProperty("java.awt.headless", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (stage != null && stage.isShowing()) {
             org.testfx.api.FxToolkit.cleanupStages();
        }
    }

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        cat1 = new ExpenseCategoryDTO(1, "Category A EN", "Category A AR", "", true);
        bank1 = new BankNameDTO(1, "Bank X EN", "Bank X AR", true);

        when(mockExpenseCategoryService.getActiveExpenseCategories()).thenReturn(List.of(cat1));
        when(mockBankNameService.getActiveBankNames()).thenReturn(List.of(bank1));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ExpenseFormDialog.fxml"));
        loader.setResources(MessageProvider.getBundle());
        dialogPane = loader.load();
        controller = loader.getController();

        Platform.runLater(() -> controller.initializeDialog(null, mockExpenseService, mockExpenseCategoryService, mockBankNameService, stage));
        WaitForAsyncUtils.waitForFxEvents();

        Scene scene = new Scene(dialogPane);
        stage.setScene(scene);
        stage.setTitle(MessageProvider.getString("expenseform.dialog.title.add"));
        stage.show();
    }

    @Test
    void initializeDialog_populatesCombosAndDefaults(FxRobot robot) {
        assertEquals(LocalDate.now(), robot.lookup("#dateField").queryAs(DatePicker.class).getValue());
        assertEquals(1, robot.lookup("#categoryCombo").queryAs(ComboBox.class).getItems().size());
        assertEquals(1, robot.lookup("#bankNameCombo").queryAs(ComboBox.class).getItems().size());
        assertFalse(robot.lookup("#bankNamePromptLabel").queryAs(Label.class).isVisible());
    }

    @Test
    void paymentMethodSelection_togglesBankFields(FxRobot robot) {
        ComboBox<String> paymentMethodCombo = robot.lookup("#paymentMethodCombo").queryComboBox();

        Platform.runLater(() -> paymentMethodCombo.setValue("Card"));
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(robot.lookup("#bankNamePromptLabel").queryAs(Label.class).isVisible());
        assertTrue(robot.lookup("#bankNameCombo").queryComboBox().isVisible());
        assertTrue(robot.lookup("#transactionIdPromptLabel").queryAs(Label.class).isVisible());
        assertTrue(robot.lookup("#transactionIdField").queryAs(TextField.class).isVisible());

        Platform.runLater(() -> paymentMethodCombo.setValue("Cash"));
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(robot.lookup("#bankNamePromptLabel").queryAs(Label.class).isVisible());
    }

    @Test
    void handleSaveExpense_validCashExpense_callsServiceAndCloses(FxRobot robot) throws Exception {
        ExpenseDTO savedDto = new ExpenseDTO(); savedDto.setExpenseId(101);
        ArgumentCaptor<ExpenseDTO> dtoCaptor = ArgumentCaptor.forClass(ExpenseDTO.class);
        when(mockExpenseService.recordExpense(dtoCaptor.capture())).thenReturn(savedDto);

        robot.interact(() -> controller.dateField.setValue(LocalDate.now().minusDays(1)));
        robot.interact(() -> controller.categoryCombo.setValue(cat1));
        robot.interact(() -> controller.descriptionField.setText("Valid Cash Expense"));
        robot.interact(() -> controller.amountField.setText("75.50"));
        robot.interact(() -> controller.paymentMethodCombo.setValue("Cash"));
        WaitForAsyncUtils.waitForFxEvents();

        // Simulate clicking OK button (ButtonType.OK is used in FXML)
        Button okButtonNode = (Button) dialogPane.lookupButton(ButtonType.OK); // DialogPane lookup
        robot.clickOn(okButtonNode);
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockExpenseService).recordExpense(any(ExpenseDTO.class));
        ExpenseDTO capturedDto = dtoCaptor.getValue();
        assertEquals("Valid Cash Expense", capturedDto.getDescription());
        assertEquals(0, new BigDecimal("75.50").compareTo(capturedDto.getAmount()));
        assertEquals("Cash", capturedDto.getPaymentMethod());
        assertTrue(controller.isSaved());
        assertFalse(stage.isShowing(), "Dialog should close on successful save.");
    }

    @Test
    void handleSaveExpense_validBankExpense_callsService(FxRobot robot) throws Exception {
        ExpenseDTO savedDto = new ExpenseDTO(); savedDto.setExpenseId(102);
        ArgumentCaptor<ExpenseDTO> dtoCaptor = ArgumentCaptor.forClass(ExpenseDTO.class);
        when(mockExpenseService.recordExpense(dtoCaptor.capture())).thenReturn(savedDto);

        robot.interact(() -> controller.dateField.setValue(LocalDate.now()));
        robot.interact(() -> controller.categoryCombo.setValue(cat1));
        robot.interact(() -> controller.descriptionField.setText("Valid Bank Expense"));
        robot.interact(() -> controller.amountField.setText("120.00"));
        robot.interact(() -> controller.paymentMethodCombo.setValue("Bank Transaction"));
        robot.interact(() -> controller.bankNameCombo.setValue(bank1));
        robot.interact(() -> controller.transactionIdField.setText("TXN789"));
        WaitForAsyncUtils.waitForFxEvents();

        Button okButtonNode = (Button) dialogPane.lookupButton(ButtonType.OK);
        robot.clickOn(okButtonNode);
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockExpenseService).recordExpense(any(ExpenseDTO.class));
        ExpenseDTO capturedDto = dtoCaptor.getValue();
        assertEquals("Bank Transaction", capturedDto.getPaymentMethod());
        assertEquals(bank1.getBankNameId(), capturedDto.getBankNameId());
        assertEquals("TXN789", capturedDto.getTransactionIdRef());
        assertTrue(controller.isSaved());
    }

    @Test
    void handleSaveExpense_missingAmount_showsValidationErrorAndStaysOpen(FxRobot robot) {
        robot.interact(() -> controller.categoryCombo.setValue(cat1)); // Other fields valid
        robot.interact(() -> controller.descriptionField.setText("Expense with no amount"));
        robot.interact(() -> controller.paymentMethodCombo.setValue("Cash"));
        robot.interact(() -> controller.amountField.setText("")); // Missing amount
        WaitForAsyncUtils.waitForFxEvents();

        try (var alertMock = mockConstruction(Alert.class)) {
            Button okButtonNode = (Button) dialogPane.lookupButton(ButtonType.OK);
            robot.clickOn(okButtonNode);
            WaitForAsyncUtils.waitForFxEvents();

            assertTrue(alertMock.constructed().size() >= 1, "Validation alert should be shown.");
            Alert alert = alertMock.constructed().get(0);
            assertEquals(Alert.AlertType.ERROR, alert.getAlertType());
            assertTrue(alert.getContentText().contains(MessageProvider.getString("expenseform.validation.amountPositive")));
        }
        verify(mockExpenseService, never()).recordExpense(any());
        assertFalse(controller.isSaved());
        assertTrue(stage.isShowing(), "Dialog should remain open on validation error.");
    }

    @Test
    void handleSaveExpense_cashExpenseNoShift_serviceThrowsException_showsErrorAlert(FxRobot robot) throws Exception {
        when(mockExpenseService.recordExpense(any(ExpenseDTO.class)))
            .thenThrow(new NoActiveShiftException("No active shift for cash expense."));

        robot.interact(() -> controller.categoryCombo.setValue(cat1));
        robot.interact(() -> controller.descriptionField.setText("Cash Expense No Shift"));
        robot.interact(() -> controller.amountField.setText("30.00"));
        robot.interact(() -> controller.paymentMethodCombo.setValue("Cash"));
        WaitForAsyncUtils.waitForFxEvents();

        try (var alertMock = mockConstruction(Alert.class)) {
            Button okButtonNode = (Button) dialogPane.lookupButton(ButtonType.OK);
            robot.clickOn(okButtonNode);
            WaitForAsyncUtils.waitForFxEvents();

            assertTrue(alertMock.constructed().size() >= 1);
            Alert alert = alertMock.constructed().get(0);
            assertEquals(Alert.AlertType.ERROR, alert.getAlertType());
            assertTrue(alert.getContentText().contains("No active shift for cash expense."));
        }
        assertFalse(controller.isSaved());
        assertTrue(stage.isShowing());
    }
}
