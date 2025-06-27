package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.model.ExpenseDTO;
import com.basariatpos.service.ExpenseCategoryService;
import com.basariatpos.service.ExpenseService;
import com.basariatpos.ui.utilui.AlertUtil;
import com.basariatpos.util.AppLogger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ExpenseManagementController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(ExpenseManagementController.class);

    @FXML private DatePicker fromDateField;
    @FXML private DatePicker toDateField;
    @FXML private ComboBox<ExpenseCategoryDTO> categoryFilterCombo;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private TableView<ExpenseDTO> expensesTable;
    @FXML private TableColumn<ExpenseDTO, LocalDate> dateColumn;
    @FXML private TableColumn<ExpenseDTO, String> categoryColumn;
    @FXML private TableColumn<ExpenseDTO, String> descriptionColumn;
    @FXML private TableColumn<ExpenseDTO, BigDecimal> amountColumn;
    @FXML private TableColumn<ExpenseDTO, String> paymentMethodColumn;
    @FXML private TableColumn<ExpenseDTO, String> bankNameColumn;
    @FXML private TableColumn<ExpenseDTO, String> transactionIdColumn;
    @FXML private TableColumn<ExpenseDTO, String> recordedByColumn;
    @FXML private TableColumn<ExpenseDTO, Integer> shiftIdColumn;
    @FXML private Button addExpenseButton;
    @FXML private BorderPane expenseManagementPane; // For RTL

    private ExpenseService expenseService;
    private ExpenseCategoryService expenseCategoryService;
    private BankNameService bankNameService;
    private Stage currentStage; // For dialog ownership

    private ObservableList<ExpenseDTO> expenseList = FXCollections.observableArrayList();

    public void setServices(ExpenseService expenseService, ExpenseCategoryService expenseCategoryService, BankNameService bankNameService) {
        this.expenseService = expenseService;
        this.expenseCategoryService = expenseCategoryService;
        this.bankNameService = bankNameService;
        loadInitialData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        expensesTable.setItems(expenseList);
        setupCategoryFilterCombo();
        updateNodeOrientation();

        fromDateField.setValue(LocalDate.now().withDayOfMonth(1));
        toDateField.setValue(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
        updateNodeOrientation();
    }

    private void updateNodeOrientation() {
        if (expenseManagementPane != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                expenseManagementPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                expenseManagementPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("expenseManagementPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryNameEnDisplay")); // Assuming English display
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        bankNameColumn.setCellValueFactory(new PropertyValueFactory<>("bankNameDisplayEn")); // Assuming English display
        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("transactionIdRef"));
        recordedByColumn.setCellValueFactory(new PropertyValueFactory<>("createdByNameDisplay"));
        shiftIdColumn.setCellValueFactory(new PropertyValueFactory<>("shiftId"));

        // Custom cell factory for amount to format currency if needed
        amountColumn.setCellFactory(column -> new TableCell<ExpenseDTO, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", item)); // Basic number formatting
                }
            }
        });
    }

    private void setupCategoryFilterCombo() {
        categoryFilterCombo.setConverter(new StringConverter<ExpenseCategoryDTO>() {
            @Override
            public String toString(ExpenseCategoryDTO category) {
                return category == null ? MessageProvider.getString("combobox.allCategories") : category.getCategoryNameEn(); // Assuming English
            }
            @Override
            public ExpenseCategoryDTO fromString(String string) { return null; } // Not needed
        });
    }

    private void loadInitialData() {
        loadExpenseCategories();
        loadExpenses();
    }

    private void loadExpenseCategories() {
        if (expenseCategoryService == null) {
            logger.warn("ExpenseCategoryService not available. Categories cannot be loaded.");
            return;
        }
        try {
            List<ExpenseCategoryDTO> categories = expenseCategoryService.getAllExpenseCategories();
            ObservableList<ExpenseCategoryDTO> categoryList = FXCollections.observableArrayList();
            categoryList.add(null); // For "All Categories" option
            categoryList.addAll(categories);
            categoryFilterCombo.setItems(categoryList);
        } catch (Exception e) {
            logger.error("Failed to load expense categories: {}", e.getMessage(), e);
            AlertUtil.showError("Load Error", "Could not load expense categories for filter.");
        }
    }

    @FXML
    void handleApplyFilterAction(ActionEvent event) {
        loadExpenses();
    }

    @FXML
    void handleClearFilterAction(ActionEvent event) {
        fromDateField.setValue(LocalDate.now().withDayOfMonth(1));
        toDateField.setValue(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        categoryFilterCombo.setValue(null); // Select "All Categories"
        loadExpenses();
    }

    private void loadExpenses() {
        if (expenseService == null) {
            logger.warn("ExpenseService not available. Expenses cannot be loaded.");
            expenseList.clear();
            return;
        }
        LocalDate from = fromDateField.getValue();
        LocalDate to = toDateField.getValue();
        ExpenseCategoryDTO selectedCategory = categoryFilterCombo.getValue();
        Integer categoryId = (selectedCategory != null) ? selectedCategory.getExpenseCategoryId() : null;

        try {
            List<ExpenseDTO> expenses = expenseService.findExpenses(from, to, categoryId);
            expenseList.setAll(expenses);
        } catch (Exception e) {
            logger.error("Failed to load expenses: {}", e.getMessage(), e);
            AlertUtil.showError("Load Error", "Could not load expenses: " + e.getMessage());
            expenseList.clear();
        }
    }

    @FXML
    void handleAddExpenseAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ExpenseFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load(); // Changed to Parent for Scene constructor

            ExpenseFormDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("expenseform.dialog.title.add"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (addExpenseButton.getScene() != null && addExpenseButton.getScene().getWindow() != null ) {
                 dialogStage.initOwner(addExpenseButton.getScene().getWindow());
            }

            // Pass necessary services to the dialog controller
            controller.initializeDialog(null, // Null for new expense
                                        this.expenseService,
                                        this.expenseCategoryService,
                                        this.bankNameService, // Pass BankNameService
                                        dialogStage);

            Scene scene = new Scene(dialogRoot);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadExpenses(); // Refresh the list
            }

        } catch (IOException e) {
            logger.error("Failed to load ExpenseFormDialog.fxml: {}", e.getMessage(), e);
            AlertUtil.showError("UI Error", "Could not open the add expense form: " + e.getMessage());
        }
    }
}
