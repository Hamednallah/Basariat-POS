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

    private Stage dialogStage;
    private ExpenseService expenseService;
    private ExpenseCategoryService expenseCategoryService;
    private BankNameService bankNameService;
    private ExpenseDTO currentExpense; // Used for both add (new DTO) and edit (passed DTO)
    private boolean saved = false;

    private final List<String> PAYMENT_METHODS = Arrays.asList("Cash", "Bank Transaction", "Card", "Cheque", "Other");

    public void initialize() {
        TextFormatters.applyBigDecimalFormatter(amountField);
        paymentMethodCombo.setItems(FXCollections.observableArrayList(PAYMENT_METHODS));

        paymentMethodCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            toggleBankFields(newVal);
        });

        categoryCombo.setConverter(new StringConverter<ExpenseCategoryDTO>() {
            @Override public String toString(ExpenseCategoryDTO cat) { return cat == null ? null : (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale()) ? cat.getCategoryNameAr() : cat.getCategoryNameEn()); }
            @Override public ExpenseCategoryDTO fromString(String string) { return null; }
        });
        bankNameCombo.setConverter(new StringConverter<BankNameDTO>() {
            @Override public String toString(BankNameDTO bank) { return bank == null ? null : (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale()) ? bank.getNameAr() : bank.getNameEn()); }
            @Override public BankNameDTO fromString(String string) { return null; }
        });

        dateField.setValue(LocalDate.now());
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (expenseFormDialogPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                expenseFormDialogPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                expenseFormDialogPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("expenseFormDialogPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    public void initializeDialog(ExpenseDTO expenseToEdit,
                                 ExpenseService expenseService,
                                 ExpenseCategoryService expenseCategoryService,
                                 BankNameService bankNameService,
                                 Stage stage) {
        this.expenseService = expenseService;
        this.expenseCategoryService = expenseCategoryService;
        this.bankNameService = bankNameService;
        this.dialogStage = stage;

        updateNodeOrientation();

        loadCategories();
        loadBankNames();

        if (expenseToEdit != null && expenseToEdit.getExpenseId() != null && expenseToEdit.getExpenseId() > 0) {
            this.currentExpense = expenseToEdit;
            titleLabel.setText(MessageProvider.getString("expenseform.dialog.title.edit"));
            populateFieldsForEdit();
        } else {
            titleLabel.setText(MessageProvider.getString("expenseform.dialog.title.add"));
            this.currentExpense = new ExpenseDTO();
        }

        toggleBankFields(paymentMethodCombo.getValue());

        final Button btOk = (Button) expenseFormDialogPane.lookupButton(ButtonType.OK);
        if (btOk != null) {
             btOk.setText(MessageProvider.getString("expenseform.button.save"));
             btOk.addEventFilter(ActionEvent.ACTION, this::handleSaveExpenseAction);
        } else {
            logger.error("Save button (ButtonType.OK) not found in DialogPane. Cannot attach save handler.");
        }
    }

    private void populateFieldsForEdit() {
        if (currentExpense == null) return;
        dateField.setValue(currentExpense.getExpenseDate());
        descriptionField.setText(currentExpense.getDescription());
        amountField.setText(currentExpense.getAmount() != null ? currentExpense.getAmount().toPlainString() : "");
        paymentMethodCombo.setValue(currentExpense.getPaymentMethod());

        if (currentExpense.getExpenseCategoryId() != null) {
            categoryCombo.getItems().stream()
                .filter(cat -> cat != null && cat.getExpenseCategoryId().equals(currentExpense.getExpenseCategoryId()))
                .findFirst().ifPresent(categoryCombo::setValue);
        }
        if (currentExpense.getBankNameId() != null) {
            bankNameCombo.getItems().stream()
                .filter(bank -> bank != null && bank.getBankNameId().equals(currentExpense.getBankNameId()))
                .findFirst().ifPresent(bankNameCombo::setValue);
        }
        transactionIdField.setText(currentExpense.getTransactionIdRef());
    }

    private void loadCategories() {
        try {
            List<ExpenseCategoryDTO> categories = expenseCategoryService.getActiveExpenseCategories().stream()
                .filter(cat -> !cat.isSystemReserved())
                .toList();
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
            event.consume();
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

        try {
            ExpenseDTO savedExpense = expenseService.recordExpense(currentExpense);
            AlertUtil.showSuccess(
                MessageProvider.getString("expenseform.success.title"),
                MessageProvider.getString("expenseform.success.message", String.valueOf(savedExpense.getExpenseId()))
            );
            saved = true;
        } catch (ExpenseValidationException | NoActiveShiftException | CategoryNotFoundException | BankNameNotFoundException | ExpenseException e) {
            logger.error("Failed to record expense: {}", e.getMessage(), e);
            AlertUtil.showError(MessageProvider.getString("expenseform.error.title"), e.getMessage());
            event.consume();
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
