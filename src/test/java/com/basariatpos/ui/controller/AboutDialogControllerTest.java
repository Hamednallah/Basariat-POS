package com.basariatpos.ui.controller;

import com.basariatpos.config.AppConfigLoader;
import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testfx.matcher.control.LabeledMatchers.hasText;


@ExtendWith(ApplicationExtension.class)
class AboutDialogControllerTest {

    @Mock
    private AppConfigLoader mockAppConfigLoader;

    private AboutDialogController controller; // Controller instance
    private Parent root;
    private Stage stage;

    // For verifying text content
    private final String APP_NAME_TEXT_ID = "#appNameText";
    private final String APP_DESCRIPTION_TEXT_ID = "#appDescriptionText";
    private final String VERSION_TEXT_ID = "#versionText";
    private final String DEVELOPER_EN_TEXT_ID = "#developerNameEnText";
    private final String DEVELOPER_AR_TEXT_ID = "#developerNameArText";
    private final String CONTACT_DETAILS_TEXT_ID = "#contactDetailsText";
    private final String COPYRIGHT_TEXT_ID = "#copyrightText";
    private final String CLOSE_BUTTON_ID = "#closeButton";


    @Start
    private void start(Stage stage) throws IOException {
        MockitoAnnotations.openMocks(this); // Initialize mocks for this test instance

        // Set default locale for MessageProvider consistency in tests
        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE); // Assuming Arabic is default
        ResourceBundle bundle = MessageProvider.getBundle();

        // Configure mock AppConfigLoader before FXML loading
        when(mockAppConfigLoader.getProperty(eq("app.version"), anyString())).thenReturn("1.0.0-TEST");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/AboutDialog.fxml"));
        loader.setResources(bundle);

        loader.setControllerFactory(param -> {
            AboutDialogController aboutController = new AboutDialogController();
            // Manually inject mock AppConfigLoader
            try {
                java.lang.reflect.Field configLoaderField = AboutDialogController.class.getDeclaredField("appConfigLoader");
                configLoaderField.setAccessible(true);
                configLoaderField.set(aboutController, mockAppConfigLoader);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Failed to inject mock AppConfigLoader into AboutDialogController via reflection.", e);
            }
            this.controller = aboutController;
            return aboutController;
        });

        root = loader.load();
        this.stage = stage;
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(MessageProvider.getString("about.dialog.title")); // Set title as it would be in MainFrame
        stage.show();
        stage.toFront();
    }

    @Test
    void initialize_populatesLabelsCorrectly(FxRobot robot) {
        // Assert that labels are populated with expected text from MessageProvider and AppConfigLoader mock

        // App Name (Set by FXML %key, check if it's correct)
        Label appNameLabel = robot.lookup(APP_NAME_TEXT_ID).queryAs(Label.class);
        assertEquals(MessageProvider.getString("about.dialog.appNameText"), appNameLabel.getText());

        // App Description
        Label appDescriptionLabel = robot.lookup(APP_DESCRIPTION_TEXT_ID).queryAs(Label.class);
        assertEquals(MessageProvider.getString("app.description"), appDescriptionLabel.getText());

        // Version (from mocked AppConfigLoader)
        Label versionLabel = robot.lookup(VERSION_TEXT_ID).queryAs(Label.class);
        assertEquals("1.0.0-TEST", versionLabel.getText());

        // Developer Name EN
        Label devEnLabel = robot.lookup(DEVELOPER_EN_TEXT_ID).queryAs(Label.class);
        assertEquals(MessageProvider.getString("about.dialog.developerNameEn"), devEnLabel.getText());

        // Developer Name AR
        Label devArLabel = robot.lookup(DEVELOPER_AR_TEXT_ID).queryAs(Label.class);
        assertEquals(MessageProvider.getString("about.dialog.developerNameAr"), devArLabel.getText());

        // Contact Details
        Label contactLabel = robot.lookup(CONTACT_DETAILS_TEXT_ID).queryAs(Label.class);
        assertEquals(MessageProvider.getString("about.dialog.contactDetails"), contactLabel.getText());

        // Copyright
        Label copyrightLabel = robot.lookup(COPYRIGHT_TEXT_ID).queryAs(Label.class);
        assertEquals(MessageProvider.getString("about.dialog.copyrightText"), copyrightLabel.getText());

        // Close button text (Set by FXML %key)
        Button closeButton = robot.lookup(CLOSE_BUTTON_ID).queryAs(Button.class);
        assertEquals(MessageProvider.getString("button.close"), closeButton.getText());
    }

    @Test
    void handleClose_closesTheDialog(FxRobot robot) {
        // Pre-condition: Stage is showing
        assertTrue(stage.isShowing());

        // Act
        robot.clickOn(CLOSE_BUTTON_ID);
        WaitForAsyncUtils.waitForFxEvents();

        // Assert: Stage should be closed
        assertFalse(stage.isShowing());
    }

    @Test
    void switchLanguageAndReopenDialog_showsTranslatedContent(FxRobot robot) throws IOException {
        // 1. Close the initial dialog (opened in Arabic by default)
        robot.clickOn(CLOSE_BUTTON_ID);
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(stage.isShowing());

        // 2. Change locale to English
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        ResourceBundle englishBundle = MessageProvider.getBundle(Locale.ENGLISH);

        // Reconfigure mock for English version if needed (app.version is locale-agnostic from config)
        // when(mockAppConfigLoader.getProperty(eq("app.version"), anyString())).thenReturn("1.0.0-TEST-EN");


        // 3. Re-open the dialog using FXMLLoader (simulating MainFrameController's action)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/AboutDialog.fxml"));
        loader.setResources(englishBundle);
        loader.setControllerFactory(param -> { // Re-inject mocks for the new controller instance
            AboutDialogController newController = new AboutDialogController();
            try {
                java.lang.reflect.Field configLoaderField = AboutDialogController.class.getDeclaredField("appConfigLoader");
                configLoaderField.setAccessible(true);
                configLoaderField.set(newController, mockAppConfigLoader);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Failed to re-inject mock AppConfigLoader.", e);
            }
            return newController;
        });

        Parent newRoot = loader.load();
        Scene newScene = new Scene(newRoot);
        stage.setScene(newScene); // Reuse the same stage
        stage.setTitle(MessageProvider.getString("about.dialog.title")); // Update title for new locale
        stage.show();
        stage.toFront();
        WaitForAsyncUtils.waitForFxEvents();

        // 4. Assert content is in English
        assertTrue(stage.isShowing());
        Label appNameLabel = robot.lookup(APP_NAME_TEXT_ID).queryAs(Label.class);
        assertEquals(englishBundle.getString("about.dialog.appNameText"), appNameLabel.getText());

        Label versionLabel = robot.lookup(VERSION_TEXT_ID).queryAs(Label.class);
        assertEquals("1.0.0-TEST", versionLabel.getText()); // Version is from config, should be same

        Button closeButton = robot.lookup(CLOSE_BUTTON_ID).queryAs(Button.class);
        assertEquals(englishBundle.getString("button.close"), closeButton.getText());

        // Close it again
        robot.clickOn(CLOSE_BUTTON_ID);
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(stage.isShowing());
    }
}
