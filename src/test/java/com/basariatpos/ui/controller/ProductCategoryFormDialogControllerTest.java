package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.service.CategoryAlreadyExistsException;
import com.basariatpos.service.ProductCategoryService;
import com.basariatpos.service.ValidationException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class ProductCategoryFormDialogControllerTest {

    @Mock
    private ProductCategoryService mockProductCategoryService;

    private ProductCategoryFormDialogController controller;
    private Stage stage;

    private final String NAME_EN_FIELD = "#nameEnField";
    private final String NAME_AR_FIELD = "#nameArField";
    private final String SAVE_BUTTON = "#saveButton";
    private final String DIALOG_TITLE_LABEL = "#dialogTitleLabel";


    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ProductCategoryFormDialog.fxml"));
        loader.setResources(bundle);
        Parent root = loader.load();
        controller = loader.getController();

        controller.setProductCategoryService(mockProductCategoryService);
        controller.setDialogStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
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
    void addMode_initializesFieldsCorrectly(FxRobot robot) {
        // Title is set by caller, but FXML has a default. Here, we test controller's default state.
        assertEquals("", robot.lookup(NAME_EN_FIELD).queryAs(TextField.class).getText());
        assertEquals("", robot.lookup(NAME_AR_FIELD).queryAs(TextField.class).getText());
    }

    @Test
    void editMode_populatesFieldsCorrectly(FxRobot robot) {
        ProductCategoryDTO category = new ProductCategoryDTO(1, "Eyeglasses", "نظارات طبية");

        robot.interact(() -> controller.setEditableCategory(category));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Eyeglasses", robot.lookup(NAME_EN_FIELD).queryAs(TextField.class).getText());
        assertEquals("نظارات طبية", robot.lookup(NAME_AR_FIELD).queryAs(TextField.class).getText());
        assertEquals(MessageProvider.getString("productcategory.dialog.edit.title"), robot.lookup(DIALOG_TITLE_LABEL).queryAs(Label.class).getText());
    }

    @Test
    void saveButton_addMode_validInput_callsServiceAndCloses(FxRobot robot) throws Exception {
        ProductCategoryDTO savedCategory = new ProductCategoryDTO(10, "New Category EN", "فئة جديدة AR");
        when(mockProductCategoryService.saveProductCategory(any(ProductCategoryDTO.class))).thenReturn(savedCategory);

        robot.clickOn(NAME_EN_FIELD).write("New Category EN");
        robot.clickOn(NAME_AR_FIELD).write("فئة جديدة AR");
        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockProductCategoryService).saveProductCategory(any(ProductCategoryDTO.class));
        assertTrue(controller.isSaved());
        assertFalse(stage.isShowing());
    }

    @Test
    void saveButton_emptyEnglishName_showsValidationError(FxRobot robot) {
        robot.clickOn(NAME_AR_FIELD).write("اسم عربي");
        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(robot.lookup(".alert.error").tryQuery().orElse(null), "Error alert should be shown.");
        assertFalse(controller.isSaved());
        assertTrue(stage.isShowing());

        robot.targetWindow(robot.listWindows().stream().filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL).findFirst().get()).close();
    }

    @Test
    void cancelButton_closesDialog_isSavedFalse(FxRobot robot) {
        robot.clickOn("#cancelButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(controller.isSaved());
        assertFalse(stage.isShowing());
    }
}
