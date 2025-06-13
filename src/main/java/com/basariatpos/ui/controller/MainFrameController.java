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
import java.util.Optional;
import java.util.ResourceBundle;

public class MainFrameController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(MainFrameController.class);

    @FXML private Label shiftStatusLabel;
    @FXML private Label welcomeLabel;

    @FXML private MenuBar menuBar;
    // --- Menu FXML Fields (matching the new FXML) ---
    @FXML private Menu menuFile;
    @FXML private MenuItem menuItemFileNewPlaceholder;
    @FXML private MenuItem menuItemFileOpenPlaceholder;
    @FXML private Menu menuFileLanguage;
    @FXML private MenuItem menuItemEnglish;
    @FXML private MenuItem menuItemArabic;
    @FXML private MenuItem menuItemFileSettings;
    @FXML private MenuItem menuFileExit;

    @FXML private Menu menuEdit;
    @FXML private MenuItem menuItemEditUndo;
    @FXML private MenuItem menuItemEditRedo;
    @FXML private MenuItem menuItemEditCut;
    @FXML private MenuItem menuItemEditCopy;
    @FXML private MenuItem menuItemEditPaste;

    @FXML private Menu menuView;
    @FXML private MenuItem menuItemViewZoomIn;
    @FXML private MenuItem menuItemViewZoomOut;
    @FXML private MenuItem menuItemViewResetZoom;

    @FXML private Menu menuPatient;
    @FXML private MenuItem patientManagementMenuItem; // Existing, retained
    @FXML private MenuItem menuItemPatientNewOpticalRx;

    @FXML private Menu menuSales;
    @FXML private MenuItem menuItemSalesNewSale;
    @FXML private MenuItem menuItemSalesViewSales;
    @FXML private MenuItem menuItemSalesNewReturn;

    @FXML private Menu menuInventory;
    @FXML private MenuItem productManagementMenuItem; // Existing, retained
    @FXML private MenuItem inventoryItemManagementMenuItem; // Existing, retained (was handleInventoryManageStock)
    @FXML private MenuItem purchaseOrderManagementMenuItem; // Existing, retained
    @FXML private MenuItem stockAdjustmentMenuItem; // Existing, retained

    @FXML private Menu menuReports;
    @FXML private MenuItem menuItemReportsSalesReport;
    @FXML private MenuItem menuItemReportsInventoryReport;
    @FXML private MenuItem menuItemReportsFinancialReport;

    @FXML private Menu menuAdmin;
    @FXML private MenuItem userManagementMenuItem; // Existing, retained
    @FXML private MenuItem editCenterProfileMenuItem; // Existing, retained
    @FXML private MenuItem appSettingsManagementMenuItem; // Existing, retained
    @FXML private MenuItem bankNameManagementMenuItem; // Existing, retained
    @FXML private MenuItem expenseCategoryManagementMenuItem; // Existing, retained
    @FXML private MenuItem productCategoryManagementMenuItem; // Existing, retained
    @FXML private MenuItem menuItemAdminDbManagement;

    @FXML private Menu menuHelp;
    @FXML private MenuItem viewUserManualMenuItem; // Existing, retained
    @FXML private MenuItem menuHelpAbout; // Existing, retained

    // Shift Control Buttons (existing, retained)
    @FXML private Button startShiftButton;
    @FXML private Button pauseShiftButton;
    @FXML private Button resumeShiftButton;

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

            // Update texts for new File menu items
            if(menuItemFileNewPlaceholder != null) menuItemFileNewPlaceholder.setText(MessageProvider.getString("menu.file.newPlaceholder"));
            if(menuItemFileOpenPlaceholder != null) menuItemFileOpenPlaceholder.setText(MessageProvider.getString("menu.file.openPlaceholder"));
            if(menuItemFileSettings != null) menuItemFileSettings.setText(MessageProvider.getString("menu.file.settings"));

            menuEdit.setText(MessageProvider.getString("menu.edit"));
            // Update texts for new Edit menu items
            if(menuItemEditUndo != null) menuItemEditUndo.setText(MessageProvider.getString("menu.edit.undo"));
            if(menuItemEditRedo != null) menuItemEditRedo.setText(MessageProvider.getString("menu.edit.redo"));
            if(menuItemEditCut != null) menuItemEditCut.setText(MessageProvider.getString("menu.edit.cut"));
            if(menuItemEditCopy != null) menuItemEditCopy.setText(MessageProvider.getString("menu.edit.copy"));
            if(menuItemEditPaste != null) menuItemEditPaste.setText(MessageProvider.getString("menu.edit.paste"));

            menuView.setText(MessageProvider.getString("menu.view"));
            // Update texts for new View menu items
            if(menuItemViewZoomIn != null) menuItemViewZoomIn.setText(MessageProvider.getString("menu.view.zoomIn"));
            if(menuItemViewZoomOut != null) menuItemViewZoomOut.setText(MessageProvider.getString("menu.view.zoomOut"));
            if(menuItemViewResetZoom != null) menuItemViewResetZoom.setText(MessageProvider.getString("menu.view.resetZoom"));

            menuPatient.setText(MessageProvider.getString("menu.patient"));
            if(patientManagementMenuItem != null) patientManagementMenuItem.setText(MessageProvider.getString("menu.patient.manage")); // Adjusted key for consistency
            if(menuItemPatientNewOpticalRx != null) menuItemPatientNewOpticalRx.setText(MessageProvider.getString("menu.patient.newOpticalRx"));

            menuSales.setText(MessageProvider.getString("menu.sales"));
            if(menuItemSalesNewSale != null) menuItemSalesNewSale.setText(MessageProvider.getString("menu.sales.newSale"));
            if(menuItemSalesViewSales != null) menuItemSalesViewSales.setText(MessageProvider.getString("menu.sales.viewSales"));
            if(menuItemSalesNewReturn != null) menuItemSalesNewReturn.setText(MessageProvider.getString("menu.sales.newReturn"));

            menuInventory.setText(MessageProvider.getString("menu.inventory"));
            if(productManagementMenuItem != null) productManagementMenuItem.setText(MessageProvider.getString("menu.inventory.manageProducts")); // Adjusted key
            if(inventoryItemManagementMenuItem != null) inventoryItemManagementMenuItem.setText(MessageProvider.getString("menu.inventory.manageStock")); // Adjusted key
            if(purchaseOrderManagementMenuItem != null) purchaseOrderManagementMenuItem.setText(MessageProvider.getString("menu.inventory.purchaseOrders")); // Adjusted key
            if(stockAdjustmentMenuItem != null) stockAdjustmentMenuItem.setText(MessageProvider.getString("menu.inventory.stockAdjustments")); // Adjusted key

            menuReports.setText(MessageProvider.getString("menu.reports"));
            if(menuItemReportsSalesReport != null) menuItemReportsSalesReport.setText(MessageProvider.getString("menu.reports.salesReport"));
            if(menuItemReportsInventoryReport != null) menuItemReportsInventoryReport.setText(MessageProvider.getString("menu.reports.inventoryReport"));
            if(menuItemReportsFinancialReport != null) menuItemReportsFinancialReport.setText(MessageProvider.getString("menu.reports.financialReport"));

            menuAdmin.setText(MessageProvider.getString("menu.admin"));
            if(userManagementMenuItem != null) userManagementMenuItem.setText(MessageProvider.getString("menu.admin.userManagement")); // Adjusted key
            if(editCenterProfileMenuItem != null) editCenterProfileMenuItem.setText(MessageProvider.getString("menu.admin.centerProfile")); // Adjusted key
            if(appSettingsManagementMenuItem != null) appSettingsManagementMenuItem.setText(MessageProvider.getString("menu.admin.applicationSettings")); // Adjusted key
            if(bankNameManagementMenuItem != null) bankNameManagementMenuItem.setText(MessageProvider.getString("bankname.management.title")); // Existing key fine
            if(expenseCategoryManagementMenuItem != null) expenseCategoryManagementMenuItem.setText(MessageProvider.getString("expensecategory.management.title")); // Existing key fine
            if(productCategoryManagementMenuItem != null) productCategoryManagementMenuItem.setText(MessageProvider.getString("productcategory.management.title")); // Existing key fine
            if(menuItemAdminDbManagement != null) menuItemAdminDbManagement.setText(MessageProvider.getString("menu.admin.dbManagement"));

            menuHelp.setText(MessageProvider.getString("menu.help"));
            if(viewUserManualMenuItem != null) viewUserManualMenuItem.setText(MessageProvider.getString("menu.help.viewUserManual"));
            if(menuHelpAbout != null) menuHelpAbout.setText(MessageProvider.getString("menu.help.about"));
        }

        if (welcomeLabel != null) {
            // Original FXML from subtask description used "Welcome to Basariat POS"
            // The new FXML uses "%label.welcomeToBasariatPOS"
            welcomeLabel.setText(MessageProvider.getString("label.welcomeToBasariatPOS"));
        }
        if (shiftStatusLabel != null) {
            // Original FXML from subtask description used "Shift Status: Not Active"
            // The new FXML uses "%label.shiftStatusNotActive"
            // This will be updated by updateShiftStatusDisplayAndControls anyway, but for initial text:
            shiftStatusLabel.setText(MessageProvider.getString("label.shiftStatusNotActive"));
        }
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
            Optional<ShiftDTO> shiftOpt = shiftService.getIncompleteShiftForUser(currentUser.getUserId()); // Changed method name
            if (shiftOpt.isPresent()) {
                ShiftDTO currentShift = shiftOpt.get();
                if (userSessionService != null) userSessionService.setActiveShift(currentShift);

                String statusKey;
                if ("Active".equalsIgnoreCase(currentShift.getStatus())) {
                    statusKey = "mainframe.shiftstatus.active";
                } else if ("Paused".equalsIgnoreCase(currentShift.getStatus())) {
                    statusKey = "mainframe.shiftstatus.paused";
                } else { // Interrupted or other
                    statusKey = "mainframe.shiftstatus.interrupted";
                }
                shiftStatusLabel.setText(MessageProvider.getString(statusKey,
                                         String.valueOf(currentShift.getShiftId()),
                                         currentShift.getStartedByUsername() != null ? currentShift.getStartedByUsername() : currentUser.getUsername()));

                boolean isActive = "Active".equalsIgnoreCase(currentShift.getStatus());
                boolean isPausedOrInterrupted = "Paused".equalsIgnoreCase(currentShift.getStatus()) || "Interrupted".equalsIgnoreCase(currentShift.getStatus());

                startShiftButton.setDisable(true);
                startShiftButton.setVisible(false); startShiftButton.setManaged(false);

                pauseShiftButton.setDisable(!isActive);
                pauseShiftButton.setVisible(isActive); pauseShiftButton.setManaged(isActive);

                resumeShiftButton.setDisable(!isPausedOrInterrupted);
                resumeShiftButton.setVisible(isPausedOrInterrupted); resumeShiftButton.setManaged(isPausedOrInterrupted);

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

            // Service Injection Block - Ensure this is kept up-to-date with all views loaded this way
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
            } else if (loadedController instanceof PatientManagementController) {
                PatientManagementController patientCtrl = (PatientManagementController) loadedController;
                patientCtrl.setPatientService(AppLauncher.getPatientService());
                patientCtrl.setOpticalDiagnosticService(AppLauncher.getOpticalDiagnosticService());
            } else if (loadedController instanceof ProductManagementController) {
                ProductManagementController productCtrl = (ProductManagementController) loadedController;
                productCtrl.setProductService(AppLauncher.getProductService());
                productCtrl.setProductCategoryService(AppLauncher.getProductCategoryService());
            } else if (loadedController instanceof InventoryItemManagementController) { // Added for Inventory Item Management
                 InventoryItemManagementController invItemCtrl = (InventoryItemManagementController) loadedController;
                 invItemCtrl.setInventoryItemService(AppLauncher.getInventoryItemService());
                 invItemCtrl.setProductService(AppLauncher.getProductService()); // For product dropdown
            } else if (loadedController instanceof PurchaseOrderListController) { // Added for Purchase Order List
                ((PurchaseOrderListController) loadedController).setPurchaseOrderService(AppLauncher.getPurchaseOrderService());
            }
            // End Service Injection Block

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
        logger.info("Inventory -> Manage Product Categories selected. Loading Product Category Management view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/ProductCategoryManagementView.fxml", "productcategory.management.title");
    }

    @FXML
    private void handleProductManagementAction(ActionEvent event) { // New handler
        logger.info("Inventory -> Manage Products selected. Loading Product Management view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/ProductManagementView.fxml", "product.management.title");
    }

    @FXML
    private void handleAppSettingsManagementAction(ActionEvent event) {
        logger.info("Admin -> Manage Application Settings selected. Loading Application Settings Management view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/AppSettingsManagementView.fxml", "appsettings.management.title");
    }

    @FXML
    private void handlePatientManagementAction(ActionEvent event) {
        logger.info("Patient -> Patient Management selected. Loading Patient Management view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/PatientManagementView.fxml", "patientmanagement.title");
    }

    // --- Shift Action Handlers ---
    @FXML
    private void handleStartShiftAction(ActionEvent event) {
        UserDTO currentUser = userSessionService.getCurrentUser();
        if (currentUser == null) {
            showGenericErrorAlert("Error", MessageProvider.getString("shift.error.generic"));
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
            shiftToResumeOpt = shiftService.getIncompleteShiftForUser(currentUser.getUserId());
        } catch (ShiftException e) {
            logger.error("Error fetching shift to resume for user {}: {}", currentUser.getUserId(), e.getMessage());
            showGenericErrorAlert("Resume Shift Error", "Could not fetch current shift status: " + e.getMessage());
            updateShiftStatusDisplayAndControls();
            return;
        }

        if (shiftToResumeOpt.isEmpty() || !("Paused".equalsIgnoreCase(shiftToResumeOpt.get().getStatus()) || "Interrupted".equalsIgnoreCase(shiftToResumeOpt.get().getStatus()))) {
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

    public void handleInterruptedShift(ShiftDTO interruptedShift) {
        logger.info("Handling interrupted shift ID: {}", interruptedShift.getShiftId());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/InterruptedShiftDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();
            InterruptedShiftDialogController dialogController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("interruptedshift.dialog.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getStage());
            dialogStage.setScene(new Scene(dialogRoot));

            dialogController.initializeDialog(interruptedShift, dialogStage);
            dialogStage.showAndWait();

            InterruptedShiftDialogController.InterruptedShiftAction choice = dialogController.getResult();
            UserDTO currentUser = userSessionService.getCurrentUser(); // Should still be available

            if (choice == InterruptedShiftDialogController.InterruptedShiftAction.RESUME) {
                ShiftDTO resumedShift = shiftService.resumePausedShift(interruptedShift.getShiftId(), currentUser.getUserId());
                userSessionService.setActiveShift(resumedShift);
                showGenericInfoAlert("Shift Resumed", MessageProvider.getString("shift.success.resumed", String.valueOf(resumedShift.getShiftId())));
            } else if (choice == InterruptedShiftDialogController.InterruptedShiftAction.FORCIBLY_END) {
                showEndShiftDialog(interruptedShift, true); // true for forced end
            } else { // CANCEL or dialog closed
                logger.info("User chose to decide later or closed interrupted shift dialog. Logging out.");
                // AppLauncher.showLoginScreen(); // This would be the ideal way to logout
                Platform.exit(); // For now, simple exit if user cancels handling of interrupted shift
            }
        } catch (IOException e) {
            logger.error("Failed to load InterruptedShiftDialog.fxml: {}", e.getMessage(), e);
            showGenericErrorAlert("UI Error", "Could not open the interrupted shift dialog.");
        } catch (ShiftException e) {
            logger.error("Error handling interrupted shift: {}", e.getMessage(), e);
            showGenericErrorAlert("Shift Error", e.getMessage());
        } finally {
            updateShiftStatusDisplayAndControls();
        }
    }

    private void showEndShiftDialog(ShiftDTO shiftToEnd, boolean isForcedEnd) {
        try {
            UserDTO currentUser = userSessionService.getCurrentUser();
            if (currentUser == null) {
                showGenericErrorAlert("Error", "No user context to end shift."); return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/EndShiftDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();
            EndShiftDialogController dialogController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getStage());
            dialogController.initializeDialog(shiftToEnd, dialogStage, isForcedEnd);

            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (dialogController.isSaved()) {
                javafx.util.Pair<BigDecimal, String> result = dialogController.getResult();
                shiftService.endShift(shiftToEnd.getShiftId(), currentUser.getUserId(), result.getKey(), result.getValue());
                userSessionService.clearActiveShift();
                showGenericInfoAlert("Shift Ended", MessageProvider.getString("shift.success.ended", String.valueOf(shiftToEnd.getShiftId())));
            }
        } catch (IOException e) {
            logger.error("Failed to load EndShiftDialog.fxml: {}", e.getMessage(), e);
            showGenericErrorAlert("UI Error", "Could not open the end shift form.");
        } catch (Exception e) { // Catch Validation, ShiftOperation, ShiftException
            logger.error("Error ending shift: {}", e.getMessage(), e);
            showGenericErrorAlert(isForcedEnd ? MessageProvider.getString("endshiftdialog.title.forced") : MessageProvider.getString("endshiftdialog.title"), e.getMessage());
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

    // --- Placeholder Handlers for New Menu Items ---

    @FXML
    private void handleFileNewPlaceholder(ActionEvent event) {
        logger.info("File -> New Placeholder selected. Action not yet implemented.");
        // Future: Open a new document/entry, etc.
    }

    @FXML
    private void handleFileOpenPlaceholder(ActionEvent event) {
        logger.info("File -> Open Placeholder selected. Action not yet implemented.");
        // Future: Show file chooser to open a document
    }

    @FXML
    private void handleFileSettings(ActionEvent event) {
        logger.info("File -> Settings selected. Action not yet implemented.");
        // Future: Open application settings dialog/view
    }

    @FXML
    private void handleEditUndo(ActionEvent event) {
        logger.info("Edit -> Undo selected. Action not yet implemented.");
    }

    @FXML
    private void handleEditRedo(ActionEvent event) {
        logger.info("Edit -> Redo selected. Action not yet implemented.");
    }

    @FXML
    private void handleEditCut(ActionEvent event) {
        logger.info("Edit -> Cut selected. Action not yet implemented.");
    }

    @FXML
    private void handleEditCopy(ActionEvent event) {
        logger.info("Edit -> Copy selected. Action not yet implemented.");
    }

    @FXML
    private void handleEditPaste(ActionEvent event) {
        logger.info("Edit -> Paste selected. Action not yet implemented.");
    }

    @FXML
    private void handleViewZoomIn(ActionEvent event) {
        logger.info("View -> Zoom In selected. Action not yet implemented.");
    }

    @FXML
    private void handleViewZoomOut(ActionEvent event) {
        logger.info("View -> Zoom Out selected. Action not yet implemented.");
    }

    @FXML
    private void handleViewResetZoom(ActionEvent event) {
        logger.info("View -> Reset Zoom selected. Action not yet implemented.");
    }

    @FXML
    private void handlePatientNewOpticalRx(ActionEvent event) {
        logger.info("Patient -> New Optical Rx selected. Action not yet implemented.");
        // Future: Open a form to create a new optical prescription, possibly for the current/selected patient
    }

    @FXML
    private void handleSalesNewSale(ActionEvent event) {
        logger.info("Sales -> New Sale selected. Action not yet implemented.");
        // Future: Open the main sales transaction interface
    }

    @FXML
    private void handleSalesViewSales(ActionEvent event) {
        logger.info("Sales -> View Sales selected. Action not yet implemented.");
        // Future: Open a view to browse/search past sales
    }

    @FXML
    private void handleSalesNewReturn(ActionEvent event) {
        logger.info("Sales -> New Return selected. Action not yet implemented.");
        // Future: Open interface for processing customer returns
    }

    // handleProductManagementAction is existing and retained

    @FXML
    private void handleInventoryItemManagementAction(ActionEvent event) {
        logger.info("Inventory -> Manage Stock (Inventory Items) selected. Loading view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/InventoryManagementView.fxml", "menu.inventory.manageStock");
    }

    @FXML
    private void handlePurchaseOrderManagementAction(ActionEvent event) {
        logger.info("Inventory -> Purchase Orders selected. Loading view...");
        loadViewIntoCenter("/com/basariatpos/ui/view/PurchaseOrderListView.fxml", "menu.inventory.purchaseOrders");
    }

    @FXML
    private void handleStockAdjustmentAction(ActionEvent event) {
        logger.info("Inventory -> Stock Adjustments selected. Action not yet implemented / Or loading view if available.");
        // This was linked to handleInventoryStockAdjustments in the new FXML.
        // If a dialog exists (as per prior sprints), it should be launched here.
        // For now, keeping as a placeholder, will be implemented if a stock adjustment dialog is part of the system.
        // Based on Sprint 2, item 20, a StockAdjustmentDialog.fxml exists.
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/StockAdjustmentDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();
            StockAdjustmentDialogController dialogController = loader.getController();
            // dialogController.setServices(...); // If it needs services directly

            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("stockadjustment.dialog.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getStage());
            dialogStage.setScene(new Scene(dialogRoot));
            // dialogController.setDialogStage(dialogStage); // If needed by controller

            dialogStage.showAndWait();
            // Process result if any
        } catch (IOException e) {
            logger.error("Failed to load StockAdjustmentDialog.fxml: {}", e.getMessage(), e);
            showGenericErrorAlert("UI Error", "Could not open the stock adjustment dialog.");
        }
    }


    @FXML
    private void handleReportsSalesReport(ActionEvent event) {
        logger.info("Reports -> Sales Report selected. Action not yet implemented.");
    }

    @FXML
    private void handleReportsInventoryReport(ActionEvent event) {
        logger.info("Reports -> Inventory Report selected. Action not yet implemented.");
    }

    @FXML
    private void handleReportsFinancialReport(ActionEvent event) {
        logger.info("Reports -> Financial Report selected. Action not yet implemented.");
    }

    // handleUserManagementAction is existing and retained
    // handleEditCenterProfile is existing and retained
    // handleAppSettingsManagementAction is existing and retained
    // handleBankNameManagementAction is existing and retained
    // handleExpenseCategoryManagementAction is existing and retained
    // handleProductCategoryManagementAction is existing and retained

    @FXML
    private void handleAdminDbManagement(ActionEvent event) {
        logger.info("Admin -> DB Management selected. Action not yet implemented.");
        // Future: Open a database management/backup utility or view
    }

    // handleViewUserManual is existing and retained
    // handleHelpAbout is existing and retained
}
