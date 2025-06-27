package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.model.PaymentDTO;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.service.BankNameService;
import com.basariatpos.service.PaymentService;
import com.basariatpos.service.exception.NoActiveShiftException;
import com.basariatpos.service.exception.PaymentException;
import com.basariatpos.service.exception.PaymentValidationException;
import com.basariatpos.service.exception.SalesOrderNotFoundException;
import com.basariatpos.ui.utilui.AlertUtil;
import com.basariatpos.ui.utilui.TextFormatters;
import com.basariatpos.util.AppLogger;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaymentDialogController {

    private static final Logger logger = AppLogger.getLogger(PaymentDialogController.class);

    @FXML private DialogPane paymentDialogPane;
    @FXML private Label titleLabel;
    @FXML private Label orderIdLabel;
    @FXML private Label balanceDueLabel;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Label bankNameLabel;
    @FXML private ComboBox<BankNameDTO> bankNameCombo;
    @FXML private Label transactionIdLabel;
    @FXML private TextField transactionIdField;
    @FXML private TextArea notesArea;

    private Stage dialogStage;
    private SalesOrderDTO currentOrder;
    private PaymentService paymentService;
    private BankNameService bankNameService;
    private PaymentDTO resultPayment = null;

    // Common payment methods (could be externalized or configurable)
    private final List<String> PAYMENT_METHODS = Arrays.asList("Cash", "Card", "Bank Transfer", "Cheque", "Mobile Money");


    public void initialize() {
        TextFormatters.applyBigDecimalFormatter(amountField);
        paymentMethodCombo.setItems(FXCollections.observableArrayList(PAYMENT_METHODS));
        paymentMethodCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            toggleBankFields(newVal);
        });

        bankNameCombo.setConverter(new StringConverter<BankNameDTO>() {
            @Override public String toString(BankNameDTO bank) { return bank == null ? null : (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale()) ? bank.getNameAr() : bank.getNameEn()); }
            @Override public BankNameDTO fromString(String string) { return null; }
        });
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (paymentDialogPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                paymentDialogPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                paymentDialogPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("paymentDialogPane is null. Cannot set RTL/LTR orientation.");
        }
    }


    public void initializeDialog(SalesOrderDTO order, PaymentService paymentService,
                                 BankNameService bankNameService, Stage stage) {
        this.currentOrder = order;
        this.paymentService = paymentService;
        this.bankNameService = bankNameService;
        this.dialogStage = stage;

        updateNodeOrientation(); // Ensure orientation when dialog is fully set up

        titleLabel.setText(MessageProvider.getString("payment.dialog.title", String.valueOf(order.getSalesOrderId())));
        orderIdLabel.setText(String.valueOf(order.getSalesOrderId()));
        balanceDueLabel.setText(order.getBalanceDue().toPlainString());
        amountField.setText(order.getBalanceDue().toPlainString()); // Default payment to full balance

        loadBankNames();
        toggleBankFields(null); // Initial state for bank fields

        // Add event filter for OK button after stage is set
        final Button okButton = (Button) paymentDialogPane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, this::handleSubmitPaymentAction);
        // Consider adding one for Print & Pay if that button is added
    }

    private void loadBankNames() {
        try {
            List<BankNameDTO> banks = bankNameService.getActiveBankNames();
            bankNameCombo.setItems(FXCollections.observableArrayList(banks));
        } catch (Exception e) {
            logger.error("Failed to load bank names: {}", e.getMessage(), e);
            AlertUtil.showError("Load Error", "Could not load bank names for selection.");
        }
    }

    private void toggleBankFields(String paymentMethod) {
        boolean bankDetailsRequired = "Card".equalsIgnoreCase(paymentMethod) ||
                                      "Bank Transfer".equalsIgnoreCase(paymentMethod) ||
                                      "Cheque".equalsIgnoreCase(paymentMethod);
        bankNameLabel.setVisible(bankDetailsRequired); bankNameLabel.setManaged(bankDetailsRequired);
        bankNameCombo.setVisible(bankDetailsRequired); bankNameCombo.setManaged(bankDetailsRequired);
        transactionIdLabel.setVisible(bankDetailsRequired); transactionIdLabel.setManaged(bankDetailsRequired);
        transactionIdField.setVisible(bankDetailsRequired); transactionIdField.setManaged(bankDetailsRequired);
    }

    private void handleSubmitPaymentAction(ActionEvent event) {
        List<String> errors = new ArrayList<>();
        BigDecimal amount = TextFormatters.parseBigDecimal(amountField.getText(), null);
        String method = paymentMethodCombo.getValue();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(MessageProvider.getString("payment.validation.amountPositive"));
        } else if (currentOrder != null && amount.compareTo(currentOrder.getBalanceDue()) > 0) {
            errors.add(MessageProvider.getString("payment.validation.amountExceedsBalance"));
        }
        if (method == null || method.trim().isEmpty()) {
            errors.add(MessageProvider.getString("payment.validation.methodRequired"));
        }

        BankNameDTO selectedBank = null;
        if (bankNameCombo.isVisible()) { // Bank details are visible, hence potentially required
            selectedBank = bankNameCombo.getValue();
            if (selectedBank == null) {
                errors.add(MessageProvider.getString("payment.validation.bankNameRequired"));
            }
            if (transactionIdField.getText() == null || transactionIdField.getText().trim().isEmpty()) {
                 errors.add(MessageProvider.getString("payment.validation.transactionIdRequired"));
            }
        }

        if (!errors.isEmpty()) {
            AlertUtil.showValidationError(errors);
            event.consume(); // Prevent dialog closing
            return;
        }

        PaymentDTO payment = new PaymentDTO();
        payment.setSalesOrderId(currentOrder.getSalesOrderId());
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setPaymentDate(OffsetDateTime.now()); // Set at time of recording
        if (selectedBank != null) {
            payment.setBankNameId(selectedBank.getBankNameId());
        }
        payment.setTransactionId(transactionIdField.getText());
        payment.setNotes(notesArea.getText());
        // receivedByUserId and shiftId will be set by the service

        try {
            resultPayment = paymentService.recordPayment(payment);
            AlertUtil.showSuccess("Payment Recorded", MessageProvider.getString("payment.success", String.valueOf(resultPayment.getPaymentId())));
            // The dialog will close automatically as event is not consumed.
            // Print receipt option
            // handlePrintReceiptPlaceholder(resultPayment);
        } catch (PaymentValidationException | SalesOrderNotFoundException | NoActiveShiftException | PaymentException e) {
            logger.error("Failed to record payment: {}", e.getMessage(), e);
            AlertUtil.showError("Payment Error", MessageProvider.getString("payment.error.saveFailed") + "\n" + e.getMessage());
            event.consume(); // Prevent dialog closing on error
        }
    }

    public PaymentDTO getResultPayment() {
        return resultPayment;
    }

    // Placeholder for receipt printing logic
    private void handlePrintReceiptPlaceholder(PaymentDTO payment) {
        if (payment == null) return;
        StringBuilder receiptText = new StringBuilder();
        receiptText.append(MessageProvider.getString("payment.receipt.header")).append("\n\n");
        receiptText.append(MessageProvider.getString("payment.receipt.orderId", String.valueOf(payment.getSalesOrderId()))).append("\n");
        receiptText.append(MessageProvider.getString("payment.receipt.paymentId", String.valueOf(payment.getPaymentId()))).append("\n");
        receiptText.append(MessageProvider.getString("payment.receipt.date", DateTimeUtil.formatUserFriendlyDateTime(payment.getPaymentDate()))).append("\n");
        receiptText.append(MessageProvider.getString("payment.receipt.amountPaid", payment.getAmount().toPlainString())).append("\n");
        receiptText.append(MessageProvider.getString("payment.receipt.method", payment.getPaymentMethod())).append("\n");
        // Add Center Name - this would ideally come from CenterProfileService via AppLauncher if needed here
        // receiptText.append(MessageProvider.getString("payment.receipt.centerName")).append("\n");

        AlertUtil.showInfo(MessageProvider.getString("payment.receipt.title"), receiptText.toString());
        logger.info("Placeholder: Receipt printed for payment ID {}", payment.getPaymentId());
    }
}
