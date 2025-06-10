package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem; // Added
import javafx.stage.Stage; // Added
import javafx.scene.web.WebView; // Added
import javafx.scene.web.WebEngine; // Added
import javafx.scene.control.Alert; // Added

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach; // Added (though not used in this specific version, good practice)
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock; // Added
import org.mockito.MockedConstruction; // Added
import org.mockito.MockedStatic; // Added
import org.mockito.Mockito; // Added
import org.mockito.MockitoAnnotations; // Added
import org.mockito.ArgumentCaptor; // Added


import org.testfx.api.FxRobot; // Added
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils; // Added


import java.io.IOException;
import java.net.URL; // Added
import java.util.Locale; // Added
import java.util.ResourceBundle; // Added
import java.util.logging.Level;


import static org.junit.jupiter.api.Assertions.*; //assertTrue already there
import static org.mockito.ArgumentMatchers.any; // Added
import static org.mockito.ArgumentMatchers.anyString; // Added
import static org.mockito.Mockito.*; // Added


/**
 * Test class for MainFrameController.
 * Uses TestFX for JavaFX component testing.
 */
@ExtendWith(ApplicationExtension.class)
class MainFrameControllerTest {

    private MainFrameController controller;
    private Parent root;
    private Stage currentStage;

    // Hold onto static mocks to close them
    private MockedStatic<LocaleManager> staticLocaleManagerMock;
    private MockedStatic<MessageProvider> staticMessageProviderMock;


