package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.service.ProductCategoryService;
import com.basariatpos.service.ProductService;
import com.basariatpos.service.exception.ProductAlreadyExistsException;
import com.basariatpos.service.exception.ProductServiceException;
import com.basariatpos.service.exception.ProductValidationException;
import com.basariatpos.service.exception.CategoryNotFoundException;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductFormDialogControllerTest {

    @Mock private Label dialogTitleLabel;
    @Mock private TextField productCodeField;
    @Mock private TextField nameEnField;
    @Mock private TextField nameArField;
    @Mock private TextArea descriptionEnArea;
    @Mock private TextArea descriptionArArea;
    @Mock private ComboBox<ProductCategoryDTO> categoryComboBox;
    @Mock private CheckBox isServiceCheckBox;
    @Mock private CheckBox isStockItemCheckBox;
    @Mock private VBox productFormRootPane; // For RTL
    @Mock private Stage mockDialogStage;
    @Mock private ProductService mockProductService;
    @Mock private ProductCategoryService mockProductCategoryService;

    @InjectMocks
    private ProductFormDialogController controller;

    private static ResourceBundle resourceBundle;
    private ProductCategoryDTO sampleCategory;

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
        // Manual FXML injection
        controller.dialogTitleLabel = dialogTitleLabel;
        controller.productCodeField = productCodeField;
        controller.nameEnField = nameEnField;
        controller.nameArField = nameArField;
        controller.descriptionEnArea = descriptionEnArea;
        controller.descriptionArArea = descriptionArArea;
        controller.categoryComboBox = categoryComboBox;
        controller.isServiceCheckBox = isServiceCheckBox;
        controller.isStockItemCheckBox = isStockItemCheckBox;
        controller.productFormRootPane = productFormRootPane;

        sampleCategory = new ProductCategoryDTO("Electronics EN", "إلكترونيات");
        sampleCategory.setCategoryId(1);
        try {
            when(mockProductCategoryService.getAllProductCategories()).thenReturn(List.of(sampleCategory));
        } catch (Exception e) { fail("Mock setup failed: " + e.getMessage()); }

        // Mock ComboBox behavior
        when(categoryComboBox.getItems()).thenReturn(FXCollections.observableArrayList(sampleCategory));
        when(categoryComboBox.getValue()).thenReturn(sampleCategory); // Assume a category is selected for valid saves

        // Mock CheckBox selectedProperty for listeners
        // These are critical for the listener logic in initializeDialog
        when(isServiceCheckBox.selectedProperty()).thenReturn(new javafx.beans.property.SimpleBooleanProperty(false));
        when(isStockItemCheckBox.selectedProperty()).thenReturn(new javafx.beans.property.SimpleBooleanProperty(true));


        // initializeDialog is the entry point
        // controller.initializeDialog(mockProductService, mockProductCategoryService, mockDialogStage, null);
    }

    @Test
    void initializeDialog_addMode_setsDefaultsAndOrientation() {
        controller.initializeDialog(mockProductService, mockProductCategoryService, mockDialogStage, null);
        verify(dialogTitleLabel).setText(MessageProvider.getString("product.dialog.add.title"));
        verify(isStockItemCheckBox).setSelected(true);
        verify(isServiceCheckBox).setSelected(false);
        verify(productFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
        verify(categoryComboBox).setItems(any(ObservableList.class));
    }

    @Test
    void initializeDialog_editMode_populatesFields() {
        ProductDTO product = new ProductDTO();
        product.setProductId(1L);
        product.setProductCode("P001");
        product.setProductNameEn("Test Product");
        product.setCategoryId(sampleCategory.getCategoryId()); // Link to sample category
        product.setService(true);
        product.setStockItem(false); // Explicitly set for service

        // Reset selectedProperty mocks for this specific scenario if needed, or ensure they reflect 'product.isService()'
        when(isServiceCheckBox.selectedProperty()).thenReturn(new javafx.beans.property.SimpleBooleanProperty(product.isService()));


        controller.initializeDialog(mockProductService, mockProductCategoryService, mockDialogStage, product);

        verify(dialogTitleLabel).setText(MessageProvider.getString("product.dialog.edit.title"));
        verify(productCodeField).setText("P001");
        verify(nameEnField).setText("Test Product");
        verify(isServiceCheckBox).setSelected(true);
        verify(isStockItemCheckBox).setDisable(true);
        verify(isStockItemCheckBox).setSelected(false);
        verify(categoryComboBox).setValue(sampleCategory);
    }

    @Test
    void handleSaveButtonAction_addMode_validInput_savesProduct() throws ProductServiceException {
        controller.initializeDialog(mockProductService, mockProductCategoryService, mockDialogStage, null);

        when(productCodeField.getText()).thenReturn("NEW001");
        when(nameEnField.getText()).thenReturn("New Product EN");
        when(nameArField.getText()).thenReturn("منتج جديد");
        // categoryComboBox mock already returns sampleCategory
        when(isServiceCheckBox.isSelected()).thenReturn(false);
        when(isStockItemCheckBox.isSelected()).thenReturn(true);

        ProductDTO savedDto = new ProductDTO();
        savedDto.setProductNameEn("New Product EN"); // Set a field to verify returned DTO
        when(mockProductService.saveProduct(any(ProductDTO.class))).thenReturn(savedDto);

        controller.handleSaveButtonAction(null);

        assertTrue(controller.isSaved());
        assertEquals(savedDto, controller.getSavedProduct());
        verify(mockDialogStage).close();
        ArgumentCaptor<ProductDTO> captor = ArgumentCaptor.forClass(ProductDTO.class);
        verify(mockProductService).saveProduct(captor.capture());
        assertEquals("NEW001", captor.getValue().getProductCode());
        assertTrue(captor.getValue().isStockItem());
        assertFalse(captor.getValue().isService());
    }

    @Test
    void handleSaveButtonAction_invalidCode_showsError() {
        controller.initializeDialog(mockProductService, mockProductCategoryService, mockDialogStage, null);
        when(productCodeField.getText()).thenReturn(""); // Invalid
        when(nameEnField.getText()).thenReturn("Name");
        when(nameArField.getText()).thenReturn("اسم");
        // For showErrorAlert
        when(mockDialogStage.getOwner()).thenReturn(null);
        when(productCodeField.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(productCodeField.getScene().getWindow()).thenReturn(mockDialogStage);


        controller.handleSaveButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void initializeDialog_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initializeDialog(mockProductService, mockProductCategoryService, mockDialogStage, null);

        verify(productFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
