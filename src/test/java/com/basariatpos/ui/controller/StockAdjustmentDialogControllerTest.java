package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.service.InventoryItemService;
import com.basariatpos.service.exception.InventoryItemNotFoundException;
import com.basariatpos.service.exception.InventoryItemServiceException;
import com.basariatpos.service.exception.InventoryItemValidationException;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
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
import java.util.function.UnaryOperator; // Import for TextFormatter.Change

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockAdjustmentDialogControllerTest {

    @Mock private ComboBox<InventoryItemDTO> itemComboBox;
    @Mock private TextField currentQohField;
    @Mock private TextField adjustmentQuantityField;
    @Mock private ComboBox<String> reasonComboBox;
    @Mock private Label otherReasonLabel;
    @Mock private TextArea otherReasonArea;
    @Mock private VBox stockAdjustmentRootPane; // For RTL
    @Mock private Stage mockDialogStage;
    @Mock private InventoryItemService mockItemService;

    @InjectMocks
    private StockAdjustmentDialogController controller;

    private static ResourceBundle resourceBundle;
    private InventoryItemDTO sampleItem;

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
        controller.itemComboBox = itemComboBox;
        controller.currentQohField = currentQohField;
        controller.adjustmentQuantityField = adjustmentQuantityField;
        controller.reasonComboBox = reasonComboBox;
        controller.otherReasonLabel = otherReasonLabel;
        controller.otherReasonArea = otherReasonArea;
        controller.stockAdjustmentRootPane = stockAdjustmentRootPane;

        sampleItem = new InventoryItemDTO();
        sampleItem.setInventoryItemId(1L);
        sampleItem.setItemSpecificNameEn("Sample Item");
        sampleItem.setQuantityOnHand(100);
        sampleItem.setStockItem(true);
        sampleItem.setActive(true);

        try {
            // Ensure the mock returns a modifiable list if the controller tries to sort/filter it, though not the case here.
            when(mockItemService.getAllInventoryItems(false)).thenReturn(new java.util.ArrayList<>(List.of(sampleItem)));
        } catch (InventoryItemServiceException e) {
            fail("Mock setup failed: " + e.getMessage());
        }

        when(itemComboBox.getItems()).thenReturn(FXCollections.observableArrayList(sampleItem));
        when(reasonComboBox.getItems()).thenReturn(FXCollections.observableArrayList(
            controller.predefinedReasonKeys.stream().map(MessageProvider::getString).toList()
        ));

        // Mock selection models and properties for listeners
        SingleSelectionModel<InventoryItemDTO> itemSelectionModel = mock(SingleSelectionModel.class);
        when(itemComboBox.getSelectionModel()).thenReturn(itemSelectionModel);
        when(itemSelectionModel.selectedItemProperty()).thenReturn(mock(javafx.beans.property.ReadOnlyObjectProperty.class));

        SingleSelectionModel<String> reasonSelectionModel = mock(SingleSelectionModel.class);
        when(reasonComboBox.getSelectionModel()).thenReturn(reasonSelectionModel);
        when(reasonSelectionModel.selectedItemProperty()).thenReturn(mock(javafx.beans.property.ReadOnlyObjectProperty.class));

        when(otherReasonLabel.visibleProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(otherReasonLabel.managedProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(otherReasonArea.visibleProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(otherReasonArea.managedProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));

        // Mock textFormatter for adjustmentQuantityField as it's setup in initializeDialog
        // This avoids NPE if the field itself is not deeply mocked for setTextFormatter.
        // Alternatively, ensure the field is not null and can accept a formatter.
        // For simplicity, we just verify it's called.
        doNothing().when(adjustmentQuantityField).setTextFormatter(any(TextFormatter.class));


        controller.initializeDialog(mockItemService, mockDialogStage);
    }

    @Test
    void initializeDialog_loadsItemsAndReasons_setsOrientation() throws InventoryItemServiceException {
        verify(itemComboBox).setItems(any(ObservableList.class));
        verify(reasonComboBox).setItems(any(ObservableList.class));
        verify(adjustmentQuantityField).setTextFormatter(any(TextFormatter.class)); // Verifies applyIntegerFormatter was called
        verify(stockAdjustmentRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void handleSubmitAdjustmentButtonAction_validInput_performsAdjustment() throws InventoryItemServiceException {
        when(itemComboBox.getValue()).thenReturn(sampleItem);
        when(adjustmentQuantityField.getText()).thenReturn("-10");
        String validReason = MessageProvider.getString("stockadjustment.reason.damaged");
        when(reasonComboBox.getValue()).thenReturn(validReason);

        controller.handleSubmitAdjustmentButtonAction(null);

        assertTrue(controller.isSaved());
        verify(mockItemService).performStockAdjustment(sampleItem.getInventoryItemId(), -10, validReason);
        verify(mockDialogStage).close();
    }

    @Test
    void handleSubmitAdjustmentButtonAction_otherReason_usesTextArea() throws InventoryItemServiceException {
        when(itemComboBox.getValue()).thenReturn(sampleItem);
        when(adjustmentQuantityField.getText()).thenReturn("5");
        String otherReasonKey = MessageProvider.getString("stockadjustment.reason.other");
        when(reasonComboBox.getValue()).thenReturn(otherReasonKey);
        when(otherReasonArea.getText()).thenReturn("Special custom reason");

        controller.handleSubmitAdjustmentButtonAction(null);

        assertTrue(controller.isSaved());
        verify(mockItemService).performStockAdjustment(sampleItem.getInventoryItemId(), 5, "Special custom reason");
        verify(mockDialogStage).close();
    }

    @Test
    void handleSubmitAdjustmentButtonAction_noItemSelected_showsError() {
        when(itemComboBox.getValue()).thenReturn(null);
        when(itemComboBox.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(itemComboBox.getScene().getWindow()).thenReturn(mockDialogStage);

        controller.handleSubmitAdjustmentButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockItemService, never()).performStockAdjustment(anyLong(), anyInt(), anyString());
        verify(mockDialogStage, never()).close();
    }

    @Test
    void handleSubmitAdjustmentButtonAction_qohGoesNegative_showsError() {
        when(itemComboBox.getValue()).thenReturn(sampleItem); // QOH is 100
        when(adjustmentQuantityField.getText()).thenReturn("-101"); // Makes QOH -1
        when(reasonComboBox.getValue()).thenReturn(MessageProvider.getString("stockadjustment.reason.correction"));
        when(itemComboBox.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(itemComboBox.getScene().getWindow()).thenReturn(mockDialogStage);

        controller.handleSubmitAdjustmentButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
    }


    @Test
    void initializeDialog_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initializeDialog(mockItemService, mockDialogStage);

        verify(stockAdjustmentRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
