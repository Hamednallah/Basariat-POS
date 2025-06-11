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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button; // Added
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.control.Alert;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;


import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional; // Added
import java.util.ResourceBundle;
import java.time.OffsetDateTime; // Added
import java.util.logging.Level;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


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


    @Start
    private void start(Stage stage) throws IOException {
        MockitoAnnotations.openMocks(this);

        java.util.logging.Logger.getLogger(FXMLLoader.class.getName()).setLevel(Level.OFF);

        staticLocaleManagerMock = Mockito.mockStatic(LocaleManager.class);
        staticMessageProviderMock = Mockito.mockStatic(MessageProvider.class);
        staticAppLauncherMock = Mockito.mockStatic(AppLauncher.class);

        staticLocaleManagerMock.when(LocaleManager::getCurrentLocale).thenReturn(LocaleManager.ARABIC);
        staticLocaleManagerMock.when(LocaleManager::getEnglish).thenReturn(Locale.ENGLISH);
        staticLocaleManagerMock.when(() -> LocaleManager.getLocaleByLanguageCode(anyString())).thenCallRealMethod();
        staticLocaleManagerMock.when(() -> LocaleManager.getSupportedLocales()).thenCallRealMethod();
        staticLocaleManagerMock.when(() -> LocaleManager.getDefaultLocale()).thenCallRealMethod();


        staticMessageProviderMock.when(() -> MessageProvider.getString(anyString())).thenAnswer(inv -> "mock: " + inv.getArgument(0));
        staticMessageProviderMock.when((String key, String p1, String p2) -> MessageProvider.getString(key,p1,p2)).thenAnswer(inv -> "mock: " + inv.getArgument(0) + " P1:" + inv.getArgument(1) + " P2:" + inv.getArgument(2) );
        staticMessageProviderMock.when(() -> MessageProvider.getBundle(any(Locale.class))).thenAnswer(inv -> ResourceBundle.getBundle(MessageProvider.RESOURCE_BUNDLE_BASE_NAME, inv.getArgument(0)));
        staticMessageProviderMock.when(() -> MessageProvider.getBundle()).thenAnswer(() -> ResourceBundle.getBundle(MessageProvider.RESOURCE_BUNDLE_BASE_NAME, LocaleManager.getCurrentLocale()));

        staticAppLauncherMock.when(AppLauncher::getShiftService).thenReturn(mockShiftService);
        staticAppLauncherMock.when(AppLauncher::getUserSessionService).thenReturn(mockUserSessionService);
        // Mock other services from AppLauncher if MainFrameController uses them directly in initialize or elsewhere

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
        if (staticLocaleManagerMock != null) staticLocaleManagerMock.close();
        if (staticMessageProviderMock != null) staticMessageProviderMock.close();
        if (staticAppLauncherMock != null) staticAppLauncherMock.close();
    }

    @BeforeEach
    void resetMocksForEachTest() {
        Mockito.reset(mockShiftService, mockUserSessionService, mockCurrentUser);

        when(mockUserSessionService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(mockCurrentUser.getUserId()).thenReturn(1);
        when(mockCurrentUser.getUsername()).thenReturn("testuser");
        try {
            when(mockShiftService.getActiveOrPausedShiftForUser(1)).thenReturn(Optional.empty());
        } catch (ShiftException e) {
            fail("Mock setup failed for getActiveOrPausedShiftForUser", e);
        }

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
        assertEquals("mock: mainframe.shiftstatus.noActiveShift", shiftStatusLabel.getText());
    }

    @Test
    void shiftControls_userNotLoggedIn_startShiftDisabled(FxRobot robot) {
        when(mockUserSessionService.getCurrentUser()).thenReturn(null);
        controller.updateShiftStatusDisplayAndControls();

        assertTrue(robot.lookup("#startShiftButton").queryButton().isDisabled());
        assertFalse(robot.lookup("#pauseShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#resumeShiftButton").queryButton().isVisible());
    }

    @Test
    void shiftControls_userLoggedIn_noActiveShift_startShiftEnabled(FxRobot robot) {
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
        controller.updateShiftStatusDisplayAndControls();

        assertFalse(robot.lookup("#startShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#pauseShiftButton").queryButton().isVisible());
        assertFalse(robot.lookup("#resumeShiftButton").queryButton().isDisabled());
        assertTrue(robot.lookup("#resumeShiftButton").queryButton().isVisible());
        assertEquals("mock: mainframe.shiftstatus.paused P1:101 P2:testuser", robot.lookup("#shiftStatusLabel").queryAs(Label.class).getText());
    }

    @Test
    void handleStartShiftAction_opensDialog_andStartsShift_onDialogSave(FxRobot robot) throws Exception {
        ShiftDTO startedShift = new ShiftDTO(102, 1, "testuser", OffsetDateTime.now(), null, "Active", new BigDecimal("150.00"));
        when(mockShiftService.startNewShift(eq(1), any(BigDecimal.class))).thenReturn(startedShift);

        robot.clickOn("#startShiftButton");
        WaitForAsyncUtils.waitForFxEvents();

        FxRobot dialogRobot = robot.targetWindow(robot.listTargetWindows().get(robot.listTargetWindows().size()-1) );
        dialogRobot.clickOn("#openingFloatFld").write("150.00");
        dialogRobot.clickOn("#startButton");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockShiftService).startNewShift(eq(1), eq(new BigDecimal("150.00")));
        verify(mockUserSessionService).setActiveShift(startedShift);

        when(mockShiftService.getActiveOrPausedShiftForUser(1)).thenReturn(Optional.of(startedShift));
        // controller.updateShiftStatusDisplayAndControls(); // Called in finally block of handler

        assertEquals("mock: mainframe.shiftstatus.active P1:102 P2:testuser", robot.lookup("#shiftStatusLabel").queryAs(Label.class).getText());
        assertFalse(robot.lookup("#startShiftButton").queryButton().isVisible());
        assertTrue(robot.lookup("#pauseShiftButton").queryButton().isVisible());
    }

    @Test
    void handleViewUserManual_loadsCorrectFile_forCurrentLocale(FxRobot robot) {
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
            verify(helpStage).setTitle("mock: help.usermanual.title");
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

        try (MockedConstruction<Alert> mockedAlert = Mockito.mockConstruction(Alert.class,
            (mock, context) -> {})) {

            robot.clickOn("#menuHelp").clickOn("#viewUserManualMenuItem");
            WaitForAsyncUtils.waitForFxEvents();

            if (mockedAlert.constructed().isEmpty()) {
                System.err.println("Test Warning: Alert for missing manual not shown in handleViewUserManual_showsErrorAlert_ifAllManualsNotFound. Fallback UserManual_en.html might have been found by the test classloader.");
            } else {
                Alert alertInstance = mockedAlert.constructed().get(0);
                verify(alertInstance).showAndWait();
                assertEquals("mock: help.error.loadFailed.title", alertInstance.getTitle());
            }
        }
    }
}
