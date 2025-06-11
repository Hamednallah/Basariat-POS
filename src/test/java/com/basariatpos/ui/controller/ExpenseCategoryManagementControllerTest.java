package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.service.ExpenseCategoryService;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class ExpenseCategoryManagementControllerTest {

    @Mock
    private ExpenseCategoryService mockExpenseCategoryService;

    private ExpenseCategoryManagementController controller;
    private Stage stage;
    private MockedStatic<AppLauncher> appLauncherMockedStatic;

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        appLauncherMockedStatic = Mockito.mockStatic(AppLauncher.class);
        appLauncherMockedStatic.when(AppLauncher::getExpenseCategoryService).thenReturn(mockExpenseCategoryService);

        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ExpenseCategoryManagementView.fxml"));
        loader.setResources(bundle);
        Parent root = loader.load();
        controller = loader.getController();
        // Service is set in controller's initialize via AppLauncher.getExpenseCategoryService()

        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws Exception {
        ExpenseCategoryDTO cat1 = new ExpenseCategoryDTO(1, "Rent", "إيجار", true);
        ExpenseCategoryDTO cat2 = new ExpenseCategoryDTO(2, "Utilities", "خدمات", false);
        when(mockExpenseCategoryService.getAllExpenseCategories(true)).thenReturn(Arrays.asList(cat1, cat2));

        // If controller's initialize has already run, need to manually trigger data load with new mock setup.
        // The current controller's initialize method calls loadCategories(), which uses the service.
        // The static mock for AppLauncher should ensure the mock service is available at that point.
    }

    @AfterEach
    void tearDown(FxRobot robot) {
        if (appLauncherMockedStatic != null) {
            appLauncherMockedStatic.close();
        }
        robot.listWindows().stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL)
            .forEach(w -> robot.targetWindow(w).close());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void tableIsPopulated_onInitialize(FxRobot robot) {
        TableView<ExpenseCategoryDTO> categoriesTable = robot.lookup("#categoriesTable").queryTableView();
        assertNotNull(categoriesTable);
        robot.waitUntil(() -> categoriesTable.getItems().size() == 2, 2000);
        assertEquals(2, categoriesTable.getItems().size());

        ObservableList<ExpenseCategoryDTO> items = categoriesTable.getItems();
        assertTrue(items.stream().anyMatch(c -> c.getCategoryNameEn().equals("Rent")));
        assertTrue(items.stream().anyMatch(c -> c.getCategoryNameEn().equals("Utilities")));
    }

    @Test
    void addButton_opensCategoryFormDialog(FxRobot robot) {
        robot.clickOn("#addButton");
        WaitForAsyncUtils.waitForFxEvents();

        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage &&
                                              ((Stage)w).getTitle().equals(MessageProvider.getString("expensecategory.dialog.add.title"))),
                   "Add Expense Category dialog should open.");

        windows.stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("expensecategory.dialog.add.title")))
            .findFirst().ifPresent(w -> robot.targetWindow(w).close());
    }

    @Test
    void editButton_enablesAndOpensDialog_onSelection(FxRobot robot) {
        Button editButton = robot.lookup("#editButton").queryButton();
        assertTrue(editButton.isDisabled(), "Edit button should be disabled initially.");

        TableView<ExpenseCategoryDTO> categoriesTable = robot.lookup("#categoriesTable").queryTableView();
        robot.waitUntil(() -> !categoriesTable.getItems().isEmpty(), 1000);

        robot.interact(() -> categoriesTable.getSelectionModel().selectFirst());
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(editButton.isDisabled(), "Edit button should be enabled after selection.");

        robot.clickOn(editButton);
        WaitForAsyncUtils.waitForFxEvents();

        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage &&
                                              ((Stage)w).getTitle().equals(MessageProvider.getString("expensecategory.dialog.edit.title"))),
                   "Edit Expense Category dialog should open.");

        windows.stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("expensecategory.dialog.edit.title")))
            .findFirst().ifPresent(w -> robot.targetWindow(w).close());
    }

    // Add more tests for toggleActiveButton, error handling, etc.
}
