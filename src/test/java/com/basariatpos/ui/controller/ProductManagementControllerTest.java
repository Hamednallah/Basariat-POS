package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.service.ProductCategoryService;
import com.basariatpos.service.ProductService;
import com.basariatpos.service.exception.ProductInUseException;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
class ProductManagementControllerTest {

    @Mock private ProductService mockProductService;
    @Mock private ProductCategoryService mockProductCategoryService; // For dialog

    private ProductManagementController controller;
    private Stage stage;
    private MockedStatic<AppLauncher> appLauncherMockedStatic;

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        appLauncherMockedStatic = Mockito.mockStatic(AppLauncher.class);
        appLauncherMockedStatic.when(AppLauncher::getProductService).thenReturn(mockProductService);
        appLauncherMockedStatic.when(AppLauncher::getProductCategoryService).thenReturn(mockProductCategoryService);


        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ProductManagementView.fxml"));
        loader.setResources(bundle);
        Parent root = loader.load();
        controller = loader.getController();
        // Services are set in controller's initialize via AppLauncher

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws Exception {
        ProductDTO prod1 = new ProductDTO(1, "P001", "Sunglasses", "نظارات شمسية", 1, "Cat1 EN", "Cat1 AR", null, null, false, true);
        ProductDTO prod2 = new ProductDTO(2, "P002", "Lenses", "عدسات", 1, "Cat1 EN", "Cat1 AR", null, null, false, true);
        when(mockProductService.getAllProducts()).thenReturn(Arrays.asList(prod1, prod2));
        when(mockProductService.searchProducts(anyString())).thenReturn(Arrays.asList(prod1)); // Default search result

        // Trigger table load, as initialize should call it via AppLauncher mock
        controller.setProductService(mockProductService); // Re-inject to ensure load is called if initialize depends on it
        controller.setProductCategoryService(mockProductCategoryService);
    }

    @AfterEach
    void tearDown(FxRobot robot) {
        if (appLauncherMockedStatic != null) appLauncherMockedStatic.close();
        robot.listWindows().stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL)
            .forEach(w -> robot.targetWindow(w).close());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void tableIsPopulated_onInitialize(FxRobot robot) {
        TableView<ProductDTO> productsTable = robot.lookup("#productsTable").queryTableView();
        robot.waitUntil(() -> productsTable.getItems().size() == 2, 2000);
        assertEquals(2, productsTable.getItems().size());
        assertTrue(productsTable.getItems().stream().anyMatch(p -> "P001".equals(p.getProductCode())));
    }

    @Test
    void searchButton_filtersTable(FxRobot robot) throws Exception {
        robot.clickOn("#searchField").write("Sunglasses");
        robot.clickOn("#searchButton");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockProductService).searchProducts("Sunglasses");
        TableView<ProductDTO> productsTable = robot.lookup("#productsTable").queryTableView();
        assertEquals(1, productsTable.getItems().size()); // Based on default search mock
        assertEquals("P001", productsTable.getItems().get(0).getProductCode());
    }

    @Test
    void clearSearchButton_reloadsAllProducts(FxRobot robot) throws Exception {
        // First, filter
        robot.clickOn("#searchField").write("Sunglasses");
        robot.clickOn("#searchButton");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1, robot.lookup("#productsTable").queryTableView().getItems().size());

        // Then, clear
        robot.clickOn("#clearSearchButton");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockProductService, times(2)).getAllProducts(); // Initial load + clear search
        assertEquals(2, robot.lookup("#productsTable").queryTableView().getItems().size());
    }


    @Test
    void addProductButton_opensProductFormDialog(FxRobot robot) {
        robot.clickOn("#addProductButton");
        WaitForAsyncUtils.waitForFxEvents();

        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage &&
                                              ((Stage)w).getTitle().equals(MessageProvider.getString("product.dialog.add.title"))),
                   "Add Product dialog should open.");

        windows.stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("product.dialog.add.title")))
            .findFirst().ifPresent(w -> robot.targetWindow(w).close());
    }

    @Test
    void deleteProductButton_showsConfirmation_callsServiceOnConfirm(FxRobot robot) throws Exception {
        TableView<ProductDTO> productsTable = robot.lookup("#productsTable").queryTableView();
        robot.waitUntil(() -> !productsTable.getItems().isEmpty(), 1000);
        robot.interact(() -> productsTable.getSelectionModel().selectFirst());
        WaitForAsyncUtils.waitForFxEvents();

        ProductDTO selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        doNothing().when(mockProductService).deleteProduct(selectedProduct.getProductId());
        // Simulate list after deletion for refresh
        when(mockProductService.getAllProducts()).thenReturn(List.of(productsTable.getItems().get(1)));


        robot.clickOn("#deleteProductButton");
        WaitForAsyncUtils.waitForFxEvents();

        FxRobot dialogRobot = robot.targetWindow(
            robot.listWindows().stream()
                 .filter(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("product.confirm.delete.title")))
                 .findFirst().orElseThrow(() -> new AssertionError("Confirmation dialog not found"))
        );
        dialogRobot.clickOn((Node)dialogRobot.lookup(".button").match((Button b) -> b.isDefaultButton() || b.getText().equalsIgnoreCase("yes") || b.getText().equals(MessageProvider.getString("dialog.button.yes"))).query()); // More robust button finding
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockProductService).deleteProduct(selectedProduct.getProductId());
        // Check if table refreshed, e.g. one less item
        assertEquals(1, productsTable.getItems().size());
    }
}
