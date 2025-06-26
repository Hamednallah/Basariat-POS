package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.model.ExpenseDTO;
import com.basariatpos.service.BankNameService;
import com.basariatpos.service.ExpenseCategoryService;
import com.basariatpos.service.ExpenseService;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class ExpenseManagementControllerTest {

    @Mock private ExpenseService mockExpenseService;
    @Mock private ExpenseCategoryService mockExpenseCategoryService;
    @Mock private BankNameService mockBankNameService; // Needed for Add Expense dialog

    private ExpenseManagementController controller;
    private Stage stage;

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

        // Mock service calls for initial data load
        List<ExpenseCategoryDTO> categories = new ArrayList<>();
        categories.add(new ExpenseCategoryDTO(1, "Travel EN", "Travel AR", "", true));
        categories.add(new ExpenseCategoryDTO(2, "Utilities EN", "Utilities AR", "", true));
        when(mockExpenseCategoryService.getAllExpenseCategories()).thenReturn(categories);
        when(mockExpenseService.findExpenses(any(LocalDate.class), any(LocalDate.class), isNull())).thenReturn(new ArrayList<>());


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ExpenseManagementView.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();
        controller = loader.getController();

        // Call setServices on JavaFX Application Thread
        Platform.runLater(() -> controller.setServices(mockExpenseService, mockExpenseCategoryService, mockBankNameService));
        WaitForAsyncUtils.waitForFxEvents();

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle(MessageProvider.getString("expensemanagement.title"));
        stage.show();
    }

    @Test
    void initialize_loadsCategoriesAndInitialExpenses(FxRobot robot) {
        verify(mockExpenseCategoryService).getAllExpenseCategories();
        assertNotNull(controller.categoryFilterCombo.getItems());
        // First item is null for "All Categories", then mocked categories
        assertEquals(3, controller.categoryFilterCombo.getItems().size());

        verify(mockExpenseService).findExpenses(
            eq(LocalDate.now().withDayOfMonth(1)),
            eq(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())),
            isNull()
        );
        assertTrue(controller.expensesTable.getItems().isEmpty()); // As mock returns empty list
    }

    @Test
    void handleApplyFilterAction_callsServiceAndUpdatesTable(FxRobot robot) {
        LocalDate fromDate = LocalDate.now().minusDays(10);
        LocalDate toDate = LocalDate.now().minusDays(1);
        ExpenseCategoryDTO category = new ExpenseCategoryDTO(1, "Travel EN", "Travel AR", "", true);

        List<ExpenseDTO> filteredExpenses = new ArrayList<>();
        ExpenseDTO exp1 = new ExpenseDTO(); exp1.setDescription("Filtered Expense 1"); exp1.setAmount(BigDecimal.TEN);
        filteredExpenses.add(exp1);
        when(mockExpenseService.findExpenses(fromDate, toDate, category.getExpenseCategoryId())).thenReturn(filteredExpenses);

        robot.interact(() -> {
            controller.fromDateField.setValue(fromDate);
            controller.toDateField.setValue(toDate);
            controller.categoryFilterCombo.setValue(category);
        });
        robot.clickOn("#applyFilterButton");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockExpenseService).findExpenses(fromDate, toDate, category.getExpenseCategoryId());
        assertEquals(1, controller.expensesTable.getItems().size());
        assertEquals("Filtered Expense 1", controller.expensesTable.getItems().get(0).getDescription());
    }

    @Test
    void handleClearFilterAction_resetsFiltersAndReloads(FxRobot robot) {
        // Set some filters first
        robot.interact(() -> {
            controller.fromDateField.setValue(LocalDate.now().minusDays(5));
            controller.categoryFilterCombo.setValue(new ExpenseCategoryDTO(1, "Test", "Test", "", true));
        });

        robot.clickOn("#clearFilterButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(LocalDate.now().withDayOfMonth(1), controller.fromDateField.getValue());
        assertNull(controller.categoryFilterCombo.getValue());
        verify(mockExpenseService, times(2)).findExpenses( // Once on init, once on clear
            eq(LocalDate.now().withDayOfMonth(1)),
            eq(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())),
            isNull()
        );
    }

    @Test
    void handleAddExpenseAction_opensDialog_refreshesTableOnSave(FxRobot robot) throws Exception {
        // Mock what happens when ExpenseFormDialog is "saved"
        try (var fxmlLoaderMockedConstruction = mockConstruction(FXMLLoader.class, (mock, context) -> {
            if (context.arguments().get(0) instanceof URL && ((URL)context.arguments().get(0)).getPath().contains("ExpenseFormDialog.fxml")) {
                ExpenseFormDialogController mockDialogController = mock(ExpenseFormDialogController.class);
                when(mock.load()).thenReturn(new DialogPane()); // Return a dummy Parent
                when(mock.getController()).thenReturn(mockDialogController);
                when(mockDialogController.isSaved()).thenReturn(true); // Simulate dialog being saved
            }
        })) {
            // This will be called again after dialog "save"
            when(mockExpenseService.findExpenses(any(LocalDate.class), any(LocalDate.class), isNull()))
                .thenReturn(List.of(new ExpenseDTO())); // Return one item to show refresh

            robot.clickOn("#addExpenseButton");
            WaitForAsyncUtils.waitForFxEvents();

            // Verify findExpenses was called again (initial load + after dialog save)
            verify(mockExpenseService, times(2)).findExpenses(any(LocalDate.class), any(LocalDate.class), isNull());
            assertEquals(1, controller.expensesTable.getItems().size());
        }
    }
}
