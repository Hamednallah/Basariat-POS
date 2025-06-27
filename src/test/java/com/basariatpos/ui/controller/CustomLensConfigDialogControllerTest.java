package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.SalesOrderItemDTO;
import com.basariatpos.ui.utilui.TextFormatters; // Assuming this is where formatters are

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage; // DialogPane does not use Stage directly in this controller

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomLensConfigDialogControllerTest {

    @Mock private DialogPane dialogPane;
    @Mock private TextField odSphField, odCylField, odAxisField, odAddField;
    @Mock private TextField osSphField, osCylField, osAxisField, osAddField;
    @Mock private TextField ipdField, unitPriceField;
    @Mock private ComboBox<String> materialComboBox, shadeComboBox, reflectionTypeComboBox;

    // Mock the OK button from DialogPane to simulate its action event
    @Mock private javafx.scene.control.Button mockOkButton;


    @InjectMocks
    private CustomLensConfigDialogController controller;

    private static ResourceBundle resourceBundle;
    private SalesOrderItemDTO testLensItem;
    private MockedStatic<TextFormatters> mockTextFormatters;


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
        controller.dialogPane = dialogPane;
        controller.odSphField = odSphField; controller.odCylField = odCylField; controller.odAxisField = odAxisField; controller.odAddField = odAddField;
        controller.osSphField = osSphField; controller.osCylField = osCylField; controller.osAxisField = osAxisField; controller.osAddField = osAddField;
        controller.ipdField = ipdField; controller.unitPriceField = unitPriceField;
        controller.materialComboBox = materialComboBox; controller.shadeComboBox = shadeComboBox; controller.reflectionTypeComboBox = reflectionTypeComboBox;

        // Mock ComboBox items for populateComboBoxes()
        when(materialComboBox.getItems()).thenReturn(FXCollections.observableArrayList());
        when(shadeComboBox.getItems()).thenReturn(FXCollections.observableArrayList());
        when(reflectionTypeComboBox.getItems()).thenReturn(FXCollections.observableArrayList());

        // Mock selectedProperty for materialComboBox listener
        // To properly mock this, you need to mock the whole selection model chain
        javafx.scene.control.SingleSelectionModel<String> mockSelectionModel = mock(javafx.scene.control.SingleSelectionModel.class);
        when(materialComboBox.getSelectionModel()).thenReturn(mockSelectionModel);
        javafx.beans.property.ReadOnlyObjectProperty<String> mockSelectedItemProperty = mock(javafx.beans.property.ReadOnlyObjectProperty.class);
        when(mockSelectionModel.selectedItemProperty()).thenReturn(mockSelectedItemProperty);


        // Mock TextFormatters static methods
        mockTextFormatters = Mockito.mockStatic(TextFormatters.class);
        mockTextFormatters.when(() -> TextFormatters.applyBigDecimalFormatter(anyVararg())).thenAnswer(invocation -> null);
        mockTextFormatters.when(() -> TextFormatters.applyIntegerFormatter(anyVararg())).thenAnswer(invocation -> null);

        // Mock parsing methods from TextFormatters for handleOkAction
        mockTextFormatters.when(() -> TextFormatters.parseBigDecimal(anyString())).thenAnswer(
            inv -> { String arg = inv.getArgument(0); if(arg == null || arg.isEmpty()) return null; try { return new BigDecimal(arg); } catch (Exception e) { return null; } }
        );
         mockTextFormatters.when(() -> TextFormatters.parseBigDecimal(anyString(), any(BigDecimal.class))).thenAnswer(
            inv -> { String arg = inv.getArgument(0); if(arg == null || arg.isEmpty()) return inv.getArgument(1); try { return new BigDecimal(arg); } catch (Exception e) { return inv.getArgument(1); } }
        );
        mockTextFormatters.when(() -> TextFormatters.parseInteger(anyString())).thenAnswer(
            inv -> { String arg = inv.getArgument(0); if(arg == null || arg.isEmpty()) return null; try { return Integer.parseInt(inv.getArgument(0)); } catch (Exception e) { return null; } }
        );

        // Mock lookupButton for OK button event filter
        when(dialogPane.lookupButton(ButtonType.OK)).thenReturn(mockOkButton);


        controller.initialize(); // Calls populateComboBoxes, addNumericFormatters, updateNodeOrientation

        testLensItem = new SalesOrderItemDTO(); // Fresh item for each test
    }

    @AfterEach
    void tearDown() {
        mockTextFormatters.close();
    }


    @Test
    void initialize_populatesComboBoxesAndSetsUpFormatters() {
        verify(materialComboBox).setItems(any(ObservableList.class));
        verify(shadeComboBox).setItems(any(ObservableList.class));
        verify(reflectionTypeComboBox).setItems(any(ObservableList.class));
        mockTextFormatters.verify(() -> TextFormatters.applyBigDecimalFormatter(anyVararg()), atLeastOnce());
        mockTextFormatters.verify(() -> TextFormatters.applyIntegerFormatter(anyVararg()), atLeastOnce());
        verify(dialogPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void initializeDialog_editMode_parsesAndSetsJsonDetails() {
        // Using Gson compatible JSON structure
        String json = "{\"rx\":{\"odSph\":\"-1.25\",\"odCyl\":\"-0.50\",\"odAxis\":90,\"osSph\":null,\"osCyl\":null,\"osAxis\":null,\"odAdd\":null,\"osAdd\":null,\"ipd\":null},\"attrs\":{\"material\":\"Glass\",\"shade\":\"White\",\"reflectionType\":\"AR\"}}";
        testLensItem.setPrescriptionDetails(json);
        testLensItem.setUnitPrice(new BigDecimal("150.00"));

        controller.initializeDialog(null, testLensItem); // Stage can be null for this test if not directly used by method

        verify(odSphField).setText("-1.25");
        verify(odCylField).setText("-0.50");
        verify(odAxisField).setText("90");
        verify(materialComboBox).setValue("Glass");
        verify(shadeComboBox).setValue("White");
        verify(reflectionTypeComboBox).setValue("AR");
        verify(unitPriceField).setText("150.00");
    }

    @Test
    void handleOkAction_validInputs_updatesLensItemAndCloses() {
        controller.initializeDialog(null, testLensItem);

        when(odSphField.getText()).thenReturn("-2.00");
        when(materialComboBox.getValue()).thenReturn(controller.MATERIAL_PLASTIC); // Use constant from controller
        when(reflectionTypeComboBox.getValue()).thenReturn(controller.REFLECTION_AR); // Use constant
        when(unitPriceField.getText()).thenReturn("120.00");

        ActionEvent mockEvent = mock(ActionEvent.class);
        controller.handleOkAction(mockEvent);

        assertTrue(controller.getUpdatedLensItem().isCustomLenses());
        assertNotNull(controller.getUpdatedLensItem().getPrescriptionDetails());
        assertTrue(controller.getUpdatedLensItem().getPrescriptionDetails().contains("\"odSph\":\"-2.00\""));
        assertTrue(controller.getUpdatedLensItem().getPrescriptionDetails().contains("\"material\":\"" + controller.MATERIAL_PLASTIC + "\""));
        assertEquals(0, new BigDecimal("120.00").compareTo(controller.getUpdatedLensItem().getUnitPrice()));
        verify(mockEvent, never()).consume();
    }

    @Test
    void handleOkAction_invalidPrice_consumesEventAndShowsError() {
        controller.initializeDialog(null, testLensItem);
        when(materialComboBox.getValue()).thenReturn(controller.MATERIAL_GLASS);
        when(unitPriceField.getText()).thenReturn("invalidPrice");

        when(dialogPane.getScene()).thenReturn(mock(javafx.scene.Scene.class)); // For AlertUtil
        when(dialogPane.getScene().getWindow()).thenReturn(mock(Stage.class));


        ActionEvent mockEvent = mock(ActionEvent.class);
        controller.handleOkAction(mockEvent);

        assertNull(controller.getUpdatedLensItem());
        verify(mockEvent).consume();
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initialize();
        controller.initializeDialog(null, testLensItem);

        verify(dialogPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
