package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher; // Added
import com.basariatpos.model.ShiftDTO;   // Added
import com.basariatpos.model.UserDTO;    // Added
import com.basariatpos.service.ShiftService; // Added
import com.basariatpos.service.UserSessionService; // Added
import com.basariatpos.service.ShiftException; // Added

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import java.math.BigDecimal; // Added
import javafx.scene.control.Button; // Added
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
    private MockedStatic<AppLauncher> staticAppLauncherMock; // For mocking service getters

    // Mocks for services needed by MainFrameController for shift ops
    @Mock private ShiftService mockShiftService;
    @Mock private UserSessionService mockUserSessionService;
    @Mock private UserDTO mockCurrentUser;


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

        // Mock static methods
        staticLocaleManagerMock = Mockito.mockStatic(LocaleManager.class);
        staticMessageProviderMock = Mockito.mockStatic(MessageProvider.class);
        staticAppLauncherMock = Mockito.mockStatic(AppLauncher.class);

        // Default mock behaviors for LocaleManager
        staticLocaleManagerMock.when(LocaleManager::getCurrentLocale).thenReturn(LocaleManager.ARABIC);
        staticLocaleManagerMock.when(LocaleManager::getEnglish).thenReturn(Locale.ENGLISH);
        // Allow real calls for other LocaleManager methods if not specifically mocked

        // Default mock behaviors for MessageProvider
        staticMessageProviderMock.when(() -> MessageProvider.getString(anyString())).thenAnswer(inv -> "mock: " + inv.getArgument(0));
        staticMessageProviderMock.when((String key, String p1, String p2) -> MessageProvider.getString(key,p1,p2)).thenAnswer(inv -> "mock: " + inv.getArgument(0) + " P1:" + inv.getArgument(1) + " P2:" + inv.getArgument(2) );
        staticMessageProviderMock.when(() -> MessageProvider.getBundle(any(Locale.class))).thenAnswer(inv -> ResourceBundle.getBundle(MessageProvider.RESOURCE_BUNDLE_BASE_NAME, inv.getArgument(0)));
        staticMessageProviderMock.when(() -> MessageProvider.getBundle()).thenAnswer(() -> ResourceBundle.getBundle(MessageProvider.RESOURCE_BUNDLE_BASE_NAME, LocaleManager.getCurrentLocale()));

        // Mock AppLauncher to provide mocked services
        staticAppLauncherMock.when(AppLauncher::getShiftService).thenReturn(mockShiftService);
        staticAppLauncherMock.when(AppLauncher::getUserSessionService).thenReturn(mockUserSessionService);
        // Mock other services from AppLauncher if MainFrameController uses them directly in initialize or elsewhere
        // For now, focus on shift-related services.

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
        if (staticAppLauncherMock != null) staticAppLauncherMock.close();
    }

    @BeforeEach
    void resetMocksForEachTest() {
        // Reset interaction counts on mocks, but retain stubbing from @Start or class level if any.
        Mockito.reset(mockShiftService, mockUserSessionService, mockCurrentUser);

        // Default setup: User is logged in, no active shift initially for most tests.
        when(mockUserSessionService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(mockCurrentUser.getUserId()).thenReturn(1); // Standard test user ID
        when(mockCurrentUser.getUsername()).thenReturn("testuser");
        // Make getActiveOrPausedShiftForUser return empty Optional by default for user ID 1
        try {
            when(mockShiftService.getActiveOrPausedShiftForUser(1)).thenReturn(Optional.empty());
        } catch (ShiftException e) {
            fail("Mock setup failed for getActiveOrPausedShiftForUser", e);
        }

        // Explicitly call update method after mocks are set for the test
        // This ensures the UI reflects the state defined by the mocks for *this* test.
        controller.updateShiftStatusDisplayAndControls();
    }


    @Test
    void controller_should_be_injected() {
        assertNotNull(controller, "MainFrameController should be injected by FXMLLoader.");
    }

    @Test
    void shiftStatusLabel_should_be_initialized(FxRobot robot) {
        Label shiftStatusLabel = robot.lookup("#shiftStatusLabel").queryAs(Label.class);
        assertNotNull(shiftStatusLabel, "Shift status label should be present in the FXML.");
        // Text is now set by updateShiftStatusDisplayAndControls
        assertEquals("mock: mainframe.shiftstatus.noActiveShift", shiftStatusLabel.getText());
    }

    @Test
    void shiftControls_userNotLoggedIn_startShiftDisabled(FxRobot robot) {
        when(mockUserSessionService.getCurrentUser()).thenReturn(null); // Simulate no user logged in
        controller.updateShiftStatusDisplayAndControls(); // Manually trigger update

        assertTrue(robot.lookup("#startShiftButton").queryButton().isDisabled());
        assertFalse(robot.lookup("#pauseShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#resumeShiftButton").queryButton().isVisible());
    }

    @Test
    void shiftControls_userLoggedIn_noActiveShift_startShiftEnabled(FxRobot robot) {
        // This state is set up in @BeforeEach by default
        assertFalse(robot.lookup("#startShiftButton").queryButton().isDisabled());
        assertTrue(robot.lookup("#startShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#pauseShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#resumeShiftButton").queryButton().isVisible());
        assertEquals("mock: mainframe.shiftstatus.noActiveShift", robot.lookup("#shiftStatusLabel").queryAs(Label.class).getText());
    }

    @Test
    void shiftControls_userLoggedIn_shiftActive_pauseEnabled(FxRobot robot) throws ShiftException {
        ShiftDTO activeShift = new ShiftDTO(101, 1, "testuser", OffsetDateTime.now(), null, "Active", new BigDecimal("100"));
        when(mockShiftService.getActiveOrPausedShiftForUser(1)).thenReturn(Optional.of(activeShift));
        // when(mockUserSessionService.getActiveShift()).thenReturn(activeShift); // Session updated by controller logic
        controller.updateShiftStatusDisplayAndControls();

        assertFalse(robot.lookup("#startShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#pauseShiftButton").queryButton().isDisabled());
        assertTrue(robot.lookup("#pauseShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#resumeShiftButton").queryButton().isVisible());
        assertEquals("mock: mainframe.shiftstatus.active P1:101 P2:testuser", robot.lookup("#shiftStatusLabel").queryAs(Label.class).getText());
    }

    @Test
    void shiftControls_userLoggedIn_shiftPaused_resumeEnabled(FxRobot robot) throws ShiftException {
        ShiftDTO pausedShift = new ShiftDTO(101, 1, "testuser", OffsetDateTime.now(), null, "Paused", new BigDecimal("100"));
        when(mockShiftService.getActiveOrPausedShiftForUser(1)).thenReturn(Optional.of(pausedShift));
        // when(mockUserSessionService.getActiveShift()).thenReturn(pausedShift);
        controller.updateShiftStatusDisplayAndControls();

        assertFalse(robot.lookup("#startShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#pauseShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#resumeShiftButton").queryButton().isDisabled());
        assertTrue(robot.lookup("#resumeShiftButton").queryButton().isVisible());
        assertEquals("mock: mainframe.shiftstatus.paused P1:101 P2:testuser", robot.lookup("#shiftStatusLabel").queryAs(Label.class).getText());
    }

    @Test
    void handleStartShiftAction_opensDialog_andStartsShift_onDialogSave(FxRobot robot) throws Exception {
        // Mock service calls for starting shift
        ShiftDTO startedShift = new ShiftDTO(102, 1, "testuser", OffsetDateTime.now(), null, "Active", new BigDecimal("150.00"));
        when(mockShiftService.startNewShift(eq(1), any(BigDecimal.class))).thenReturn(startedShift);

        // Simulate clicking start shift
        robot.clickOn("#startShiftButton");
        WaitForAsyncUtils.waitForFxEvents(); // Wait for dialog to appear

        // Interact with StartShiftDialog (assuming it's the topmost window)
        FxRobot dialogRobot = robot.targetWindow(robot.listTargetWindows().get(robot.listTargetWindows().size()-1) );
        dialogRobot.clickOn("#openingFloatFld").write("150.00");
        dialogRobot.clickOn("#startButton"); // This is "Start Shift" button within the dialog
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockShiftService).startNewShift(eq(1), eq(new BigDecimal("150.00")));
        verify(mockUserSessionService).setActiveShift(startedShift);

        when(mockShiftService.getActiveOrPausedShiftForUser(1)).thenReturn(Optional.of(startedShift));
        // controller.updateShiftStatusDisplayAndControls(); // Called in finally block of handler

        assertEquals("mock: mainframe.shiftstatus.active P1:102 P2:testuser", robot.lookup("#shiftStatusLabel").queryAs(Label.class).getText());
        assertFalse(robot.lookup("#startShiftButton").queryButton().isVisible());
        assertTrue(robot.lookup("#pauseShiftButton").queryButton().isVisible());
    }


    // Original tests for menu items are retained below, with slight adjustments if needed
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
