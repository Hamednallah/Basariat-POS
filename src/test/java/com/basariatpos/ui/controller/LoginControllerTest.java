package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher; // For mocking static methods
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.UserSessionService;
import com.basariatpos.service.UserService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.Optional;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class LoginControllerTest {

    @Mock private UserService mockUserService;
    @Mock private UserSessionService mockUserSessionService;

    private LoginController controller; // Instance of the controller if needed for direct calls
    private MockedStatic<AppLauncher> mockAppLauncherStatic;

    // UI element selectors
    private final String USERNAME_FIELD = "#usernameField";
    private final String PASSWORD_FIELD = "#passwordField";
    private final String LOGIN_BUTTON = "#loginButton";
    private final String ERROR_MESSAGE_LABEL = "#errorMessageLabel";
    private final String LANGUAGE_COMBO_BOX = "#languageComboBox";

    @Start
    private void start(Stage stage) throws IOException {
        MockitoAnnotations.openMocks(this); // Initialize mocks for this test instance

        // Mock static methods of AppLauncher
        mockAppLauncherStatic = Mockito.mockStatic(AppLauncher.class);
        mockAppLauncherStatic.when(AppLauncher::getPrimaryStage).thenReturn(stage); // Provide the current stage
        mockAppLauncherStatic.when(() -> AppLauncher.showMainFrame()).thenAnswer(invocation -> null); // Do nothing for showMainFrame

        // Set default locale for consistency
        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE); // e.g., Arabic
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/LoginView.fxml"));
        loader.setResources(bundle);

        // Controller factory to inject mocks
        loader.setControllerFactory(param -> {
            LoginController loginController = new LoginController();
            // Manually inject mocks into the controller instance created by FXMLLoader
            // This requires LoginController to have setters or a constructor for these services.
            // For this test, I'll assume LoginController news up its dependencies or has setters.
            // Let's modify LoginController to allow setting these for tests or use a DI approach.
            // For now, the LoginController in the provided code news them up.
            // To test properly, we'd need to refactor LoginController or use PowerMock/Mockito subclassing.
            // A simpler way if LoginController news them: Use reflection to set the mock fields.
            try {
                java.lang.reflect.Field userServiceField = LoginController.class.getDeclaredField("userService");
                userServiceField.setAccessible(true);
                userServiceField.set(loginController, mockUserService);

                java.lang.reflect.Field sessionServiceField = LoginController.class.getDeclaredField("userSessionService");
                sessionServiceField.setAccessible(true);
                sessionServiceField.set(loginController, mockUserSessionService);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                fail("Failed to inject mocks into LoginController via reflection.", e);
            }
            this.controller = loginController; // Keep a reference to the controller instance
            return loginController;
        });

        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp(FxRobot robot) {
        // Clear fields before each test
        robot.clickOn(USERNAME_FIELD).write("");
        robot.clickOn(PASSWORD_FIELD).write("");
        // Ensure error message is hidden
        robot.lookup(ERROR_MESSAGE_LABEL).queryAs(Label.class).setVisible(false);
        // Reset mock interactions
        reset(mockUserService, mockUserSessionService); // Reset mocks for fresh verification
    }

    @AfterEach
    void tearDown() {
        mockAppLauncherStatic.close(); // Release static mock
    }

    @Test
    void login_successful(FxRobot robot) {
        // Arrange
        UserDTO mockUser = new UserDTO(1, "testadmin", "Admin User", "Admin");
        when(mockUserService.authenticate("testadmin", "password")).thenReturn(Optional.of(mockUser));

        // Act
        robot.clickOn(USERNAME_FIELD).write("testadmin");
        robot.clickOn(PASSWORD_FIELD).write("password");
        robot.clickOn(LOGIN_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        // Assert
        verify(mockUserService).authenticate("testadmin", "password");
        ArgumentCaptor<UserDTO> userCaptor = ArgumentCaptor.forClass(UserDTO.class);
        verify(mockUserSessionService).setCurrentUser(userCaptor.capture());
        assertEquals("testadmin", userCaptor.getValue().getUsername());

        Label errorMessage = robot.lookup(ERROR_MESSAGE_LABEL).queryAs(Label.class);
        assertFalse(errorMessage.isVisible(), "Error message should be hidden on successful login.");

        mockAppLauncherStatic.verify(() -> AppLauncher.showMainFrame(), times(1)); // Verify main frame is shown
    }

    @Test
    void login_failed_invalidCredentials(FxRobot robot) {
        // Arrange
        when(mockUserService.authenticate(anyString(), anyString())).thenReturn(Optional.empty());

        // Act
        robot.clickOn(USERNAME_FIELD).write("wronguser");
        robot.clickOn(PASSWORD_FIELD).write("wrongpass");
        robot.clickOn(LOGIN_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        // Assert
        verify(mockUserService).authenticate("wronguser", "wrongpass");
        verify(mockUserSessionService, never()).setCurrentUser(any(UserDTO.class));

        Label errorMessage = robot.lookup(ERROR_MESSAGE_LABEL).queryAs(Label.class);
        assertTrue(errorMessage.isVisible(), "Error message should be visible on failed login.");
        assertEquals(MessageProvider.getString("login.error.authenticationFailed"), errorMessage.getText());

        mockAppLauncherStatic.verify(() -> AppLauncher.showMainFrame(), never()); // Main frame should not be shown
    }

    @Test
    void login_emptyFields_showsError(FxRobot robot) {
        // Act
        robot.clickOn(LOGIN_BUTTON); // Click with empty fields
        WaitForAsyncUtils.waitForFxEvents();

        // Assert
        verify(mockUserService, never()).authenticate(anyString(), anyString());
        Label errorMessage = robot.lookup(ERROR_MESSAGE_LABEL).queryAs(Label.class);
        assertTrue(errorMessage.isVisible());
        // Check for a generic "fields required" or the auth failed message based on controller logic
        assertEquals(MessageProvider.getString("login.error.authenticationFailed"), errorMessage.getText());
    }


    @Test
    void languageSwitch_reloadsScene_and_updatesLocaleManager(FxRobot robot) {
        // Arrange
        Locale initialLocale = LocaleManager.getCurrentLocale();
        Locale targetLocale = initialLocale.equals(LocaleManager.ENGLISH) ? LocaleManager.ARABIC : LocaleManager.ENGLISH;

        // Act
        robot.clickOn(LANGUAGE_COMBO_BOX);
        // Select the targetLocale. This requires knowing how locales are displayed and ordered.
        // For simplicity, let's assume we can select it directly if ComboBox items are Locales.
        // This might need adjustment based on how StringConverter and items are set up.
        robot.interact(() -> robot.lookup(LANGUAGE_COMBO_BOX).queryAs(ComboBox.class).setValue(targetLocale));
        WaitForAsyncUtils.waitForFxEvents(); // Wait for onAction and scene reload

        // Assert
        assertEquals(targetLocale, LocaleManager.getCurrentLocale(), "LocaleManager should be updated to the new locale.");

        // Verify scene reload by checking if title is updated (if title is set from bundle)
        Stage currentStage = (Stage) robot.window(0); // Get current stage
        assertEquals(MessageProvider.getString("app.title.login"), currentStage.getTitle(), "Window title should be updated for new locale.");

        // Verify a known UI element's text is updated (e.g., login button)
        // This requires the scene to be fully reloaded and TestFX to pick up new elements.
        // Button loginButtonNode = robot.lookup(LOGIN_BUTTON).queryAs(Button.class);
        // assertEquals(MessageProvider.getString("login.button.login"), loginButtonNode.getText());
        // Note: Verifying reloaded scene content can be complex and depends on reload implementation.
        // The primary check here is that LocaleManager's current locale changed.
    }
}
