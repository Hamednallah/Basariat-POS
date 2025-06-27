package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.service.InventoryItemService;
import com.basariatpos.service.ProductService;
import com.basariatpos.service.exception.InventoryItemValidationException;
import com.basariatpos.service.exception.ProductNotFoundException;

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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryItemFormDialogControllerTest {

    @Mock private Label dialogTitleLabel;
    @Mock private ComboBox<ProductDTO> productComboBox;
    @Mock private TextField brandNameField, specificNameEnField, specificNameArField, unitOfMeasureField;
    @Mock private TextArea attributesArea;
    @Mock private TextField quantityOnHandField, sellingPriceField, costPriceField, minStockLevelField;
    @Mock private CheckBox activeCheckBox;
    @Mock private VBox inventoryItemFormRootPane; // For RTL
    @Mock private Stage mockDialogStage;
    @Mock private InventoryItemService mockItemService;
    @Mock private ProductService mockProductService;

    @InjectMocks
    private InventoryItemFormDialogController controller;

    private static ResourceBundle resourceBundle;
    private ProductDTO sampleProduct;

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
        controller.productComboBox = productComboBox;
        controller.brandNameField = brandNameField;
        controller.specificNameEnField = specificNameEnField;
        controller.specificNameArField = specificNameArField;
        controller.unitOfMeasureField = unitOfMeasureField;
        controller.attributesArea = attributesArea;
        controller.quantityOnHandField = quantityOnHandField;
        controller.sellingPriceField = sellingPriceField;
        controller.costPriceField = costPriceField;
        controller.minStockLevelField = minStockLevelField;
        controller.activeCheckBox = activeCheckBox;
        controller.inventoryItemFormRootPane = inventoryItemFormRootPane;

        sampleProduct = new ProductDTO();
        sampleProduct.setProductId(1);
        sampleProduct.setProductNameEn("Test Product");
        sampleProduct.setProductNameAr("منتج اختباري");
        try {
            when(mockProductService.getAllProducts()).thenReturn(List.of(sampleProduct));
        } catch (Exception e) { fail("Mock setup failed: " + e.getMessage());}

        when(productComboBox.getItems()).thenReturn(FXCollections.observableArrayList(sampleProduct));
        // when(productComboBox.getValue()).thenReturn(sampleProduct); // Set this in specific tests if needed

        controller.initialize(); // Calls updateNodeOrientation & setupNumericFieldFormatters
        // initializeDialog is the main entry point after FXML load by parent
        // controller.initializeDialog(mockItemService, mockProductService, mockDialogStage, null);
    }

    @Test
    void initialize_setsUpFormattersAndOrientation() {
        verify(quantityOnHandField).setTextFormatter(any(TextFormatter.class));
        // ... verify other formatted fields ...
        verify(inventoryItemFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void initializeDialog_addMode_setsTitleAndDefaults() {
        controller.initializeDialog(mockItemService, mockProductService, mockDialogStage, null);
        verify(dialogTitleLabel).setText(MessageProvider.getString("inventoryitem.dialog.add.title"));
        verify(productComboBox).setDisable(false);
        verify(activeCheckBox).setSelected(true); // From initialize()
        verify(productComboBox).setItems(any(ObservableList.class));
    }

    @Test
    void initializeDialog_editMode_populatesFields() {
        InventoryItemDTO item = new InventoryItemDTO();
        item.setInventoryItemId(1L);
        item.setProductId(sampleProduct.getProductId());
        item.setItemSpecificNameEn("Specific Name");
        item.setSellingPrice(new BigDecimal("19.99"));
        item.setQuantityOnHand(10);
        item.setMinStockLevel(5);
        item.setActive(true);

        when(productComboBox.getValue()).thenReturn(sampleProduct); // Ensure product is "selected"

        controller.initializeDialog(mockItemService, mockProductService, mockDialogStage, item);

        verify(dialogTitleLabel).setText(MessageProvider.getString("inventoryitem.dialog.edit.title"));
        verify(productComboBox).setValue(sampleProduct);
        verify(productComboBox).setDisable(true);
        verify(specificNameEnField).setText("Specific Name");
        verify(sellingPriceField).setText("19.99");
        verify(quantityOnHandField).setText("10");
    }

    @Test
    void handleSaveButtonAction_addMode_validInput_savesItem() throws Exception {
        controller.initializeDialog(mockItemService, mockProductService, mockDialogStage, null);
        when(productComboBox.getValue()).thenReturn(sampleProduct);
        when(specificNameEnField.getText()).thenReturn("New Item EN");
        when(specificNameArField.getText()).thenReturn("عنصر جديد");
        when(unitOfMeasureField.getText()).thenReturn("PCS");
        when(quantityOnHandField.getText()).thenReturn("50");
        when(sellingPriceField.getText()).thenReturn("12.50");
        when(minStockLevelField.getText()).thenReturn("5");
        when(activeCheckBox.isSelected()).thenReturn(true);
        when(attributesArea.getText()).thenReturn("{\"color\":\"blue\"}");

        InventoryItemDTO savedDto = new InventoryItemDTO();
        when(mockItemService.saveInventoryItem(any(InventoryItemDTO.class))).thenReturn(savedDto);

        controller.handleSaveButtonAction(null);

        assertTrue(controller.isSaved());
        assertEquals(savedDto, controller.getSavedItem());
        verify(mockDialogStage).close();
        ArgumentCaptor<InventoryItemDTO> captor = ArgumentCaptor.forClass(InventoryItemDTO.class);
        verify(mockItemService).saveInventoryItem(captor.capture());
        assertEquals(sampleProduct.getProductId(), captor.getValue().getProductId());
        assertEquals("New Item EN", captor.getValue().getItemSpecificNameEn());
    }

    @Test
    void handleSaveButtonAction_invalidJsonAttribute_showsError() {
        controller.initializeDialog(mockItemService, mockProductService, mockDialogStage, null);
        when(productComboBox.getValue()).thenReturn(sampleProduct);
        when(specificNameEnField.getText()).thenReturn("Item");
        when(unitOfMeasureField.getText()).thenReturn("Unit");
        when(quantityOnHandField.getText()).thenReturn("10");
        when(sellingPriceField.getText()).thenReturn("10");
        when(minStockLevelField.getText()).thenReturn("1");
        when(attributesArea.getText()).thenReturn("{\"color:blue}"); // Invalid JSON

        when(productComboBox.getScene()).thenReturn(mock(javafx.scene.Scene.class)); // For showErrorAlert
        when(productComboBox.getScene().getWindow()).thenReturn(mockDialogStage);


        controller.handleSaveButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initialize(); // This is called by FXML loader, then initializeDialog by parent
        controller.initializeDialog(mockItemService, mockProductService, mockDialogStage, null); // Simulate full init

        verify(inventoryItemFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
