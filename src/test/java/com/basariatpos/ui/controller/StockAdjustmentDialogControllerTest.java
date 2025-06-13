package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.service.InventoryItemService;
import com.basariatpos.service.exception.InventoryItemNotFoundException;
import com.basariatpos.service.exception.InventoryItemValidationException;
import com.basariatpos.service.exception.InventoryItemServiceException;

import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class StockAdjustmentDialogControllerTest {

    @Mock
    private InventoryItemService mockItemService;

    private StockAdjustmentDialogController controller;
    private Stage dialogStage;

    private InventoryItemDTO item1;
    private InventoryItemDTO item2;

    @BeforeAll
    static void setUpClass() {
        // Ensure JavaFX Platform is running for MessageProvider if it relies on it implicitly
        // For pure Java tests, this might not be needed if MessageProvider is FX-agnostic
        // However, TestFX tests will run on FX thread.
        try {
            FxToolkit.registerPrimaryStage(); // Ensures FX platform is initialized for TestFX
        } catch (Exception e) {
            // Ignore if already initialized or other issues, tests might still run
            System.err.println("FxToolkit registration failed: " + e.getMessage());
        }
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        MessageProvider.loadBundle(LocaleManager.getCurrentLocale());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (dialogStage != null && dialogStage.isShowing()) {
            org.testfx.api.FxToolkit.cleanupStages();
        }
    }

    @Start
    private void start(Stage stage) throws IOException, InventoryItemServiceException {
        MockitoAnnotations.openMocks(this);

        item1 = new InventoryItemDTO();
        item1.setInventoryItemId(1);
        item1.setProductNameEn("Product A");
        item1.setItemSpecificNameEn("Variant Red");
        item1.setQuantityOnHand(100);
        item1.setActive(true);
        item1.setIsStockItem(true);


        item2 = new InventoryItemDTO();
        item2.setInventoryItemId(2);
        item2.setProductNameEn("Product B");
        item2.setItemSpecificNameEn("Variant Blue");
        item2.setQuantityOnHand(50);
        item2.setActive(true);
        item2.setIsStockItem(true);

        // Setup mock before loader.load() if controller's initialize calls service
        List<InventoryItemDTO> initialItems = new ArrayList<>(Arrays.asList(item1, item2));
        when(mockItemService.getAllInventoryItems(false)).thenReturn(initialItems);


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/StockAdjustmentDialog.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();
        controller = loader.getController();

        this.dialogStage = stage;
        controller.initializeDialog(mockItemService, dialogStage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void initializeDialog_populatesItemAndReasonComboBoxes(FxRobot robot) {
        // Verification after @Start
        ComboBox<InventoryItemDTO> itemComboBox = robot.lookup("#itemComboBox").queryComboBox();
        assertNotNull(itemComboBox.getItems());
        assertEquals(2, itemComboBox.getItems().size()); // item1 and item2 from mock in @Start
        assertEquals(item1.getDisplayFullNameEn(), itemComboBox.getItems().get(0).getDisplayFullNameEn());

        ComboBox<String> reasonComboBox = robot.lookup("#reasonComboBox").queryComboBox();
        assertNotNull(reasonComboBox.getItems());
        assertTrue(reasonComboBox.getItems().size() >= 6); // 5 specific + "Other"
        assertTrue(reasonComboBox.getItems().contains(MessageProvider.getString("stockadjustment.reason.damaged")));
        assertTrue(reasonComboBox.getItems().contains(MessageProvider.getString("stockadjustment.reason.other")));
    }

    @Test
    void itemSelection_updatesCurrentQohField(FxRobot robot) {
        ComboBox<InventoryItemDTO> itemComboBox = robot.lookup("#itemComboBox").queryComboBox();
        TextField currentQohField = robot.lookup("#currentQohField").queryAs(TextField.class);

        robot.interact(() -> itemComboBox.getSelectionModel().select(item1));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(String.valueOf(item1.getQuantityOnHand()), currentQohField.getText());

        robot.interact(() -> itemComboBox.getSelectionModel().select(item2));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(String.valueOf(item2.getQuantityOnHand()), currentQohField.getText());
    }

    @Test
    void reasonSelection_other_showsOtherReasonArea(FxRobot robot) {
        ComboBox<String> reasonComboBox = robot.lookup("#reasonComboBox").queryComboBox();
        TextArea otherReasonArea = robot.lookup("#otherReasonArea").queryAs(TextArea.class);
        javafx.scene.control.Label otherReasonLabel = robot.lookup("#otherReasonLabel").queryAs(javafx.scene.control.Label.class);


        robot.interact(() -> reasonComboBox.getSelectionModel().select(MessageProvider.getString("stockadjustment.reason.other")));
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(otherReasonArea.isVisible() && otherReasonArea.isManaged());
        assertTrue(otherReasonLabel.isVisible() && otherReasonLabel.isManaged());


        robot.interact(() -> reasonComboBox.getSelectionModel().select(MessageProvider.getString("stockadjustment.reason.damaged")));
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(otherReasonArea.isVisible() && otherReasonArea.isManaged());
        assertFalse(otherReasonLabel.isVisible() && otherReasonLabel.isManaged());
    }

    @Test
    void handleSubmitAdjustment_validInput_callsServiceAndCloses(FxRobot robot) throws Exception {
        robot.interact(() -> robot.lookup("#itemComboBox").queryComboBox().getSelectionModel().select(item1));
        robot.clickOn("#adjustmentQuantityField").write("10");
        robot.interact(() -> robot.lookup("#reasonComboBox").queryComboBox().getSelectionModel().select(MessageProvider.getString("stockadjustment.reason.correction")));

        doNothing().when(mockItemService).performStockAdjustment(anyInt(), anyInt(), anyString());

        robot.clickOn("#submitButton");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockItemService).performStockAdjustment(eq(item1.getInventoryItemId()), eq(10), eq(MessageProvider.getString("stockadjustment.reason.correction")));
        assertFalse(dialogStage.isShowing());
        assertTrue(controller.isSaved());
    }

    @Test
    void handleSubmitAdjustment_itemNotSelected_showsValidationErrorAndStaysOpen(FxRobot robot) {
        robot.clickOn("#adjustmentQuantityField").write("10");
        robot.interact(() -> robot.lookup("#reasonComboBox").queryComboBox().getSelectionModel().select(MessageProvider.getString("stockadjustment.reason.correction")));

        // Mock Alert construction to verify it's shown
        try (var alertMock = mockConstruction(Alert.class, (mock, context) -> {
            // Optional: set title for mock if needed for assertions
            // when(mock.getTitle()).thenReturn(MessageProvider.getString("Validation Error"));
        })) {
            robot.clickOn("#submitButton");
            WaitForAsyncUtils.waitForFxEvents();

            // Verify an Alert was created and shown
            assertEquals(1, alertMock.constructed().size());
            Alert constructedAlert = alertMock.constructed().get(0);
            // verify(constructedAlert).showAndWait(); // This might be tricky if it actually waits in test
            assertEquals(Alert.AlertType.ERROR, constructedAlert.getAlertType());
            assertTrue(constructedAlert.getContentText().contains(MessageProvider.getString("stockadjustment.validation.itemRequired")));
        }

        assertTrue(dialogStage.isShowing()); // Dialog should remain open
        verify(mockItemService, never()).performStockAdjustment(anyInt(), anyInt(), anyString());
    }

    @Test
    void handleSubmitAdjustment_otherReasonSelected_otherReasonEmpty_showsValidationError(FxRobot robot) {
        robot.interact(() -> robot.lookup("#itemComboBox").queryComboBox().getSelectionModel().select(item1));
        robot.clickOn("#adjustmentQuantityField").write("5");
        robot.interact(() -> robot.lookup("#reasonComboBox").queryComboBox().getSelectionModel().select(MessageProvider.getString("stockadjustment.reason.other")));
        robot.lookup("#otherReasonArea").queryAs(TextArea.class).clear(); // Ensure it's empty

        try (var alertMock = mockConstruction(Alert.class)) {
            robot.clickOn("#submitButton");
            WaitForAsyncUtils.waitForFxEvents();

            assertEquals(1, alertMock.constructed().size());
            Alert alert = alertMock.constructed().get(0);
            assertEquals(Alert.AlertType.ERROR, alert.getAlertType());
            assertTrue(alert.getContentText().contains(MessageProvider.getString("stockadjustment.validation.otherReasonRequired")));
        }
        assertTrue(dialogStage.isShowing());
    }

    @Test
    void handleSubmitAdjustment_serviceThrowsValidationException_showsErrorAlert(FxRobot robot) throws Exception {
        robot.interact(() -> robot.lookup("#itemComboBox").queryComboBox().getSelectionModel().select(item1));
        robot.clickOn("#adjustmentQuantityField").write("-200"); // This should make QOH negative
        robot.interact(() -> robot.lookup("#reasonComboBox").queryComboBox().getSelectionModel().select(MessageProvider.getString("stockadjustment.reason.correction")));

        String validationErrorMsg = MessageProvider.getString("stockadjustment.validation.qohNegative");
        doThrow(new InventoryItemValidationException(validationErrorMsg, List.of(validationErrorMsg)))
            .when(mockItemService).performStockAdjustment(eq(item1.getInventoryItemId()), eq(-200), anyString());

        try (var alertMock = mockConstruction(Alert.class)) {
            robot.clickOn("#submitButton");
            WaitForAsyncUtils.waitForFxEvents();

            assertEquals(1, alertMock.constructed().size());
            Alert alert = alertMock.constructed().get(0);
            assertEquals(Alert.AlertType.ERROR, alert.getAlertType());
            assertTrue(alert.getContentText().contains(validationErrorMsg));
        }
        assertTrue(dialogStage.isShowing());
    }
}
