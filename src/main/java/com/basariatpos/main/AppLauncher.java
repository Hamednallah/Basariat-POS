package com.basariatpos.main;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.service.CenterProfileService; // For future check
import com.basariatpos.repository.CenterProfileRepositoryImpl; // For future check
import com.basariatpos.util.AppLogger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class AppLauncher extends Application {

    private static final Logger logger = AppLogger.getLogger(AppLauncher.class);
    private static Stage primaryStage;
    private static CenterProfileService centerProfileService; // For checking if profile is configured

    @Override
    public void init() throws Exception {
        super.init();
        // Initialize services that might be needed early
        // For now, centerProfileService for checking configuration status
        // In a DI setup, this would be handled by the container.
        centerProfileService = new CenterProfileService(new CenterProfileRepositoryImpl());

        // Set default locale at the very beginning
        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        logger.info("Application initializing. Default locale set to: {}", LocaleManager.getCurrentLocale().toLanguageTag());
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Primary stage is closing. Application will exit.");
            // Perform any cleanup if necessary
            // Platform.exit(); // Ensures JavaFX toolkit terminates
            // System.exit(0); // Ensures JVM terminates if non-daemon threads are running
        });

        checkProfileAndLaunch();
    }

    private void checkProfileAndLaunch() throws Exception {
        // This logic will be expanded in a future task to check if CenterProfile is configured.
        // For Sprint 0, we will assume it's not configured or go directly to login for simplicity of this task.
        // boolean profileConfigured = centerProfileService.isProfileConfigured();
        // if (!profileConfigured) {
        //    showCenterProfileSetupWizard();
        // } else {
        //    showLoginScreen();
        // }
        showLoginScreen(); // For current task, go directly to login
    }

    public static void showCenterProfileSetupWizard() throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLauncher.class.getResource("/com/basariatpos/ui/view/CenterProfileSetupWizard.fxml"));
        loader.setResources(MessageProvider.getBundle()); // Ensure wizard uses correct bundle
        Parent root = loader.load();

        // Inject service if controller needs it (manual for now)
        com.basariatpos.ui.controller.CenterProfileSetupController controller = loader.getController();
        controller.setCenterProfileService(centerProfileService); // Or however service is provided

        Stage wizardStage = new Stage();
        wizardStage.initModality(Modality.APPLICATION_MODAL);
        wizardStage.initOwner(primaryStage); // Optional: set owner

        Scene scene = new Scene(root);
        wizardStage.setScene(scene);
        wizardStage.setTitle(MessageProvider.getString("app.title.centerProfileSetup"));

        wizardStage.setOnCloseRequest(event -> {
            // After wizard closes, re-check and proceed to login or main app
            try {
                if (centerProfileService.isProfileConfigured()) {
                    showLoginScreen();
                } else {
                    logger.warn("Center profile setup was closed without completing configuration. Application might not function correctly.");
                    // Optionally, re-show wizard or exit. For now, try login.
                    showLoginScreen();
                }
            } catch (Exception e) {
                logger.error("Error after closing center profile wizard.", e);
            }
        });
        wizardStage.showAndWait(); // Show and wait for it to be closed
    }


    public static void showLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLauncher.class.getResource("/com/basariatpos/ui/view/LoginView.fxml"));
        loader.setResources(MessageProvider.getBundle()); // Ensure FXML loader uses the right bundle
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle(MessageProvider.getString("app.title.login"));
        primaryStage.show();
        logger.info("Login screen displayed.");
    }

    public static void showMainFrame() throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLauncher.class.getResource("/com/basariatpos/ui/view/MainFrame.fxml"));
        loader.setResources(MessageProvider.getBundle()); // Ensure FXML loader uses the right bundle
        Parent root = loader.load();
        Scene scene = new Scene(root, 1024, 768); // Default size for main app
        primaryStage.setScene(scene);
        primaryStage.setTitle(MessageProvider.getString("app.title.main"));
        primaryStage.show();
        logger.info("Main application frame displayed.");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        // Basic setup for logging if no external configuration is found yet for SLF4J.
        // System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");
        logger.info("Application main method started.");
        launch(args);
    }
}
