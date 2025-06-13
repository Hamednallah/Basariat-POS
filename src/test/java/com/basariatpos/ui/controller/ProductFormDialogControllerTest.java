package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.service.ProductCategoryService;
import com.basariatpos.service.ProductService;
import com.basariatpos.service.exception.ProductAlreadyExistsException;
import com.basariatpos.service.exception.ProductValidationException;
import com.basariatpos.service.exception.CategoryNotFoundException;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class ProductFormDialogControllerTest {

    @Mock private ProductService mockProductService;
    @Mock private ProductCategoryService mockProductCategoryService;

    private ProductFormDialogController controller;
    private Stage stage;

    private final String PRODUCT_CODE_FIELD = "#productCodeField";
    private final String NAME_EN_FIELD = "#nameEnField";
    private final String NAME_AR_FIELD = "#nameArField";
    private final String CATEGORY_COMBOBOX = "#categoryComboBox";
    private final String IS_SERVICE_CHECKBOX = "#isServiceCheckBox";
    private final String IS_STOCK_ITEM_CHECKBOX = "#isStockItemCheckBox";
    private final String SAVE_BUTTON = "#saveButton";

    private List<ProductCategoryDTO> mockCategories;

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        ResourceBundle bundle = MessageProvider.getBundle();

        mockCategories = Arrays.asList(
            new ProductCategoryDTO(1, "Frames", "إطارات"),
            new ProductCategoryDTO(2, "Lenses", "عدسات")
        );
        when(mockProductCategoryService.getAllProductCategories()).thenReturn(mockCategories);


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ProductFormDialog.fxml"));
        loader.setResources(bundle);
        Parent root = loader.load();
        controller = loader.getController();

        // InitializeDialog is called by the parent controller in real app. Here we call it manually for test setup.
        // For add mode:
        // controller.initializeDialog(mockProductService, mockProductCategoryService, stage, null);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        // Title is usually set by the calling controller (ProductManagementController)
        // stage.setTitle(MessageProvider.getString("product.dialog.add.title"));
        stage.show();
        stage.toFront();
    }

    @AfterEach
    void tearDown(FxRobot robot) {
        robot.listWindows().stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL && w != stage)
            .forEach(w -> robot.targetWindow(w).close());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void addMode_initializesCorrectly(FxRobot robot) {
        robot.interact(() -> controller.initializeDialog(mockProductService, mockProductCategoryService, stage, null));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("", robot.lookup(PRODUCT_CODE_FIELD).queryAs(TextField.class).getText());
        assertFalse(robot.lookup(IS_SERVICE_CHECKBOX).queryAs(CheckBox.class).isSelected());
        assertTrue(robot.lookup(IS_STOCK_ITEM_CHECKBOX).queryAs(CheckBox.class).isSelected());
        assertFalse(robot.lookup(IS_STOCK_ITEM_CHECKBOX).queryAs(CheckBox.class).isDisabled());
        ComboBox<ProductCategoryDTO> categoryCombo = robot.lookup(CATEGORY_COMBOBOX).queryComboBox();
        assertEquals(2, categoryCombo.getItems().size());
    }

    @Test
    void editMode_populatesFieldsCorrectly(FxRobot robot) {
        ProductDTO productToEdit = new ProductDTO(1, "P123", "Test Sunglass", "نظارة شمسية", 1,
                                                "Frames", "إطارات", "Desc", "وصف", false, true);
        robot.interact(() -> controller.initializeDialog(mockProductService, mockProductCategoryService, stage, productToEdit));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("P123", robot.lookup(PRODUCT_CODE_FIELD).queryAs(TextField.class).getText());
        assertEquals("Test Sunglass", robot.lookup(NAME_EN_FIELD).queryAs(TextField.class).getText());
        ComboBox<ProductCategoryDTO> categoryCombo = robot.lookup(CATEGORY_COMBOBOX).queryComboBox();
        assertNotNull(categoryCombo.getValue());
        assertEquals(1, categoryCombo.getValue().getCategoryId());
        assertFalse(robot.lookup(IS_SERVICE_CHECKBOX).queryAs(CheckBox.class).isSelected());
        assertTrue(robot.lookup(IS_STOCK_ITEM_CHECKBOX).queryAs(CheckBox.class).isSelected());
    }

    @Test
    void isServiceCheckbox_toggles_isStockItemCheckbox(FxRobot robot) {
        robot.interact(() -> controller.initializeDialog(mockProductService, mockProductCategoryService, stage, null));
        WaitForAsyncUtils.waitForFxEvents();

        CheckBox serviceCb = robot.lookup(IS_SERVICE_CHECKBOX).queryAs(CheckBox.class);
        CheckBox stockCb = robot.lookup(IS_STOCK_ITEM_CHECKBOX).queryAs(CheckBox.class);

        robot.clickOn(serviceCb); // Check isService
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(stockCb.isSelected());
        assertTrue(stockCb.isDisabled());

        robot.clickOn(serviceCb); // Uncheck isService
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(stockCb.isSelected()); // Should default back to true
        assertFalse(stockCb.isDisabled());
    }


    @Test
    void saveButton_addMode_validInput_callsServiceAndCloses(FxRobot robot) throws Exception {
        robot.interact(() -> controller.initializeDialog(mockProductService, mockProductCategoryService, stage, null));
        when(mockProductService.saveProduct(any(ProductDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        robot.clickOn(PRODUCT_CODE_FIELD).write("NEW001");
        robot.clickOn(NAME_EN_FIELD).write("New Product EN");
        robot.clickOn(NAME_AR_FIELD).write("منتج جديد AR");
        robot.interact(() -> robot.lookup(CATEGORY_COMBOBOX).queryComboBox().getSelectionModel().selectFirst());
        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockProductService).saveProduct(any(ProductDTO.class));
        assertTrue(controller.isSaved());
        assertFalse(stage.isShowing());
    }

    @Test
    void saveButton_missingProductNameEn_showsValidationError(FxRobot robot) {
        robot.interact(() -> controller.initializeDialog(mockProductService, mockProductCategoryService, stage, null));
        robot.clickOn(PRODUCT_CODE_FIELD).write("P005");
        robot.clickOn(NAME_AR_FIELD).write("منتج بالعربي فقط");
        robot.interact(() -> robot.lookup(CATEGORY_COMBOBOX).queryComboBox().getSelectionModel().selectFirst());

        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(robot.lookup(".alert.error").tryQuery().orElse(null), "Error alert should be shown.");
        assertFalse(controller.isSaved());
        assertTrue(stage.isShowing());

        robot.targetWindow(robot.listWindows().stream().filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL).findFirst().get()).close();
    }
}
