package com.basariatpos.ui.controller;

import com.basariatpos.config.AppConfigLoader;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutDialogController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(AboutDialogController.class);

    @FXML private Label appNameText; // Set by FXML %key, but can be referenced if needed
    @FXML private Label appDescriptionText;
    @FXML private Label versionText;
    @FXML private Label developerNameEnText;
    @FXML private Label developerNameArText;
    @FXML private Label contactDetailsText;
    @FXML private Label copyrightText;
    @FXML private Button closeButton;
    @FXML private javafx.scene.layout.VBox aboutDialogRoot; // For RTL

    private AppConfigLoader appConfigLoader;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // In a DI setup, AppConfigLoader would be injected.
        // For Sprint 0, we can instantiate it directly.
        appConfigLoader = new AppConfigLoader(); // Loads "application.properties" by default

        // Set node orientation
        if (aboutDialogRoot != null) {
            if (com.basariatpos.i18n.LocaleManager.ARABIC.equals(com.basariatpos.i18n.LocaleManager.getCurrentLocale())) {
                aboutDialogRoot.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            } else {
                aboutDialogRoot.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            }
        } else {
            logger.warn("aboutDialogRoot is null in AboutDialogController.initialize(). RTL/LTR might not be set correctly.");
        }

        loadAndSetTexts();
        logger.info("AboutDialogController initialized.");
    }

    private void loadAndSetTexts() {
        // App Name is set via %about.dialog.appNameText in FXML, but if we want to override or ensure:
        // appNameText.setText(MessageProvider.getString("about.dialog.appNameText"));

        appDescriptionText.setText(MessageProvider.getString("app.description"));

        String version = appConfigLoader.getProperty("app.version", "N/A");
        versionText.setText(version);
        logger.debug("App version set to: {}", version);

        developerNameEnText.setText(MessageProvider.getString("about.dialog.developerNameEn"));
        developerNameArText.setText(MessageProvider.getString("about.dialog.developerNameAr"));
        contactDetailsText.setText(MessageProvider.getString("about.dialog.contactDetails"));
        copyrightText.setText(MessageProvider.getString("about.dialog.copyrightText"));

        // Close button text is set by FXML %button.close
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
            logger.debug("About dialog closed.");
        } else {
            logger.warn("Could not get stage from close button to close About dialog.");
        }
    }
}
