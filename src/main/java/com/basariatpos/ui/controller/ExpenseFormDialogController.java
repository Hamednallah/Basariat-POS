package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.model.ExpenseDTO;
import com.basariatpos.service.BankNameService;
import com.basariatpos.service.ExpenseCategoryService;
import com.basariatpos.service.ExpenseService;
import com.basariatpos.service.exception.*;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpenseFormDialogController {

    private static final Logger logger = AppLogger.getLogger(ExpenseFormDialogController.class);

    @FXML private DialogPane expenseFormDialogPane;
    @FXML private Label titleLabel;
    @FXML private DatePicker dateField;
    @FXML private ComboBox<ExpenseCategoryDTO> categoryCombo;
    @FXML private TextArea descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Label bankNamePromptLabel;
    @FXML private ComboBox<BankNameDTO> bankNameCombo;
    @FXML private Label transactionIdPromptLabel;
    @FXML private TextField transactionIdField;
    // Save and Cancel buttons are typically part of DialogPane's buttonTypes

    private Stage dialogStage;
    private ExpenseService expenseService;
    private ExpenseCategoryService expenseCategoryService;
    private BankNameService bankNameService;
    private ExpenseDTO currentExpense;
    private boolean saved = false;

    private final List<String> PAYMENT_METHODS = Arrays.asList("Cash", "Bank Transaction", "Card", "Cheque", "Other");

    public void initialize() {
        TextFormatters.applyBigDecimalFormatter(amountField);
        paymentMethodCombo.setItems(FXCollections.observableArrayList(PAYMENT_METHODS));

        paymentMethodCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            toggleBankFields(newVal);
        });

        categoryCombo.setConverter(new StringConverter<ExpenseCategoryDTO>() {
            @Override public String toString(ExpenseCategoryDTO cat) { return cat == null ? null : cat.getCategoryNameEn(); } // Assuming English
            @Override public ExpenseCategoryDTO fromString(String string) { return null; }
        });
        bankNameCombo.setConverter(new StringConverter<BankNameDTO>() {
            @Override public String toString(BankNameDTO bank) { return bank == null ? null : bank.getNameEn(); } // Assuming English
            @Override public BankNameDTO fromString(String string) { return null; }
        });

        // Default date to today
        dateField.setValue(LocalDate.now());
    }

    public void initializeDialog(ExpenseDTO expenseToEdit,
                                 ExpenseService expenseService,
                                 ExpenseCategoryService expenseCategoryService,
                                 BankNameService bankNameService,
                                 Stage stage) {
        this.currentExpense = expenseToEdit;
        this.expenseService = expenseService;
        this.expenseCategoryService = expenseCategoryService;
        this.bankNameService = bankNameService;
        this.dialogStage = stage;

        loadCategories();
        loadBankNames(); // Load banks irrespective of initial payment method

        if (currentExpense != null) { // Editing existing expense
            titleLabel.setText(MessageProvider.getString("expenseform.dialog.title.edit"));
            // Populate fields from currentExpense (not implemented for this subtask, focuses on Add)
            // For add, currentExpense is null.
        } else { // Adding new expense
            titleLabel.setText(MessageProvider.getString("expenseform.dialog.title.add"));
            this.currentExpense = new ExpenseDTO(); // Create a new DTO for add
        }

        toggleBankFields(null); // Set initial state of bank fields

        // Handle Save button action via DialogPane's button types
        final Button btOk = (Button) expenseFormDialogPane.lookupButton(ButtonType.OK);
        if (btOk != null) { // Might be null if FXML is not fully loaded or no OK_DONE ButtonType
             btOk.setText(MessageProvider.getString("expenseform.button.save"));
             btOk.addEventFilter(ActionEvent.ACTION, this::handleSaveExpenseAction);
        } else {
            logger.error("Save button (ButtonType.OK) not found in DialogPane. Cannot attach save handler.");
        }
    }

    private void loadCategories() {
        try {
            List<ExpenseCategoryDTO> categories = expenseCategoryService.getActiveExpenseCategories();
            categoryCombo.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            logger.error("Failed to load expense categories: {}", e.getMessage(), e);
            AlertUtil.showError("Load Error", "Could not load expense categories.");
        }
    }

    private void loadBankNames() {
        try {
            List<BankNameDTO> banks = bankNameService.getActiveBankNames();
            bankNameCombo.setItems(FXCollections.observableArrayList(banks));
        } catch (Exception e) {
            logger.error("Failed to load bank names: {}", e.getMessage(), e);
            AlertUtil.showError("Load Error", "Could not load bank names.");
        }
    }

    private void toggleBankFields(String paymentMethod) {
        boolean bankDetailsRequired = false;
        if (paymentMethod != null) {
            String methodUpper = paymentMethod.toUpperCase();
            bankDetailsRequired = methodUpper.contains("BANK") || methodUpper.contains("CARD") || methodUpper.contains("CHEQUE");
        }

        bankNamePromptLabel.setVisible(bankDetailsRequired); bankNamePromptLabel.setManaged(bankDetailsRequired);
        bankNameCombo.setVisible(bankDetailsRequired); bankNameCombo.setManaged(bankDetailsRequired);
        transactionIdPromptLabel.setVisible(bankDetailsRequired); transactionIdPromptLabel.setManaged(bankDetailsRequired);
        transactionIdField.setVisible(bankDetailsRequired); transactionIdField.setManaged(bankDetailsRequired);
    }

    private void handleSaveExpenseAction(ActionEvent event) {
        List<String> errors = new ArrayList<>();
        if (dateField.getValue() == null) errors.add(MessageProvider.getString("expenseform.validation.dateRequired"));
        if (categoryCombo.getValue() == null) errors.add(MessageProvider.getString("expenseform.validation.categoryRequired"));
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) errors.add(MessageProvider.getString("expenseform.validation.descriptionRequired"));

        BigDecimal amount = null;
        try {
            amount = TextFormatters.parseBigDecimal(amountField.getText());
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(MessageProvider.getString("expenseform.validation.amountPositive"));
            }
        } catch (NumberFormatException e) {
            errors.add(MessageProvider.getString("expenseform.validation.amountPositive"));
        }

        String paymentMethod = paymentMethodCombo.getValue();
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            errors.add(MessageProvider.getString("expenseform.validation.paymentMethodRequired"));
        } else {
            if (isBankRelatedPayment(paymentMethod)) {
                if (bankNameCombo.getValue() == null) errors.add(MessageProvider.getString("expenseform.validation.bankNameRequired"));
                if (transactionIdField.getText() == null || transactionIdField.getText().trim().isEmpty()) errors.add(MessageProvider.getString("expenseform.validation.transactionIdRequired"));
            }
        }

        if (!errors.isEmpty()) {
            AlertUtil.showValidationError(errors);
            event.consume(); // Prevent dialog from closing
            return;
        }

        currentExpense.setExpenseDate(dateField.getValue());
        currentExpense.setExpenseCategoryId(categoryCombo.getValue().getExpenseCategoryId());
        currentExpense.setDescription(descriptionField.getText().trim());
        currentExpense.setAmount(amount);
        currentExpense.setPaymentMethod(paymentMethod);

        if (isBankRelatedPayment(paymentMethod)) {
            currentExpense.setBankNameId(bankNameCombo.getValue().getBankNameId());
            currentExpense.setTransactionIdRef(transactionIdField.getText().trim());
        } else {
            currentExpense.setBankNameId(null);
            currentExpense.setTransactionIdRef(null);
        }
        // createdByUserId and shiftId (for cash) will be set by the service

        try {
            ExpenseDTO savedExpense = expenseService.recordExpense(currentExpense);
            AlertUtil.showSuccess(
                MessageProvider.getString("expenseform.success.title"),
                MessageProvider.getString("expenseform.success.message", String.valueOf(savedExpense.getExpenseId()))
            );
            saved = true;
            closeDialog(); // DialogPane closes automatically if event not consumed
        } catch (ExpenseValidationException | NoActiveShiftException | CategoryNotFoundException | BankNameNotFoundException | ExpenseException e) {
            logger.error("Failed to record expense: {}", e.getMessage(), e);
            AlertUtil.showError(MessageProvider.getString("expenseform.error.title"), e.getMessage());
            event.consume(); // Prevent dialog from closing on error
        }
    }

    private boolean isBankRelatedPayment(String paymentMethod) {
        if (paymentMethod == null) return false;
        String methodUpper = paymentMethod.toUpperCase();
        return methodUpper.contains("BANK") || methodUpper.contains("CARD") || methodUpper.contains("CHEQUE");
    }

    public boolean isSaved() {
        return saved;
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
