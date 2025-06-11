package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.service.ProductCategoryService;
import com.basariatpos.service.CategoryInUseException; // For testing delete error

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
class ProductCategoryManagementControllerTest {

    @Mock
    private ProductCategoryService mockProductCategoryService;

    private ProductCategoryManagementController controller;
    private Stage stage;
    private MockedStatic<AppLauncher> appLauncherMockedStatic;

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        appLauncherMockedStatic = Mockito.mockStatic(AppLauncher.class);
        appLauncherMockedStatic.when(AppLauncher::getProductCategoryService).thenReturn(mockProductCategoryService);

        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ProductCategoryManagementView.fxml"));
        loader.setResources(bundle);
        Parent root = loader.load();
        controller = loader.getController();
        // Service set in controller's initialize via AppLauncher.getProductCategoryService()

        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws Exception {
        ProductCategoryDTO cat1 = new ProductCategoryDTO(1, "Frames", "إطارات");
        ProductCategoryDTO cat2 = new ProductCategoryDTO(2, "Lenses", "عدسات");
        when(mockProductCategoryService.getAllProductCategories()).thenReturn(Arrays.asList(cat1, cat2));
        // Ensure controller reloads data if initialize has already run.
        // The AppLauncher static mock should ensure service is available from start.
        // If controller's initialize calls loadCategories, this is enough.
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
        TableView<ProductCategoryDTO> categoriesTable = robot.lookup("#categoriesTable").queryTableView();
        assertNotNull(categoriesTable);
        robot.waitUntil(() -> categoriesTable.getItems().size() == 2, 2000);
        assertEquals(2, categoriesTable.getItems().size());

        ObservableList<ProductCategoryDTO> items = categoriesTable.getItems();
        assertTrue(items.stream().anyMatch(c -> c.getCategoryNameEn().equals("Frames")));
        assertTrue(items.stream().anyMatch(c -> c.getCategoryNameEn().equals("Lenses")));
    }

    @Test
    void addButton_opensCategoryFormDialog(FxRobot robot) {
        robot.clickOn("#addButton");
        WaitForAsyncUtils.waitForFxEvents();

        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage &&
                                              ((Stage)w).getTitle().equals(MessageProvider.getString("productcategory.dialog.add.title"))),
                   "Add Product Category dialog should open.");

        windows.stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("productcategory.dialog.add.title")))
            .findFirst().ifPresent(w -> robot.targetWindow(w).close());
    }

    @Test
    void deleteButton_showsConfirmation_andCallsService_onConfirm(FxRobot robot) throws Exception {
        TableView<ProductCategoryDTO> categoriesTable = robot.lookup("#categoriesTable").queryTableView();
        robot.waitUntil(() -> !categoriesTable.getItems().isEmpty(), 1000);
        robot.interact(() -> categoriesTable.getSelectionModel().selectFirst());
        WaitForAsyncUtils.waitForFxEvents();

        doNothing().when(mockProductCategoryService).deleteProductCategory(anyInt());
        // Mock getAllProductCategories to return a list with one less item after deletion
        ProductCategoryDTO remainingCat = new ProductCategoryDTO(2, "Lenses", "عدسات");
        when(mockProductCategoryService.getAllProductCategories()).thenReturn(Arrays.asList(remainingCat));


        robot.clickOn("#deleteButton");
        WaitForAsyncUtils.waitForFxEvents(); // For confirmation dialog

        // Confirm deletion
        FxRobot dialogRobot = robot.targetWindow(
            robot.listWindows().stream()
                 .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("productcategory.confirm.delete.title")))
                 .findFirst().orElseThrow(() -> new AssertionError("Confirmation dialog not found"))
        );
        dialogRobot.clickOn((Node)dialogRobot.lookup(".button").match((Button b) -> b.isDefaultButton() || b.getText().equalsIgnoreCase("yes")).query());
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockProductCategoryService).deleteProductCategory(categoriesTable.getSelectionModel().getSelectedItem().getCategoryId() == 1 ? 1 : categoriesTable.getItems().get(0).getCategoryId());
        verify(mockProductCategoryService, times(2)).getAllProductCategories(); // Initial load + refresh
        assertEquals(1, categoriesTable.getItems().size());
    }

    @Test
    void deleteButton_categoryInUse_showsErrorAlert(FxRobot robot) throws Exception {
        TableView<ProductCategoryDTO> categoriesTable = robot.lookup("#categoriesTable").queryTableView();
        robot.waitUntil(() -> !categoriesTable.getItems().isEmpty(), 1000);
        ProductCategoryDTO categoryToDelete = categoriesTable.getItems().get(0); // Get first item to delete
        robot.interact(() -> categoriesTable.getSelectionModel().select(categoryToDelete));
        WaitForAsyncUtils.waitForFxEvents();

        doThrow(new CategoryInUseException(categoryToDelete.getCategoryId()))
            .when(mockProductCategoryService).deleteProductCategory(categoryToDelete.getCategoryId());

        robot.clickOn("#deleteButton");
        WaitForAsyncUtils.waitForFxEvents(); // For confirmation dialog

        // Confirm deletion in the first dialog
        FxRobot confirmationDialogRobot = robot.targetWindow(
            robot.listWindows().stream()
                 .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("productcategory.confirm.delete.title")))
                 .findFirst().orElseThrow(() -> new AssertionError("Confirmation dialog not found"))
        );
        confirmationDialogRobot.clickOn((Node)confirmationDialogRobot.lookup(".button").match((Button b) -> b.isDefaultButton() || b.getText().equalsIgnoreCase("yes")).query());
        WaitForAsyncUtils.waitForFxEvents(); // For error alert

        // Verify error alert is shown
        assertNotNull(robot.lookup(".alert.error").tryQuery().orElse(null), "Error alert for category in use should be shown.");

        // Close the error alert
        robot.targetWindow(
            robot.listWindows().stream()
                 .filter(w -> w instanceof Stage && "Error Deleting Category".equals(((Stage)w).getTitle())) // Assuming this title for error
                 .findFirst().orElseThrow(() -> new AssertionError("Error alert not found"))
        ).close();
    }
}
