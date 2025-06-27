package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ExpenseCategoryDTO;
import com.basariatpos.service.CategoryAlreadyExistsException;
import com.basariatpos.service.CategoryException;
import com.basariatpos.service.ExpenseCategoryService;
import com.basariatpos.service.ValidationException;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseCategoryFormDialogControllerTest {

    @Mock private Label dialogTitleLabel;
    @Mock private TextField nameEnField;
    @Mock private TextField nameArField;
    @Mock private CheckBox activeCheckBox;
    @Mock private VBox expenseCategoryFormRootPane; // For RTL
    @Mock private Stage mockDialogStage;
    @Mock private ExpenseCategoryService mockExpenseCategoryService;

    @InjectMocks
    private ExpenseCategoryFormDialogController controller;

    private static ResourceBundle resourceBundle;

    @BeforeAll
    static void setUpClass() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        resourceBundle = MessageProvider.getBundle();
        try {
            new javafx.embed.swing.JFXPanel();
        } catch (Exception e) { /* Ignore */ }
    }

    @BeforeEach
    void setUp() {
        controller.dialogTitleLabel = dialogTitleLabel;
        controller.nameEnField = nameEnField;
        controller.nameArField = nameArField;
        controller.activeCheckBox = activeCheckBox;
        controller.expenseCategoryFormRootPane = expenseCategoryFormRootPane;

        controller.setExpenseCategoryService(mockExpenseCategoryService);
        controller.initialize(null, resourceBundle);
        controller.setDialogStage(mockDialogStage);
    }

    @Test
    void initialize_defaultsAndSetsOrientation() {
        verify(activeCheckBox).setSelected(true);
        verify(expenseCategoryFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void setEditableCategory_populatesForm_disablesFieldsIfProtected() {
        ExpenseCategoryDTO category = new ExpenseCategoryDTO("Food EN", "طعام", true);
        category.setExpenseCategoryId(1);
        when(mockExpenseCategoryService.isProtectedCategory(category)).thenReturn(true);

        controller.setEditableCategory(category);

        verify(dialogTitleLabel).setText(MessageProvider.getString("expensecategory.dialog.edit.title"));
        verify(nameEnField).setText("Food EN");
        verify(nameArField).setText("طعام");
        verify(activeCheckBox).setSelected(true);
        verify(nameEnField).setDisable(true);
        verify(nameArField).setDisable(true);
    }

    @Test
    void setEditableCategory_populatesForm_enablesFieldsIfNotProtected() {
        ExpenseCategoryDTO category = new ExpenseCategoryDTO("Travel EN", "سفر", true);
        category.setExpenseCategoryId(2);
        when(mockExpenseCategoryService.isProtectedCategory(category)).thenReturn(false);

        controller.setEditableCategory(category);
        // By default, fields are not disabled, so verify they are NOT disabled,
        // or ensure they are explicitly enabled if a previous state could have disabled them.
        // For this controller, setDisable(false) is not explicitly called if not protected,
        // so we rely on the default enabled state.
        verify(nameEnField, never()).setDisable(true);
        verify(nameArField, never()).setDisable(true);
    }


    @Test
    void handleSaveButtonAction_validInput_addMode_savesAndCloses() throws CategoryException, ValidationException {
        when(nameEnField.getText()).thenReturn("New Category EN");
        when(nameArField.getText()).thenReturn("فئة جديدة");
        when(activeCheckBox.isSelected()).thenReturn(true);

        controller.handleSaveButtonAction(null);

        assertTrue(controller.isSaved());
        ArgumentCaptor<ExpenseCategoryDTO> captor = ArgumentCaptor.forClass(ExpenseCategoryDTO.class);
        verify(mockExpenseCategoryService).saveExpenseCategory(captor.capture());
        assertEquals("New Category EN", captor.getValue().getCategoryNameEn());
        verify(mockDialogStage).close();
    }

    @Test
    void handleSaveButtonAction_serviceThrowsCategoryAlreadyExistsException_showsError() throws CategoryException, ValidationException {
        when(nameEnField.getText()).thenReturn("Existing EN");
        when(nameArField.getText()).thenReturn("موجود عربي");
        when(activeCheckBox.isSelected()).thenReturn(true);
        doThrow(new CategoryAlreadyExistsException("English name exists"))
            .when(mockExpenseCategoryService).saveExpenseCategory(any(ExpenseCategoryDTO.class));
        // For showErrorAlert to get owner stage
        when(mockDialogStage.getOwner()).thenReturn(null);
        when(nameEnField.getScene()).thenReturn(mock(javafx.scene.Scene.class)); // Mock scene
        when(nameEnField.getScene().getWindow()).thenReturn(mockDialogStage); // Mock window from scene


        controller.handleSaveButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initialize(null, resourceBundle);

        verify(expenseCategoryFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
