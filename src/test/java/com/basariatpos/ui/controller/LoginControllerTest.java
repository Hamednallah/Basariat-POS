package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.UserDTO;
import com.basariatpos.model.ShiftDTO;
import com.basariatpos.service.ShiftService;
import com.basariatpos.service.UserService;
import com.basariatpos.service.UserSessionService;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage; // For AppLauncher.showMainFrame, not directly used here for Stage ops

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

    @Mock private UserService mockUserService;
    @Mock private UserSessionService mockUserSessionService;
    @Mock private ShiftService mockShiftService;

    @Mock private TextField usernameField;
    @Mock private PasswordField passwordField;
    @Mock private Label errorMessageLabel;
    @Mock private ComboBox<Locale> languageComboBox;
    @Mock private VBox rootLoginPane; // Mock for RTL/LTR

    @InjectMocks
    private LoginController controller;

    private static ResourceBundle resourceBundle;
    private MockedStatic<AppLauncher> mockAppLauncher;
    private MockedStatic<Platform> mockPlatform;


    @BeforeAll
    static void setUpClass() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        resourceBundle = MessageProvider.getBundle();
         // Required for Platform.runLater if not using TestFX
        try {
            // Try to initialize JavaFX Toolkit if not already initialized
            // This is a common workaround for running JavaFX tests outside a TestFX runner
            new javafx.embed.swing.JFXPanel();
        } catch (Exception e) {
            System.err.println("JavaFX toolkit could not be initialized for tests: " + e.getMessage());
            // Depending on test needs, this might be a critical failure or ignorable
        }
    }

    @BeforeEach
    void setUp() {
        // Manual injection of mocks for @FXML fields
        controller.usernameField = usernameField;
        controller.passwordField = passwordField;
        controller.errorMessageLabel = errorMessageLabel;
        controller.languageComboBox = languageComboBox;
        controller.rootLoginPane = rootLoginPane; // Inject the mocked root pane

        // Mock static methods of AppLauncher used by LoginController
        // Services are now injected via @Mock and @InjectMocks for UserService and UserSessionService
        // But AppLauncher.getShiftService() is still used, and showMainFrame.
        mockAppLauncher = Mockito.mockStatic(AppLauncher.class);
        mockAppLauncher.when(AppLauncher::getUserService).thenReturn(mockUserService);
        mockAppLauncher.when(AppLauncher::getUserSessionService).thenReturn(mockUserSessionService);
        mockAppLauncher.when(AppLauncher::getShiftService).thenReturn(mockShiftService);

        // Mock Platform.runLater to execute runnable immediately for tests
        mockPlatform = Mockito.mockStatic(Platform.class);
        mockPlatform.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        });


        // Initialize the controller, which also calls setupLanguageComboBox and updateNodeOrientation
        // This will use the mocked services.
        controller.initialize(null, resourceBundle);
    }

    @AfterEach
    void tearDown() {
        mockAppLauncher.close();
        mockPlatform.close();
    }

    @Test
    void initialize_setsUpLanguageComboBoxAndNodeOrientation() {
        verify(languageComboBox).getItems(); // Check if items were added
        verify(languageComboBox).setConverter(any());
        verify(languageComboBox).setValue(Locale.ENGLISH);
        verify(rootLoginPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT); // Default is English
        verify(usernameField).requestFocus(); // Check focus request
    }

    @Test
    void handleLogin_success_noIncompleteShift() throws IOException {
        when(usernameField.getText()).thenReturn("testuser");
        when(passwordField.getText()).thenReturn("password");
        UserDTO user = new UserDTO(); user.setUsername("testuser"); user.setUserId(1L);
        when(mockUserService.authenticate("testuser", "password")).thenReturn(Optional.of(user));
        when(mockShiftService.getIncompleteShiftForUser(1L)).thenReturn(Optional.empty());

        controller.handleLogin(null);

        verify(mockUserSessionService).setCurrentUser(user);
        verify(errorMessageLabel).setVisible(false);
        mockAppLauncher.verify(() -> AppLauncher.showMainFrame(user, null));
    }

    @Test
    void handleLogin_success_withIncompleteShift() throws IOException {
        when(usernameField.getText()).thenReturn("testuser");
        when(passwordField.getText()).thenReturn("password");
        UserDTO user = new UserDTO(); user.setUsername("testuser"); user.setUserId(1L);
        ShiftDTO shift = new ShiftDTO(); shift.setShiftId(10L);
        when(mockUserService.authenticate("testuser", "password")).thenReturn(Optional.of(user));
        when(mockShiftService.getIncompleteShiftForUser(1L)).thenReturn(Optional.of(shift));

        controller.handleLogin(null);

        verify(mockUserSessionService).setCurrentUser(user);
        verify(errorMessageLabel).setVisible(false);
        mockAppLauncher.verify(() -> AppLauncher.showMainFrame(user, shift));
    }


    @Test
    void handleLogin_failure_emptyFields() {
        when(usernameField.getText()).thenReturn("");
        when(passwordField.getText()).thenReturn("password");

        controller.handleLogin(null);

        verify(errorMessageLabel).setText(MessageProvider.getString("login.error.authenticationFailed"));
        verify(errorMessageLabel).setVisible(true);
        verify(mockUserService, never()).authenticate(anyString(), anyString());
    }

    @Test
    void handleLogin_failure_authenticationFailed() throws Exception {
        when(usernameField.getText()).thenReturn("wronguser");
        when(passwordField.getText()).thenReturn("wrongpass");
        when(mockUserService.authenticate("wronguser", "wrongpass")).thenReturn(Optional.empty());

        controller.handleLogin(null);

        verify(errorMessageLabel).setText(MessageProvider.getString("login.error.authenticationFailed"));
        verify(errorMessageLabel).setVisible(true);
        verify(mockUserSessionService, never()).setCurrentUser(any());
        mockAppLauncher.verify(() -> AppLauncher.showMainFrame(any(), any()), never());
    }

    @Test
    void handleLogin_failure_serviceException() throws Exception {
        when(usernameField.getText()).thenReturn("testuser");
        when(passwordField.getText()).thenReturn("password");
        when(mockUserService.authenticate("testuser", "password")).thenThrow(new RuntimeException("Service unavailable"));

        controller.handleLogin(null);

        verify(errorMessageLabel).setText(MessageProvider.getString("login.error.serviceUnavailable"));
        verify(errorMessageLabel).setVisible(true);
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        // This test is a bit tricky because initialize is called in @BeforeEach
        // We need to change locale *before* initialize runs for this specific test.
        // One way: create a separate setup method or re-initialize.

        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        ResourceBundle arabicBundle = MessageProvider.getBundle(); // Get Arabic bundle

        // Re-initialize with Arabic locale
        // We need to setup mocks again before calling initialize, as @BeforeEach won't run again for this specific locale change.
        // However, the mocks are already set up. We just need to call initialize.
        controller.initialize(null, arabicBundle); // This will call updateNodeOrientation again

        verify(rootLoginPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        // Reset locale for other tests
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
    }

    // handleLanguageSwitch test would require mocking stage and scene reloading,
    // which is complex for a simple unit test and better for TestFX.
    // We can at least verify LocaleManager.setCurrentLocale is called.
    @Test
    void handleLanguageSwitch_changesLocaleManager() {
        when(languageComboBox.getValue()).thenReturn(LocaleManager.ARABIC);
        // Mock the scene reload part to prevent actual UI operations
        // For this, we'd need to mock the stage/scene access within reloadLoginScene or make it testable.
        // Let's assume reloadLoginScene() is complex to mock here and focus on LocaleManager call.

        // To avoid NullPointerException in reloadLoginScene if stage isn't fully mocked:
        // We can't easily stop reloadLoginScene from being called without more complex mocking or refactoring.
        // So, this test might be limited in scope if reloadLoginScene has deep UI dependencies.
        // For now, we'll just check if setCurrentLocale is called.

        // Temporarily disable the AppLauncher.reloadLoginScene call for this specific test path
        // This is a bit of a hack, ideally reloadLoginScene would be more testable.
        // Or we could mock Stage and other JavaFX components fully.
        // For now, we'll allow the call but expect it might log errors if stage/scene are not fully available in pure Mockito test.
        // A better approach is to use TestFX or similar for UI controller tests that interact with scene graph.

        controller.handleLanguageSwitch(null); // ActionEvent not used

        assertEquals(LocaleManager.ARABIC, LocaleManager.getCurrentLocale());
        // Further verification of scene reload is for UI tests.
        // mockAppLauncher.verify(() -> AppLauncher.reloadLoginScene(any()), atLeastOnce()); // This would be ideal if reloadLoginScene was static and testable
    }
}
