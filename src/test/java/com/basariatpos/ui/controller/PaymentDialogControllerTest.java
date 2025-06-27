package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.model.PaymentDTO;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.service.BankNameService;
import com.basariatpos.service.PaymentService;
import com.basariatpos.service.exception.PaymentException;
import com.basariatpos.ui.utilui.TextFormatters;


import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane; // DialogPane is not easily mockable for lookupButton without TestFX
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentDialogControllerTest {

    @Mock private DialogPane paymentDialogPane;
    @Mock private Label titleLabel, orderIdLabel, balanceDueLabel;
    @Mock private TextField amountField, transactionIdField;
    @Mock private ComboBox<String> paymentMethodCombo;
    @Mock private ComboBox<BankNameDTO> bankNameCombo;
    @Mock private Label bankNameLabel, transactionIdLabel; // For visibility toggling
    @Mock private Stage mockDialogStage;
    @Mock private PaymentService mockPaymentService;
    @Mock private BankNameService mockBankNameService;

    @Mock private Button mockOkButton; // Mock for lookupButton


    @InjectMocks
    private PaymentDialogController controller;

    private static ResourceBundle resourceBundle;
    private SalesOrderDTO testOrder;
    private MockedStatic<TextFormatters> mockTextFormatters;


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
        // Manual FXML injection
        controller.paymentDialogPane = paymentDialogPane;
        controller.titleLabel = titleLabel;
        controller.orderIdLabel = orderIdLabel;
        controller.balanceDueLabel = balanceDueLabel;
        controller.amountField = amountField;
        controller.paymentMethodCombo = paymentMethodCombo;
        controller.bankNameLabel = bankNameLabel;
        controller.bankNameCombo = bankNameCombo;
        controller.transactionIdLabel = transactionIdLabel;
        controller.transactionIdField = transactionIdField;

        testOrder = new SalesOrderDTO();
        testOrder.setSalesOrderId(1L);
        testOrder.setBalanceDue(new BigDecimal("100.00"));

        // Mock ComboBox items & listeners
        when(paymentMethodCombo.getItems()).thenReturn(FXCollections.observableArrayList("Cash", "Card"));
        SingleSelectionModel<String> paymentMethodSelectionModel = mock(SingleSelectionModel.class);
        when(paymentMethodCombo.getSelectionModel()).thenReturn(paymentMethodSelectionModel);
        when(paymentMethodSelectionModel.selectedItemProperty()).thenReturn(mock(javafx.beans.property.ReadOnlyObjectProperty.class));

        when(bankNameCombo.getItems()).thenReturn(FXCollections.observableArrayList(new BankNameDTO(1, "Bank A EN", "Bank A AR", true)));
        // Visibility mocks for bank fields
        when(bankNameLabel.visibleProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(bankNameLabel.managedProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(bankNameCombo.visibleProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(bankNameCombo.managedProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(transactionIdLabel.visibleProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(transactionIdLabel.managedProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(transactionIdField.visibleProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(transactionIdField.managedProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));


        // Mock TextFormatters
        mockTextFormatters = Mockito.mockStatic(TextFormatters.class);
        mockTextFormatters.when(() -> TextFormatters.applyBigDecimalFormatter(any(TextField.class))).thenAnswer(inv -> null);
        mockTextFormatters.when(() -> TextFormatters.parseBigDecimal(anyString(), any())).thenAnswer(
            inv -> { String arg = inv.getArgument(0); if(arg == null || arg.isEmpty()) return inv.getArgument(1); try { return new BigDecimal(arg); } catch (Exception e) { return inv.getArgument(1); } }
        );

        when(paymentDialogPane.lookupButton(ButtonType.OK)).thenReturn(mockOkButton);


        controller.initialize();
        controller.initializeDialog(testOrder, mockPaymentService, mockBankNameService, mockDialogStage);
    }

    @AfterEach
    void tearDown() {
        mockTextFormatters.close();
    }


    @Test
    void initializeDialog_populatesFieldsAndSetsOrientation() {
        verify(titleLabel).setText(MessageProvider.getString("payment.dialog.title", "1"));
        verify(orderIdLabel).setText("1");
        verify(balanceDueLabel).setText("100.00");
        verify(amountField).setText("100.00");
        verify(paymentDialogPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
        verify(mockOkButton).addEventFilter(eq(ActionEvent.ACTION), any());
    }

    @Test
    void handleSubmitPaymentAction_validCashPayment_recordsAndCloses() throws PaymentException {
        when(amountField.getText()).thenReturn("50.00");
        when(paymentMethodCombo.getValue()).thenReturn("Cash");

        PaymentDTO mockResult = new PaymentDTO(); mockResult.setPaymentId(123L);
        when(mockPaymentService.recordPayment(any(PaymentDTO.class))).thenReturn(mockResult);

        when(paymentDialogPane.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(paymentDialogPane.getScene().getWindow()).thenReturn(mockDialogStage);


        ActionEvent mockEvent = mock(ActionEvent.class);
        controller.handleSubmitPaymentAction(mockEvent);

        assertNotNull(controller.getResultPayment());
        assertEquals(123L, controller.getResultPayment().getPaymentId());
        verify(mockPaymentService).recordPayment(any(PaymentDTO.class));
        verify(mockEvent, never()).consume();
    }

    @Test
    void handleSubmitPaymentAction_invalidAmount_showsErrorAndConsumesEvent() {
        when(amountField.getText()).thenReturn("-10.00");
        when(paymentMethodCombo.getValue()).thenReturn("Cash");

        when(paymentDialogPane.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(paymentDialogPane.getScene().getWindow()).thenReturn(mockDialogStage);


        ActionEvent mockEvent = mock(ActionEvent.class);
        controller.handleSubmitPaymentAction(mockEvent);

        assertNull(controller.getResultPayment());
        verify(mockPaymentService, never()).recordPayment(any(PaymentDTO.class));
        verify(mockEvent).consume();
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initialize();
        controller.initializeDialog(testOrder, mockPaymentService, mockBankNameService, mockDialogStage);

        verify(paymentDialogPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH);
    }
}
