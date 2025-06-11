package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher; // For services
import com.basariatpos.model.ShiftDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.ShiftException;
import com.basariatpos.service.ShiftService;
import com.basariatpos.service.UserSessionService;
import com.basariatpos.service.ValidationException; // For start shift dialog
import com.basariatpos.util.AppLogger;

import javafx.application.Platform; // For Platform.exit()
import javafx.event.ActionEvent;
import java.math.BigDecimal; // For Shift opening float
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.Button; // For Shift Buttons
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional; // For Optional ShiftDTO
import java.util.ResourceBundle;

public class MainFrameController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(MainFrameController.class);

    @FXML
    private Label shiftStatusLabel;
    @FXML
    private Label welcomeLabel;

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
    @FXML private MenuItem viewUserManualMenuItem;
    @FXML private MenuItem userManagementMenuItem;
    @FXML private MenuItem editCenterProfileMenuItem;
    @FXML private MenuItem bankNameManagementMenuItem;
    @FXML private MenuItem expenseCategoryManagementMenuItem;
    @FXML private MenuItem productCategoryManagementMenuItem;
    @FXML private MenuItem appSettingsManagementMenuItem;

    // Shift Control Buttons
    @FXML private Button startShiftButton;
    @FXML private Button pauseShiftButton;
    @FXML private Button resumeShiftButton;
    // @FXML private Button endShiftButton; // For later sprint

    private ShiftService shiftService;
    private UserSessionService userSessionService;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.shiftService = AppLauncher.getShiftService();
        this.userSessionService = AppLauncher.getUserSessionService();

        if (this.shiftService == null || this.userSessionService == null) {
            logger.error("Critical services (ShiftService or UserSessionService) not available. Shift controls will be disabled.");
            if(startShiftButton!=null) startShiftButton.setDisable(true);
            if(pauseShiftButton!=null) pauseShiftButton.setDisable(true);
            if(resumeShiftButton!=null) resumeShiftButton.setDisable(true);
        }

        refreshMenuTexts();
        updateShiftStatusDisplayAndControls();

        logger.info("MainFrameController initialized. Default Locale: {}", LocaleManager.getCurrentLocale().toLanguageTag());
    }

    private void refreshMenuTexts() {
        if (menuBar != null) {
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
            // Ensure new menu items also get their text updated if not using %key binding fully
            if(viewUserManualMenuItem != null) viewUserManualMenuItem.setText(MessageProvider.getString("menu.help.viewUserManual"));
            if(userManagementMenuItem != null) userManagementMenuItem.setText(MessageProvider.getString("usermanagement.title"));
            if(editCenterProfileMenuItem != null) editCenterProfileMenuItem.setText(MessageProvider.getString("centerprofile.editor.title"));
            if(bankNameManagementMenuItem != null) bankNameManagementMenuItem.setText(MessageProvider.getString("bankname.management.title"));
            if(expenseCategoryManagementMenuItem != null) expenseCategoryManagementMenuItem.setText(MessageProvider.getString("expensecategory.management.title"));
            if(productCategoryManagementMenuItem != null) productCategoryManagementMenuItem.setText(MessageProvider.getString("productcategory.management.title"));
            if(appSettingsManagementMenuItem != null) appSettingsManagementMenuItem.setText(MessageProvider.getString("appsettings.management.title"));
        }

        if (welcomeLabel != null) {
            welcomeLabel.setText(MessageProvider.getString("label.welcome"));
        }
        // shiftStatusLabel text is handled by updateShiftStatusDisplayAndControls()
        logger.debug("Menu texts refreshed for locale: {}", LocaleManager.getCurrentLocale().toLanguageTag());
    }

    public void updateShiftStatusDisplayAndControls() {
        UserDTO currentUser = (userSessionService != null) ? userSessionService.getCurrentUser() : null;

        if (currentUser == null) {
            shiftStatusLabel.setText(MessageProvider.getString("mainframe.shiftstatus.noActiveShift"));
            if(startShiftButton != null) {
                startShiftButton.setDisable(true);
                startShiftButton.setVisible(true); startShiftButton.setManaged(true);
            }
            if(pauseShiftButton != null) {
                pauseShiftButton.setDisable(true);
                pauseShiftButton.setVisible(false); pauseShiftButton.setManaged(false);
            }
            if(resumeShiftButton != null) {
                resumeShiftButton.setDisable(true);
                resumeShiftButton.setVisible(false); resumeShiftButton.setManaged(false);
            }
            logger.info("No user logged in. Shift controls updated accordingly.");
            return;
        }

        if(startShiftButton != null) startShiftButton.setDisable(false);

        try {
            Optional<ShiftDTO> shiftOpt = shiftService.getActiveOrPausedShiftForUser(currentUser.getUserId());
            if (shiftOpt.isPresent()) {
                ShiftDTO currentShift = shiftOpt.get();
                if (userSessionService != null) userSessionService.setActiveShift(currentShift);

                String statusKey = "Active".equalsIgnoreCase(currentShift.getStatus()) ?
                                   "mainframe.shiftstatus.active" :
                                   "mainframe.shiftstatus.paused";
                shiftStatusLabel.setText(MessageProvider.getString(statusKey,
                                         String.valueOf(currentShift.getShiftId()),
                                         currentShift.getStartedByUsername() != null ? currentShift.getStartedByUsername() : currentUser.getUsername()));

                boolean isActive = "Active".equalsIgnoreCase(currentShift.getStatus());
                startShiftButton.setDisable(true);
                startShiftButton.setVisible(false); startShiftButton.setManaged(false);

                pauseShiftButton.setDisable(!isActive);
                pauseShiftButton.setVisible(isActive); pauseShiftButton.setManaged(isActive);

                resumeShiftButton.setDisable(isActive);
                resumeShiftButton.setVisible(!isActive); resumeShiftButton.setManaged(!isActive);

                logger.info("Shift status updated: ID {}, Status '{}', User '{}'. Controls adjusted.", currentShift.getShiftId(), currentShift.getStatus(), currentUser.getUsername());
            } else {
                if (userSessionService != null) userSessionService.clearActiveShift();
                shiftStatusLabel.setText(MessageProvider.getString("mainframe.shiftstatus.noActiveShift"));
                startShiftButton.setDisable(false);
                startShiftButton.setVisible(true); startShiftButton.setManaged(true);

                pauseShiftButton.setDisable(true);
                pauseShiftButton.setVisible(false); pauseShiftButton.setManaged(false);

                resumeShiftButton.setDisable(true);
                resumeShiftButton.setVisible(false); resumeShiftButton.setManaged(false);

                logger.info("No active/paused shift found for user '{}'. 'Start Shift' enabled.", currentUser.getUsername());
            }
        } catch (ShiftException e) {
            logger.error("Error updating shift status display: {}", e.getMessage(), e);
            showGenericErrorAlert("Shift Status Error", "Could not retrieve current shift status: " + e.getMessage());
            shiftStatusLabel.setText(MessageProvider.getString("mainframe.shiftstatus.noActiveShift"));
            startShiftButton.setDisable(true); startShiftButton.setVisible(true); startShiftButton.setManaged(true);
            pauseShiftButton.setDisable(true); pauseShiftButton.setVisible(false); pauseShiftButton.setManaged(false);
            resumeShiftButton.setDisable(true); resumeShiftButton.setVisible(false); resumeShiftButton.setManaged(false);
        }
    }

    @FXML
    private void handleSwitchToEnglish(ActionEvent event) {
        logger.info("Switching to English locale.");
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        reloadScene();
    }

    @FXML
    private void handleSwitchToArabic(ActionEvent event) {
        logger.info("Switching to Arabic locale.");
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        reloadScene();
    }

    private void reloadScene() {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/MainFrame.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            logger.info("Scene reloaded for locale: {}", LocaleManager.getCurrentLocale().toLanguageTag());
        } catch (IOException e) {
            logger.error("Failed to reload MainFrame.fxml after locale change.", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(MessageProvider.getString("Locale Change Error"));
            alert.setHeaderText(MessageProvider.getString("Failed to apply language change."));
            alert.setContentText(MessageProvider.getString("There was an error reloading the interface for the new language.") + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleFileExit() {
        logger.info("File -> Exit selected (Action from: {})", MessageProvider.getString("menu.file.exit"));
        Platform.exit();
    }

    @FXML
    private void handleHelpAbout() {
        logger.info("Help -> About selected. Opening About Dialog...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/AboutDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent aboutDialogRoot = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("about.dialog.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (menuBar != null && menuBar.getScene() != null && menuBar.getScene().getWindow() != null) {
                dialogStage.initOwner(menuBar.getScene().getWindow());
            }
            Scene scene = new Scene(aboutDialogRoot);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.error("Failed to load or show About Dialog.", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Could not open About Dialog");
            errorAlert.setContentText("An error occurred while trying to display the About information.\n" + e.getMessage());
            errorAlert.showAndWait();
        }
    }

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
                if (!LocaleManager.ENGLISH.getLanguage().equals(langCode)) {
                    manualFileName = "UserManual_" + LocaleManager.ENGLISH.getLanguage() + ".html";
                    manualUrl = getClass().getResource("/com/basariatpos/help/" + manualFileName);
                    logger.info("Attempting to load English user manual: {}", manualFileName);
                }
            }

            if (manualUrl == null) {
                String finalAttemptedFile = manualFileName;
                logger.error("User manual file still not found after fallback: {}", finalAttemptedFile);
                showGenericErrorAlert(
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
            }
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

    private void showGenericErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (getStage() != null) alert.initOwner(getStage());
        alert.showAndWait();
    }

    @FXML
    private void handleUserManagementAction(ActionEvent event) {
        logger.info("Admin -> User Management selected. Opening User Management window...");
        try {
            AppLauncher.showUserManagementView();
        } catch (IOException e) {
            logger.error("Failed to load or show User Management View.", e);
            showGenericErrorAlert(
                MessageProvider.getString("usermanagement.title"),
                "Could not open the User Management interface.\nError: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleEditCenterProfile(ActionEvent event) {
        logger.info("Admin -> Edit Center Profile selected. Loading editor view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/CenterProfileEditorView.fxml", "centerprofile.editor.title");
    }

    private void loadViewIntoCenter(String fxmlPath, String titleKey) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setResources(MessageProvider.getBundle());
            Parent viewRoot = loader.load();
            Object loadedController = loader.getController();

            if (loadedController instanceof CenterProfileEditorController) {
                ((CenterProfileEditorController) loadedController).setCenterProfileService(AppLauncher.getCenterProfileService());
            } else if (loadedController instanceof UserManagementController) {
                 ((UserManagementController) loadedController).setUserService(AppLauncher.getUserService());
            } else if (loadedController instanceof BankNameManagementController) {
                 ((BankNameManagementController) loadedController).setBankNameService(AppLauncher.getBankNameService());
            } else if (loadedController instanceof ExpenseCategoryManagementController) {
                ((ExpenseCategoryManagementController) loadedController).setExpenseCategoryService(AppLauncher.getExpenseCategoryService());
            } else if (loadedController instanceof ProductCategoryManagementController) {
                ((ProductCategoryManagementController) loadedController).setProductCategoryService(AppLauncher.getProductCategoryService());
            } else if (loadedController instanceof AppSettingsManagementController) {
                ((AppSettingsManagementController) loadedController).setApplicationSettingsService(AppLauncher.getApplicationSettingsService());
           }

            if (menuBar.getScene().getRoot() instanceof BorderPane) {
                BorderPane mainBorderPane = (BorderPane) menuBar.getScene().getRoot();
                mainBorderPane.setCenter(viewRoot);
                logger.info("View '{}' loaded into center.", MessageProvider.getString(titleKey));
            } else {
                logger.error("Main layout is not a BorderPane. Cannot load view into center.");
                showGenericErrorAlert("UI Error", "Cannot display the requested view due to an internal layout error.");
            }
        } catch (IOException e) {
            logger.error("Failed to load view from FXML: {}", fxmlPath, e);
            showGenericErrorAlert(
                "Error Loading View",
                "Could not open the view: " + MessageProvider.getString(titleKey) + "\nError: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleBankNameManagementAction(ActionEvent event) {
        logger.info("Admin -> Manage Bank Names selected. Loading Bank Name Management view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/BankNameManagementView.fxml", "bankname.management.title");
    }

    @FXML
    private void handleExpenseCategoryManagementAction(ActionEvent event) {
        logger.info("Admin -> Manage Expense Categories selected. Loading Expense Category Management view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/ExpenseCategoryManagementView.fxml", "expensecategory.management.title");
    }

    @FXML
    private void handleProductCategoryManagementAction(ActionEvent event) {
        logger.info("Admin -> Manage Product Categories selected. Loading Product Category Management view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/ProductCategoryManagementView.fxml", "productcategory.management.title");
    }

    @FXML
    private void handleAppSettingsManagementAction(ActionEvent event) {
        logger.info("Admin -> Manage Application Settings selected. Loading Application Settings Management view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/AppSettingsManagementView.fxml", "appsettings.management.title");
    }

    // --- Shift Action Handlers ---
    @FXML
    private void handleStartShiftAction(ActionEvent event) {
        UserDTO currentUser = userSessionService.getCurrentUser();
        if (currentUser == null) {
            showGenericErrorAlert("Error", MessageProvider.getString("shift.error.generic")); // More generic or specific "login required"
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/StartShiftDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();
            StartShiftDialogController dialogController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("startshiftdialog.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getStage());
            dialogStage.setScene(new Scene(dialogRoot));
            dialogController.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (dialogController.isSaved()) {
                BigDecimal openingFloat = dialogController.getOpeningFloat();
                ShiftDTO startedShift = shiftService.startNewShift(currentUser.getUserId(), openingFloat);
                userSessionService.setActiveShift(startedShift);
                showGenericInfoAlert(MessageProvider.getString("startshiftdialog.title"),
                                     MessageProvider.getString("shift.success.started", String.valueOf(startedShift.getShiftId())));
            }
        } catch (IOException e) {
            logger.error("Failed to load StartShiftDialog.fxml: {}", e.getMessage(), e);
            showGenericErrorAlert("UI Error", "Could not open the start shift form.");
        } catch (ValidationException | ShiftOperationException | ShiftException e) {
            logger.error("Error starting shift: {}", e.getMessage(), e);
            showGenericErrorAlert(MessageProvider.getString("startshiftdialog.title"), e.getMessage());
        } finally {
            updateShiftStatusDisplayAndControls();
        }
    }

    @FXML
    private void handlePauseShiftAction(ActionEvent event) {
        ShiftDTO activeShift = userSessionService.getActiveShift();
        UserDTO currentUser = userSessionService.getCurrentUser();
        if (activeShift == null || currentUser == null || !"Active".equalsIgnoreCase(activeShift.getStatus())) {
            showGenericErrorAlert("Error", MessageProvider.getString("shift.error.notActiveToPause"));
            return;
        }
        try {
            shiftService.pauseActiveShift(activeShift.getShiftId(), currentUser.getUserId());
            activeShift.setStatus("Paused");
            userSessionService.setActiveShift(activeShift);
            showGenericInfoAlert("Shift Paused", MessageProvider.getString("shift.success.paused", String.valueOf(activeShift.getShiftId())));
        } catch (ShiftException e) {
            logger.error("Error pausing shift ID {}: {}", activeShift.getShiftId(), e.getMessage(), e);
            showGenericErrorAlert("Pause Shift Error", e.getMessage());
        } finally {
            updateShiftStatusDisplayAndControls();
        }
    }

    @FXML
    private void handleResumeShiftAction(ActionEvent event) {
        UserDTO currentUser = userSessionService.getCurrentUser();
        if (currentUser == null) {
             showGenericErrorAlert("Error", "No user logged in.");
             return;
        }

        Optional<ShiftDTO> shiftToResumeOpt;
        try {
            shiftToResumeOpt = shiftService.getActiveOrPausedShiftForUser(currentUser.getUserId());
        } catch (ShiftException e) {
            logger.error("Error fetching shift to resume for user {}: {}", currentUser.getUserId(), e.getMessage());
            showGenericErrorAlert("Resume Shift Error", "Could not fetch current shift status: " + e.getMessage());
            updateShiftStatusDisplayAndControls();
            return;
        }

        if (shiftToResumeOpt.isEmpty() || !"Paused".equalsIgnoreCase(shiftToResumeOpt.get().getStatus())) {
            showGenericErrorAlert("Error", MessageProvider.getString("shift.error.notPausedToResume"));
            return;
        }

        ShiftDTO shiftToResume = shiftToResumeOpt.get();
        try {
            ShiftDTO resumedShift = shiftService.resumePausedShift(shiftToResume.getShiftId(), currentUser.getUserId());
            userSessionService.setActiveShift(resumedShift);
            showGenericInfoAlert("Shift Resumed", MessageProvider.getString("shift.success.resumed", String.valueOf(resumedShift.getShiftId())));
        } catch (ShiftException e) {
            logger.error("Error resuming shift ID {}: {}", shiftToResume.getShiftId(), e.getMessage(), e);
            showGenericErrorAlert("Resume Shift Error", e.getMessage());
        } finally {
            updateShiftStatusDisplayAndControls();
        }
    }

    private void showGenericInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(getStage());
        alert.showAndWait();
    }

    private Stage getStage() {
        if (menuBar != null && menuBar.getScene() != null) {
            return (Stage) menuBar.getScene().getWindow();
        }
        logger.warn("Could not reliably determine the current stage from menuBar.");
        return null;
    }
}
