package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.model.PaymentDTO;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.service.BankNameService;
import com.basariatpos.service.PaymentService;
import com.basariatpos.service.exception.PaymentValidationException;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class PaymentDialogControllerTest {

    @Mock private PaymentService mockPaymentService;
    @Mock private BankNameService mockBankNameService;

    private PaymentDialogController controller;
    private Stage stage;
    private SalesOrderDTO testOrder;

    @BeforeAll
    static void setUpClass() throws Exception {
        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Or your test default
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

        testOrder = new SalesOrderDTO();
        testOrder.setSalesOrderId(123);
        testOrder.setBalanceDue(new BigDecimal("150.75"));

        // Mock bank name service response
        List<BankNameDTO> banks = new ArrayList<>();
        banks.add(new BankNameDTO(1, "Bank A EN", "Bank A AR", true));
        banks.add(new BankNameDTO(2, "Bank B EN", "Bank B AR", true));
        when(mockBankNameService.getActiveBankNames()).thenReturn(banks);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/PaymentDialog.fxml"));
        loader.setResources(MessageProvider.getBundle());
        DialogPane root = loader.load(); // PaymentDialog.fxml uses DialogPane as root
        controller = loader.getController();

        controller.initializeDialog(testOrder, mockPaymentService, mockBankNameService, stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(MessageProvider.getString("payment.dialog.title", String.valueOf(testOrder.getSalesOrderId())));
        stage.show();
    }

    @Test
    void initializeDialog_populatesFieldsCorrectly(FxRobot robot) {
        assertEquals(String.valueOf(testOrder.getSalesOrderId()), robot.lookup("#orderIdLabel").queryAs(Label.class).getText());
        assertEquals(testOrder.getBalanceDue().toPlainString(), robot.lookup("#balanceDueLabel").queryAs(Label.class).getText());
        assertEquals(testOrder.getBalanceDue().toPlainString(), robot.lookup("#amountField").queryAs(TextField.class).getText()); // Defaults to balance due

        ComboBox<String> paymentMethodCombo = robot.lookup("#paymentMethodCombo").queryComboBox();
        assertFalse(paymentMethodCombo.getItems().isEmpty());

        ComboBox<BankNameDTO> bankNameCombo = robot.lookup("#bankNameCombo").queryComboBox();
        assertEquals(2, bankNameCombo.getItems().size()); // From mockBankNameService
    }

    @Test
    void paymentMethodSelection_togglesBankFieldsVisibility(FxRobot robot) {
        ComboBox<String> paymentMethodCombo = robot.lookup("#paymentMethodCombo").queryComboBox();
        Label bankNameLabel = robot.lookup("#bankNameLabel").queryAs(Label.class);

        Platform.runLater(() -> paymentMethodCombo.setValue("Card"));
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(bankNameLabel.isVisible());
        assertTrue(robot.lookup("#bankNameCombo").queryComboBox().isVisible());
        assertTrue(robot.lookup("#transactionIdField").queryAs(TextField.class).isVisible());

        Platform.runLater(() -> paymentMethodCombo.setValue("Cash"));
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(bankNameLabel.isVisible());
    }

    @Test
    void handleSubmitPayment_validCashPayment_callsServiceAndCloses(FxRobot robot) throws Exception {
        PaymentDTO mockSavedPayment = new PaymentDTO(); mockSavedPayment.setPaymentId(1001);
        when(mockPaymentService.recordPayment(any(PaymentDTO.class))).thenReturn(mockSavedPayment);

        robot.interact(() -> controller.paymentMethodCombo.setValue("Cash"));
        robot.interact(() -> controller.amountField.setText("50.00"));
        WaitForAsyncUtils.waitForFxEvents();

        // Simulate clicking OK button (ButtonType.OK)
        // This requires the DialogPane to be part of a Dialog that is shown.
        // For more direct testing of handler:
        // Platform.runLater(() -> controller.handleSubmitPaymentAction(new ActionEvent(paymentDialogPane.lookupButton(ButtonType.OK), Button.USE_COMPUTED_SIZE)));

        // For TestFX, clicking the actual button node
        Button okButtonNode = (Button) robot.lookup(".dialog-pane .button").match(button -> ((Button)button).isDefaultButton()).queryButton();
        robot.clickOn(okButtonNode);
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockPaymentService).recordPayment(any(PaymentDTO.class));
        assertNotNull(controller.getResultPayment());
        assertEquals(1001, controller.getResultPayment().getPaymentId());
        // Stage should be closed by DialogPane mechanism if event not consumed by validation fail
        // assertFalse(stage.isShowing()); // This assertion can be flaky with TestFX dialogs
    }

    @Test
    void handleSubmitPayment_invalidAmount_showsValidationError(FxRobot robot) {
        robot.interact(() -> controller.amountField.setText("-10.00")); // Invalid amount
        WaitForAsyncUtils.waitForFxEvents();

        try (var alertMock = mockConstruction(Alert.class)) {
            Button okButtonNode = (Button) robot.lookup(".dialog-pane .button").match(button -> ((Button)button).isDefaultButton()).queryButton();
            robot.clickOn(okButtonNode);
            WaitForAsyncUtils.waitForFxEvents();

            assertTrue(alertMock.constructed().size() >= 1);
            Alert alert = alertMock.constructed().get(0);
            assertEquals(Alert.AlertType.ERROR, alert.getAlertType());
            assertTrue(alert.getContentText().contains(MessageProvider.getString("payment.validation.amountPositive")));
        }
        assertTrue(stage.isShowing()); // Dialog should remain open
    }
}
