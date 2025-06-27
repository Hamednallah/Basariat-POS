package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.MockUserServiceImpl; // For Sprint 0
import com.basariatpos.service.UserSessionService;
import com.basariatpos.service.UserService;
import com.basariatpos.util.AppLogger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private ComboBox<Locale> languageComboBox;
    @FXML private Label errorMessageLabel;
    @FXML private javafx.scene.layout.VBox rootLoginPane; // For RTL support

    private UserService userService;
    private UserSessionService userSessionService;
    private ShiftService shiftService; // Added ShiftService
    private ResourceBundle resources;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        // Obtain services from AppLauncher (or DI framework in future)
        this.userService = AppLauncher.getUserService();
        this.userSessionService = AppLauncher.getUserSessionService();
        this.shiftService = AppLauncher.getShiftService();

        if (userService == null || userSessionService == null || shiftService == null) {
            logger.error("One or more critical services not available in LoginController. Login might fail.");
            // Optionally disable login button or show a general error here
        }

        setupLanguageComboBox();
        errorMessageLabel.setText("");
        errorMessageLabel.setVisible(false);

        // Set focus on username field initially
        Platform.runLater(() -> usernameField.requestFocus());

        updateNodeOrientation();

        logger.info("LoginController initialized. Current locale: {}", LocaleManager.getCurrentLocale().toLanguageTag());
    }

    private void updateNodeOrientation() {
        if (rootLoginPane != null) {
            if (LocaleManager.ARABIC.equals(LocaleManager.getCurrentLocale())) {
                rootLoginPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                rootLoginPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("rootLoginPane is null. Cannot set RTL/LTR orientation.");
        }
    }

    private void setupLanguageComboBox() {
        languageComboBox.getItems().addAll(LocaleManager.getSupportedLocales());
        languageComboBox.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale locale) {
                if (locale == null) return "";
                // Use MessageProvider for localized language names
                if (locale.equals(LocaleManager.ENGLISH)) {
                    return MessageProvider.getString("login.language.english");
                } else if (locale.equals(LocaleManager.ARABIC)) {
                    return MessageProvider.getString("login.language.arabic");
                }
                return locale.getDisplayLanguage(); // Fallback
            }

            @Override
            public Locale fromString(String string) {
                // Not needed for ComboBox if not editable
                return null;
            }
        });
        languageComboBox.setValue(LocaleManager.getCurrentLocale());
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError(MessageProvider.getString("login.error.authenticationFailed")); // Or a more specific "fields required"
            return;
        }

        try {
            Optional<UserDTO> userOpt = userService.authenticate(username, password);

            if (userOpt.isPresent()) {
                userSessionService.setCurrentUser(userOpt.get());
                logger.info("User {} logged in successfully. Role: {}", userOpt.get().getUsername(), userOpt.get().getRole());
                errorMessageLabel.setVisible(false);

                // Check for interrupted shifts
                UserDTO authenticatedUser = userOpt.get();
                Optional<ShiftDTO> incompleteShiftOpt = shiftService.getIncompleteShiftForUser(authenticatedUser.getUserId());

                if (incompleteShiftOpt.isPresent()) {
                    logger.info("Incomplete shift found for user {}. Passing to AppLauncher.", authenticatedUser.getUsername());
                    AppLauncher.showMainFrame(authenticatedUser, incompleteShiftOpt.get());
                } else {
                    AppLauncher.showMainFrame(authenticatedUser, null);
                }
            } else {
                showError(MessageProvider.getString("login.error.authenticationFailed"));
                logger.warn("Authentication failed for username: {}", username);
            }
        } catch (Exception e) { // Catch broader exceptions from service if any (e.g., DB down in real service)
            logger.error("Error during authentication for user {}: {}", username, e.getMessage(), e);
            showError(MessageProvider.getString("login.error.serviceUnavailable"));
        }
    }

    @FXML
    private void handleLanguageSwitch(ActionEvent event) {
        Locale selectedLocale = languageComboBox.getValue();
        if (selectedLocale != null && !selectedLocale.equals(LocaleManager.getCurrentLocale())) {
            LocaleManager.setCurrentLocale(selectedLocale);
            logger.info("Language switched to: {}", selectedLocale.toLanguageTag());
            // Reload the login scene to apply the new locale
            reloadLoginScene();
        }
    }

    private void reloadLoginScene() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/LoginView.fxml"));
            loader.setResources(MessageProvider.getBundle()); // Get new bundle for the selected locale
            Parent root = loader.load();

            // Access controller after loading to set orientation if needed, or ensure FXML root has fx:id
            // LoginController newControllerInstance = loader.getController();
            // newControllerInstance.updateNodeOrientation(); // if controller needs to do it post-load

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(MessageProvider.getString("app.title.login")); // Update title too

            // Explicitly update orientation of the new root if it's not handled by its own controller's initialize
            if (root instanceof javafx.scene.layout.VBox) { // Assuming root is VBox as per FXML
                 if (LocaleManager.ARABIC.equals(LocaleManager.getCurrentLocale())) {
                    root.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
                } else {
                    root.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
                }
            }

            logger.info("Login scene reloaded for locale: {}", LocaleManager.getCurrentLocale().toLanguageTag());
        } catch (IOException e) {
            logger.error("Failed to reload LoginView.fxml after locale change.", e);
            showError("Error applying language change. Please restart.");
        }
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }
}
