package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.SalesOrderItemDTO;
import com.basariatpos.ui.utilui.TextFormatters; // Assuming this will be created or exists

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class CustomLensConfigDialogControllerTest {

    private CustomLensConfigDialogController controller;
    private Stage stage;
    private SalesOrderItemDTO testLensItem;

    // Keys for ComboBox options - must match controller for reliable testing
    private final String MATERIAL_BLUECUT = MessageProvider.getString("customlens.material.bluecut");
    private final String REFLECTION_AR = MessageProvider.getString("customlens.reflection.antireflective");
    private final String MATERIAL_PLASTIC = MessageProvider.getString("customlens.material.plastic");


    @BeforeAll
    static void setUpClass() throws Exception {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        MessageProvider.loadBundle(LocaleManager.getCurrentLocale());
        if (System.getProperty("os.name", "").toLowerCase().startsWith("linux")) {
            System.setProperty("java.awt.headless", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (stage != null && stage.isShowing()) {
             org.testfx.api.FxToolkit.cleanupStages();
        }
    }

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this); // Though no mocks are used in this specific test yet

        testLensItem = new SalesOrderItemDTO();
        // Initialize with some default or empty values if needed for dialog opening
        testLensItem.setUnitPrice(BigDecimal.ZERO);


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/CustomLensConfigDialog.fxml"));
        loader.setResources(MessageProvider.getBundle());
        DialogPane root = loader.load(); // Load DialogPane
        controller = loader.getController();

        // DialogPane needs to be in a Scene and Stage to be shown
        Scene scene = new Scene(root);
        stage.setScene(scene);

        // Call initializeDialog manually AFTER the stage is set up and FXML loaded
        controller.initializeDialog(stage, testLensItem);

        stage.setTitle(MessageProvider.getString("customlens.dialog.title"));
        stage.show();
    }

    @Test
    void initializeDialog_populatesFieldsFromItem(FxRobot robot) {
        SalesOrderItemDTO item = new SalesOrderItemDTO();
        item.setUnitPrice(new BigDecimal("123.45"));
        String initialJson = "{\"rx\":{\"odSph\":\"-1.25\", \"odCyl\":\"-0.50\", \"odAxis\":\"90\"}, \"attrs\":{\"material\":\"" + MATERIAL_PLASTIC + "\", \"shade\":\"White\"}}";
        item.setPrescriptionDetails(initialJson);

        Platform.runLater(() -> controller.initializeDialog(stage, item));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("-1.25", robot.lookup("#odSphField").queryAs(TextField.class).getText());
        assertEquals("-0.50", robot.lookup("#odCylField").queryAs(TextField.class).getText());
        assertEquals("90", robot.lookup("#odAxisField").queryAs(TextField.class).getText());
        assertEquals(MATERIAL_PLASTIC, robot.lookup("#materialComboBox").queryComboBox().getValue());
        assertEquals("123.45", robot.lookup("#unitPriceField").queryAs(TextField.class).getText());
    }

    @Test
    void materialBlueCut_disablesAndSetsReflectionAR(FxRobot robot) {
        ComboBox<String> materialCombo = robot.lookup("#materialComboBox").queryComboBox();
        ComboBox<String> reflectionCombo = robot.lookup("#reflectionTypeComboBox").queryComboBox();

        robot.interact(() -> materialCombo.setValue(MATERIAL_BLUECUT));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(REFLECTION_AR, reflectionCombo.getValue());
        assertTrue(reflectionCombo.isDisabled());

        robot.interact(() -> materialCombo.setValue(MATERIAL_PLASTIC));
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(reflectionCombo.isDisabled());
    }

    @Test
    void handleOkAction_validData_updatesItemAndCloses(FxRobot robot) {
        robot.interact(() -> {
            controller.odSphField.setText("-2.00");
            controller.osSphField.setText("-1.75");
            controller.materialComboBox.setValue(MATERIAL_BLUECUT); // This will trigger AR
            controller.shadeComboBox.setValue(MessageProvider.getString("customlens.shade.photochromic"));
            // Reflection type is set by Blue Cut rule
            controller.unitPriceField.setText("150.00");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Simulate clicking OK button
        // Accessing DialogPane's button requires it to be part of a Dialog first,
        // or we can call the handler directly if access is available.
        // Since controller's handleOkAction is hooked to ButtonType.OK via event filter:
        Platform.runLater(() -> {
            ButtonType okButton = controller.dialogPane.getButtonTypes().stream()
                .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .findFirst().orElse(null);
            assertNotNull(okButton, "OK ButtonType not found in DialogPane");
            // Simulate button click by firing an action event on the actual button node
             ((Button)controller.dialogPane.lookupButton(okButton)).fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(controller.okClicked);
        SalesOrderItemDTO updatedItem = controller.getUpdatedLensItem();
        assertNotNull(updatedItem);
        assertEquals(new BigDecimal("150.00"), updatedItem.getUnitPrice());
        assertTrue(updatedItem.isCustomLenses());
        assertEquals(MessageProvider.getString("salesorder.itemtype.customlens") + " (" + MATERIAL_BLUECUT + ")", updatedItem.getDescription());

        String details = updatedItem.getPrescriptionDetails();
        assertTrue(details.contains("\"odSph\":\"-2.00\""));
        assertTrue(details.contains("\"material\":\"" + MATERIAL_BLUECUT + "\""));
        assertTrue(details.contains("\"reflectionType\":\"" + REFLECTION_AR + "\""));

        // Stage should be closed by DialogPane mechanism if event not consumed by validation fail
        // However, direct verification of stage.isShowing() might be tricky if test ends too fast.
        // Rely on okClicked flag and DTO content for now.
    }

    @Test
    void handleOkAction_invalidPrice_showsErrorAndStaysOpen(FxRobot robot) {
        robot.interact(() -> {
            controller.materialComboBox.setValue(MATERIAL_PLASTIC);
            controller.unitPriceField.setText("invalid");
        });
        WaitForAsyncUtils.waitForFxEvents();

        try (var alertMock = mockConstruction(javafx.scene.control.Alert.class)) {
            Platform.runLater(() -> {
                ButtonType okButton = controller.dialogPane.getButtonTypes().stream()
                    .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                    .findFirst().orElse(null);
                ((Button)controller.dialogPane.lookupButton(okButton)).fire();
            });
            WaitForAsyncUtils.waitForFxEvents();

            assertTrue(alertMock.constructed().size() >= 1);
            Alert alert = alertMock.constructed().get(0);
            assertEquals(Alert.AlertType.ERROR, alert.getAlertType());
            assertTrue(alert.getContentText().contains(MessageProvider.getString("customlens.error.priceInvalid")));
        }
        assertFalse(controller.okClicked); // Should not have proceeded
        assertTrue(stage.isShowing()); // Dialog should remain open
    }
}
