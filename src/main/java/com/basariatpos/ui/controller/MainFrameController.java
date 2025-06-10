package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.util.AppLogger; // Assuming AppLogger uses SLF4J

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert; // Added import
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.web.WebView; // Added for User Manual
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL; // Already present, good.
import java.util.Locale;
import java.util.ResourceBundle;

public class MainFrameController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(MainFrameController.class);

    @FXML
    private Label shiftStatusLabel;
    @FXML
    private Label welcomeLabel; // Added for the welcome message

    // For dynamic updates, get references to Menus/MenuItems if their text needs to be changed
    // Not strictly necessary if FXML loader handles %keys on reload, but good for manual refresh.
    @FXML private MenuBar menuBar;
    @FXML private Menu menuFile;
    @FXML private Menu menuFileLanguage;
    @FXML private MenuItem menuItemEnglish;
    @FXML private MenuItem menuItemArabic;
    @FXML private MenuItem menuFileExit;
    @FXML private Menu menuEdit;
    @FXML private Menu menuView;
    @FXML private Menu menuPatient;
    @FXML private Menu menuSales;
    @FXML private Menu menuInventory;
    @FXML private Menu menuReports;
    @FXML private Menu menuAdmin;
    @FXML private Menu menuHelp;
    @FXML private MenuItem menuHelpAbout;
    @FXML private MenuItem viewUserManualMenuItem; // New FXML ID


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 'resources' here is automatically loaded by FXMLLoader based on current Locale if FXML has fx:resources.
        // If not, or for more control, use LocaleManager and MessageProvider.
        // For now, we'll assume FXML's %key syntax works with FXMLLoader's default bundle loading.
        // We will manually set texts that need to be dynamic or explicitly controlled.

        // Set initial texts using MessageProvider to ensure consistency,
        // especially if the FXML loader's resource bundle isn't configured or if we want to override.
        refreshUITexts();
        logger.info("MainFrameController initialized. Default Locale: {}", LocaleManager.getCurrentLocale().toLanguageTag());
    }

    private void refreshUITexts() {
        // This method manually updates texts for components managed by this controller.
        // It's a basic approach for Sprint 0. A more advanced solution might involve
        // re-loading the FXML with a new ResourceBundle or binding text properties.

        // MenuBar items
        // Note: For FXML loaded with %, direct text setting might not be needed if the scene is reloaded.
        // However, for a simple refresh without full reload:
        if (menuBar != null) { // Check if FXML elements are injected
            menuFile.setText(MessageProvider.getString("menu.file"));
            menuFileLanguage.setText(MessageProvider.getString("menu.file.language"));
            menuItemEnglish.setText(MessageProvider.getString("menu.file.language.english"));
            menuItemArabic.setText(MessageProvider.getString("menu.file.language.arabic"));
            menuFileExit.setText(MessageProvider.getString("menu.file.exit"));
            menuEdit.setText(MessageProvider.getString("menu.edit"));
            menuView.setText(MessageProvider.getString("menu.view"));
            menuPatient.setText(MessageProvider.getString("menu.patient"));
            menuSales.setText(MessageProvider.getString("menu.sales"));
            menuInventory.setText(MessageProvider.getString("menu.inventory"));
            menuReports.setText(MessageProvider.getString("menu.reports"));
            menuAdmin.setText(MessageProvider.getString("menu.admin"));
            menuHelp.setText(MessageProvider.getString("menu.help"));
            menuHelpAbout.setText(MessageProvider.getString("menu.help.about"));
        }


        // Labels
        if (shiftStatusLabel != null) {
            shiftStatusLabel.setText(MessageProvider.getString("statusbar.shiftStatusLabel.initialText"));
        }
        if (welcomeLabel != null) {
            welcomeLabel.setText(MessageProvider.getString("label.welcome"));
        }

        logger.debug("UI texts refreshed for locale: {}", LocaleManager.getCurrentLocale().toLanguageTag());
    }


    @FXML
    private void handleSwitchToEnglish(ActionEvent event) {
        logger.info("Switching to English locale.");
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        // For Sprint 0, a full scene reload is simpler than complex binding.
        reloadScene();
        // refreshUITexts(); // Alternative: manual refresh if not reloading scene
    }

    @FXML
    private void handleSwitchToArabic(ActionEvent event) {
        logger.info("Switching to Arabic locale.");
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        reloadScene();
        // refreshUITexts();
    }

    private void reloadScene() {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow(); // Get current stage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/MainFrame.fxml"));

            // Set the resource bundle directly for the FXMLLoader
            loader.setResources(MessageProvider.getBundle());

            Parent root = loader.load();
            Scene scene = new Scene(root); // Create new scene

            // Potentially copy old scene properties if needed (e.g., size, position)
            // scene.getStylesheets().addAll(stage.getScene().getStylesheets()); // Keep stylesheets

            stage.setScene(scene); // Set the new scene
            stage.show();
            logger.info("Scene reloaded for locale: {}", LocaleManager.getCurrentLocale().toLanguageTag());
        } catch (IOException e) {
            logger.error("Failed to reload MainFrame.fxml after locale change.", e);
            // Show an error alert to the user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Locale Change Error"); // TODO: Localize title
            alert.setHeaderText("Failed to apply language change."); // TODO: Localize
            alert.setContentText("There was an error reloading the interface for the new language.\n" + e.getMessage()); // TODO: Localize
            alert.showAndWait();
        }
    }


    @FXML
    private void handleFileExit() {
        logger.info("File -> Exit selected (Action from: {})", MessageProvider.getString("menu.file.exit"));
        javafx.application.Platform.exit();
        // System.exit(0); // Not always needed if Platform.exit() is effective
    }

    @FXML
    private void handleHelpAbout() {
        logger.info("Help -> About selected. Opening About Dialog...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/AboutDialog.fxml"));
            // Ensure the loader uses the currently selected locale's resource bundle
            loader.setResources(MessageProvider.getBundle());

            Parent aboutDialogRoot = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("about.dialog.title"));
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // Corrected import

            // Set owner window if possible (e.g., from the menuBar)
            if (menuBar != null && menuBar.getScene() != null && menuBar.getScene().getWindow() != null) {
                dialogStage.initOwner(menuBar.getScene().getWindow());
            } else {
                logger.warn("Could not set owner for About Dialog: menuBar or its scene/window is null.");
            }

            Scene scene = new Scene(aboutDialogRoot);
            // Optional: Apply stylesheets if not automatically inherited or if specific dialog styles are needed
            // scene.getStylesheets().add(getClass().getResource("/com/basariatpos/ui/view/styles.css").toExternalForm());

            dialogStage.setScene(scene);
            dialogStage.setResizable(false); // About dialogs are often not resizable
            dialogStage.showAndWait(); // Show and wait for it to be closed

        } catch (IOException e) {
            logger.error("Failed to load or show About Dialog.", e);
            // Optionally show an error alert to the user
            javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR); // Corrected import
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Could not open About Dialog");
            errorAlert.setContentText("An error occurred while trying to display the About information.\n" + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    // Placeholder methods for other menu items if needed in the future
    // e.g., @FXML private void handleSomeAction() { logger.info("Action triggered"); }

    @FXML
    private void handleViewUserManual(ActionEvent event) {
        logger.info("View User Manual selected. Opening User Manual window...");
        try {
            Locale currentLocale = LocaleManager.getCurrentLocale();
            String langCode = currentLocale.getLanguage();
            String manualFileName = "UserManual_" + langCode + ".html";

            URL manualUrl = getClass().getResource("/com/basariatpos/help/" + manualFileName);

            if (manualUrl == null) {
                logger.warn("User manual file not found for locale {}: {}. Attempting fallback to English.", langCode, manualFileName);
                if (!LocaleManager.ENGLISH.getLanguage().equals(langCode)) { // Check if current lang is not already English
                    manualFileName = "UserManual_" + LocaleManager.ENGLISH.getLanguage() + ".html";
                    manualUrl = getClass().getResource("/com/basariatpos/help/" + manualFileName);
                    logger.info("Attempting to load English user manual: {}", manualFileName);
                }
            }

            if (manualUrl == null) {
                String finalAttemptedFile = manualFileName; // File that was last attempted
                logger.error("User manual file still not found after fallback: {}", finalAttemptedFile);
                showGenericErrorAlert( // Changed to use the new generic helper
                    MessageProvider.getString("help.error.loadFailed.title"),
                    MessageProvider.getString("help.error.loadFailed.content") + "\nFile: " + finalAttemptedFile
                );
                return;
            }

            WebView webView = new WebView();
            webView.getEngine().load(manualUrl.toExternalForm());

            Stage helpStage = new Stage();
            helpStage.setTitle(MessageProvider.getString("help.usermanual.title"));

            if (menuBar != null && menuBar.getScene() != null && menuBar.getScene().getWindow() != null) {
                helpStage.initOwner(menuBar.getScene().getWindow());
            } else {
                logger.warn("Could not set owner for User Manual window: menuBar or its scene/window is null.");
            }
            // Setting Modality can be considered if it should block other windows, but typically not for user manual.
            // helpStage.initModality(Modality.NONE); // Or Modality.WINDOW_MODAL if preferred

            Scene scene = new Scene(webView, 800, 600);
            helpStage.setScene(scene);
            helpStage.show();

        } catch (Exception e) {
            logger.error("Failed to load or show User Manual due to an unexpected error.", e);
            showGenericErrorAlert(
                MessageProvider.getString("help.error.loadFailed.title"),
                MessageProvider.getString("help.error.loadFailed.content") + "\nError: " + e.getMessage()
            );
        }
    }

    // Generic error alert helper method
    private void showGenericErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text for simpler alert
        alert.setContentText(content);
        alert.showAndWait();
    }
}