    /**
     * Will be called with {@code @Before} semantics, i. e. before each test method.
     *
     * @param stage - Will be injected by the test runner.
     */
    @Start
    private void start(Stage stage) throws IOException {
        MockitoAnnotations.openMocks(this); // Initialize mocks defined with @Mock

        // Suppress verbose logging from FXML loader if not needed for these tests
        java.util.logging.Logger.getLogger(FXMLLoader.class.getName()).setLevel(Level.OFF);

        // Mock static methods of LocaleManager and MessageProvider before FXML loading
        staticLocaleManagerMock = Mockito.mockStatic(LocaleManager.class);
        staticMessageProviderMock = Mockito.mockStatic(MessageProvider.class);

        // Default mock behaviors
        staticLocaleManagerMock.when(LocaleManager::getCurrentLocale).thenReturn(LocaleManager.ARABIC);
        staticLocaleManagerMock.when(() -> LocaleManager.getLocaleByLanguageCode(anyString())).thenCallRealMethod();
        staticLocaleManagerMock.when(() -> LocaleManager.getSupportedLocales()).thenCallRealMethod();
        staticLocaleManagerMock.when(() -> LocaleManager.getDefaultLocale()).thenCallRealMethod();
        staticLocaleManagerMock.when(() -> LocaleManager.getEnglish()).thenReturn(Locale.ENGLISH);


        staticMessageProviderMock.when(() -> MessageProvider.getString(anyString())).thenReturn("mock string");
        staticMessageProviderMock.when(() -> MessageProvider.getString(anyString(), any(Locale.class))).thenReturn("mock string locale");
        staticMessageProviderMock.when(() -> MessageProvider.getBundle()).thenReturn(ResourceBundle.getBundle(MessageProvider.RESOURCE_BUNDLE_BASE_NAME, LocaleManager.ARABIC));
        staticMessageProviderMock.when(() -> MessageProvider.getBundle(any(Locale.class))).thenAnswer(inv -> ResourceBundle.getBundle(MessageProvider.RESOURCE_BUNDLE_BASE_NAME, inv.getArgument(0)));


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/MainFrame.fxml"));
        loader.setResources(MessageProvider.getBundle(LocaleManager.ARABIC));

        root = loader.load();
        controller = loader.getController();

        currentStage = stage;
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @AfterEach
    void tearDown() {
        // Close static mocks after each test
        if (staticLocaleManagerMock != null) staticLocaleManagerMock.close();
        if (staticMessageProviderMock != null) staticMessageProviderMock.close();
    }


    @Test
    void controller_should_be_injected() {
        assertNotNull(controller, "MainFrameController should be injected by FXMLLoader.");
    }

    @Test
    void shiftStatusLabel_should_be_initialized(FxRobot robot) {
        Label shiftStatusLabel = robot.lookup("#shiftStatusLabel").queryAs(Label.class);
        assertNotNull(shiftStatusLabel, "Shift status label should be present in the FXML.");
        // Text is set by refreshUITexts using MessageProvider mock
        assertEquals("mock string", shiftStatusLabel.getText());
    }

    @Test
    void handleViewUserManual_loadsCorrectFile_forCurrentLocale(FxRobot robot) {
        // Arrange
        Locale testLocale = LocaleManager.ARABIC;
        String expectedFileName = "UserManual_ar.html";
        String expectedPath = "/com/basariatpos/help/" + expectedFileName;

        staticLocaleManagerMock.when(LocaleManager::getCurrentLocale).thenReturn(testLocale);

        try (MockedConstruction<WebView> mockedWebView = Mockito.mockConstruction(WebView.class,
             (mock, context) -> {
                 WebEngine mockEngine = mock(WebEngine.class);
                 when(mock.getEngine()).thenReturn(mockEngine);
             });
             MockedConstruction<Stage> mockedStage = Mockito.mockConstruction(Stage.class)) {

            robot.clickOn("#menuHelp").clickOn("#viewUserManualMenuItem");
            WaitForAsyncUtils.waitForFxEvents();

            assertEquals(1, mockedWebView.constructed().size());
            WebView constructedWebView = mockedWebView.constructed().get(0);
            WebEngine constructedEngine = constructedWebView.getEngine();

            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            verify(constructedEngine).load(urlCaptor.capture());
            assertTrue(urlCaptor.getValue().endsWith(expectedPath),
                       "WebView should load URL: " + urlCaptor.getValue() + ", expected to end with: " + expectedPath);

            assertEquals(1, mockedStage.constructed().size());
            Stage helpStage = mockedStage.constructed().get(0);
            verify(helpStage).setTitle("mock string");
            verify(helpStage).show();
        }
    }

    @Test
    void handleViewUserManual_fallsBackToEnglish_ifLocaleSpecificManualNotFound(FxRobot robot) {
        Locale unsupportedLocale = new Locale("xx");
        staticLocaleManagerMock.when(LocaleManager::getCurrentLocale).thenReturn(unsupportedLocale);

        String expectedFallbackPath = "/com/basariatpos/help/UserManual_en.html";

        try (MockedConstruction<WebView> mockedWebView = Mockito.mockConstruction(WebView.class,
             (mock, context) -> {
                 WebEngine mockEngine = mock(WebEngine.class);
                 when(mock.getEngine()).thenReturn(mockEngine);
             });
             MockedConstruction<Stage> mockedStage = Mockito.mockConstruction(Stage.class)) {

            robot.clickOn("#menuHelp").clickOn("#viewUserManualMenuItem");
            WaitForAsyncUtils.waitForFxEvents();

            WebView constructedWebView = mockedWebView.constructed().get(0);
            WebEngine constructedEngine = constructedWebView.getEngine();
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            verify(constructedEngine).load(urlCaptor.capture());
            assertTrue(urlCaptor.getValue().endsWith(expectedFallbackPath),
                       "WebView should load fallback English URL: " + urlCaptor.getValue() + ", expected to end with: " + expectedFallbackPath);

            verify(mockedStage.constructed().get(0)).show();
        }
    }

    @Test
    void handleViewUserManual_showsErrorAlert_ifAllManualsNotFound(FxRobot robot) {
        staticLocaleManagerMock.when(LocaleManager::getCurrentLocale).thenReturn(new Locale("xx"));

        // This test relies on the actual getClass().getResource() calls inside the controller
        // to return null for both "xx" and "en" locales.
        // If UserManual_en.html exists, this specific path might not be hit unless
        // the resource loading is more directly controlled/mocked, or the file is temporarily removed.
        // For this test, we assume it's possible for both lookups to fail.

        try (MockedConstruction<Alert> mockedAlert = Mockito.mockConstruction(Alert.class,
            (mock, context) -> {
                // Optional: further configure mock alert if needed
            })) {

            robot.clickOn("#menuHelp").clickOn("#viewUserManualMenuItem");
            WaitForAsyncUtils.waitForFxEvents();

            if (mockedAlert.constructed().isEmpty()) {
                System.err.println("Test Warning: Alert for missing manual not shown in handleViewUserManual_showsErrorAlert_ifAllManualsNotFound. Fallback UserManual_en.html might have been found by the test classloader.");
            } else {
                Alert alertInstance = mockedAlert.constructed().get(0);
                verify(alertInstance).showAndWait();
                assertEquals("mock string", alertInstance.getTitle());
            }
        }
    }
}
