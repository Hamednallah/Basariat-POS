package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher; // For service access and reloading scene
import com.basariatpos.model.ShiftDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.*; // Import all services
import com.basariatpos.ui.theme.AppTheme;
import com.basariatpos.util.AppLogger; // Assuming AppLogger exists
import com.basariatpos.util.DesktopActions; // For opening user manual

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.web.WebView;


import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.slf4j.Logger; // Using SLF4J via AppLogger pattern

public class MainFrameController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(MainFrameController.class);

    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane mainContentArea;
    @FXML private Label shiftStatusLabel;
    @FXML private Label welcomeLabel;
    @FXML private MenuBar menuBar;

    //<editor-fold desc="Menu FXML Fields">
    @FXML private Menu menuFile;
    @FXML private Menu menuFileLanguage;
    @FXML private MenuItem menuItemEnglish;
    @FXML private MenuItem menuItemArabic;
    @FXML private MenuItem menuFileExit;
    @FXML private Menu menuPatient;
    @FXML private MenuItem patientManagementMenuItem;
    @FXML private Menu menuSales;
    @FXML private MenuItem menuItemSalesOrderList;
    @FXML private Menu menuInventory;
    @FXML private MenuItem productManagementMenuItem;
    @FXML private MenuItem inventoryItemManagementMenuItem;
    @FXML private MenuItem purchaseOrderManagementMenuItem;
    @FXML private MenuItem stockAdjustmentMenuItem;
    @FXML private Menu menuAdmin;
    @FXML private MenuItem userManagementMenuItem;
    @FXML private MenuItem editCenterProfileMenuItem;
    @FXML private MenuItem bankNameManagementMenuItem;
    @FXML private MenuItem expenseCategoryManagementMenuItem;
    @FXML private MenuItem manageExpensesMenuItem; // Added for Manage Expenses
    @FXML private MenuItem productCategoryManagementMenuItem;
    @FXML private MenuItem appSettingsManagementMenuItem;
    @FXML private MenuItem viewAuditLogsMenuItem;
    @FXML private Menu menuHelp;
    @FXML private MenuItem viewUserManualMenuItem;
    @FXML private MenuItem menuHelpAbout;

    // New MenuItems from Sales Menu
    @FXML private MenuItem menuItemSalesNewSale;
    @FXML private MenuItem menuItemManageAppointments;

    // New Menu and MenuItems from Reports Menu
    @FXML private Menu menuReports;
    @FXML private MenuItem menuItemSalesReport;
    @FXML private MenuItem menuItemPLStatement;
    @FXML private MenuItem menuItemExpenseReport;
    @FXML private MenuItem menuItemInventoryValuationReport;
    @FXML private MenuItem menuItemLowStockReport;
    @FXML private MenuItem menuItemOutstandingPaymentsReport;
    @FXML private MenuItem menuItemPatientPurchaseHistory;
    @FXML private MenuItem menuItemShiftReport;

    //</editor-fold>

    //<editor-fold desc="Status Bar FXML Fields">
    @FXML private HBox statusBar;
    @FXML private Button startShiftButton;
    @FXML private Button pauseShiftButton;
    @FXML private Button resumeShiftButton;
    //</editor-fold>

    //<editor-fold desc="Service Fields">
    private UserSessionService userSessionService;
    private ShiftService shiftService;
    private ApplicationSettingsService applicationSettingsService;
    private CenterProfileService centerProfileService;
    private ProductCategoryService productCategoryService;
    private ProductService productService;
    private InventoryItemService inventoryItemService;
    private SalesOrderService salesOrderService;
    private PurchaseOrderService purchaseOrderService;
    private PatientService patientService;
    private OpticalDiagnosticService opticalDiagnosticService; // Often coupled with PatientService
    private BankNameService bankNameService;
    private ExpenseCategoryService expenseCategoryService;
    private ExpenseService expenseService; // Added for Expense Management
    private AuditLogService auditLogService; // For View Audit Logs
    private WhatsAppNotificationService whatsAppNotificationService; // If needed by loaded views from here
    private PaymentService paymentService; // If needed by loaded views from here
    //</editor-fold>

    // Called by AppLauncher after FXML loading and controller instantiation
    public void setServicesAndLoadInitialData(
            UserSessionService userSessionService, ShiftService shiftService,
            ApplicationSettingsService applicationSettingsService, CenterProfileService centerProfileService,
            ProductCategoryService productCategoryService, ProductService productService,
            InventoryItemService inventoryItemService, SalesOrderService salesOrderService,
            PurchaseOrderService purchaseOrderService, PatientService patientService,
            OpticalDiagnosticService opticalDiagnosticService, BankNameService bankNameService,
            ExpenseCategoryService expenseCategoryService, ExpenseService expenseService, // Added expenseService
            AuditLogService auditLogService,
            WhatsAppNotificationService whatsAppNotificationService, PaymentService paymentService) {

        this.userSessionService = userSessionService;
        this.shiftService = shiftService;
        this.applicationSettingsService = applicationSettingsService;
        this.centerProfileService = centerProfileService;
        this.productCategoryService = productCategoryService;
        this.productService = productService;
        this.inventoryItemService = inventoryItemService;
        this.salesOrderService = salesOrderService;
        this.purchaseOrderService = purchaseOrderService;
        this.patientService = patientService;
        this.opticalDiagnosticService = opticalDiagnosticService;
        this.bankNameService = bankNameService;
        this.expenseCategoryService = expenseCategoryService;
        this.expenseService = expenseService; // Assign expenseService
        this.auditLogService = auditLogService;
        this.whatsAppNotificationService = whatsAppNotificationService;
        this.paymentService = paymentService;


        // Apply theme once services are available, especially if theme comes from settings
        if (mainBorderPane.getScene() != null) {
            AppTheme.getInstance().applyCurrentTheme(mainBorderPane.getScene());
        } else {
            // Scene might not be available immediately if this is called too early.
            // Consider applying theme in initialize or after scene is definitely set.
            mainBorderPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    AppTheme.getInstance().applyCurrentTheme(newScene);
                }
            });
            logger.warn("Scene was null when trying to apply theme in setServicesAndLoadInitialData. Added listener.");
        }

        LocaleManager.addLocaleChangeListener((oldLocale, newLocale) -> {
            logger.info("Locale changed from {} to {}, refreshing UI.", oldLocale, newLocale);
            updateNodeOrientations(newLocale);
            refreshMenuTexts(); // Refresh texts before reloading scene for them to be picked up by FXML loader
            reloadScene(); // Reloading the scene will re-initialize controllers and FXMLs with new resource bundle
        });

        updateNodeOrientations(LocaleManager.getCurrentLocale());
        refreshMenuTexts(); // Initial text setup
        updateShiftStatusDisplayAndControls();
        logger.info("MainFrameController services set and initial data loaded.");
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("MainFrameController FXML components initialized.");
        // Set initial node orientation based on current locale
        updateNodeOrientations(LocaleManager.getCurrentLocale());

        pauseShiftButton.setVisible(false); pauseShiftButton.setManaged(false);
        resumeShiftButton.setVisible(false); resumeShiftButton.setManaged(false);
    }

    private void updateNodeOrientations(Locale locale) {
        if (mainBorderPane == null || menuBar == null || statusBar == null || mainContentArea == null) {
            logger.warn("Attempted to update node orientations before FXML components were fully initialized.");
            return;
        }
        if (LocaleManager.ARABIC.equals(locale)) {
            mainBorderPane.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            menuBar.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            statusBar.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
            mainContentArea.setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);
        } else {
            mainBorderPane.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            menuBar.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            statusBar.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
            mainContentArea.setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
        }
        logger.debug("Node orientations updated for locale: {}", locale);
    }

    private void refreshMenuTexts() {
        ResourceBundle bundle = MessageProvider.getBundle();
        // Ensure FXML elements are injected before trying to set text
        if (menuFile == null) {
            logger.warn("refreshMenuTexts called before FXML injection complete. Skipping.");
            return;
        }
        menuFile.setText(bundle.getString("menu.file"));
        menuFileLanguage.setText(bundle.getString("menu.file.language"));
        menuItemEnglish.setText(bundle.getString("menu.file.language.english"));
        menuItemArabic.setText(bundle.getString("menu.file.language.arabic"));
        menuFileExit.setText(bundle.getString("menu.file.exit"));
        menuPatient.setText(bundle.getString("menu.patient"));
        patientManagementMenuItem.setText(bundle.getString("menu.patient.manage"));
        menuSales.setText(bundle.getString("menu.sales"));
        menuItemSalesOrderList.setText(bundle.getString("salesorder.list.title"));
        menuInventory.setText(bundle.getString("menu.inventory"));
        productManagementMenuItem.setText(bundle.getString("menu.inventory.manageProducts"));
        inventoryItemManagementMenuItem.setText(bundle.getString("menu.inventory.manageStock"));
        purchaseOrderManagementMenuItem.setText(bundle.getString("menu.inventory.purchaseOrders"));
        stockAdjustmentMenuItem.setText(bundle.getString("menu.inventory.stockAdjustments"));
        menuAdmin.setText(bundle.getString("menu.admin"));
        userManagementMenuItem.setText(bundle.getString("menu.admin.userManagement"));
        editCenterProfileMenuItem.setText(bundle.getString("menu.admin.centerProfile"));
        bankNameManagementMenuItem.setText(bundle.getString("bankname.management.title"));
        expenseCategoryManagementMenuItem.setText(bundle.getString("expensecategory.management.title"));
        if (manageExpensesMenuItem != null) manageExpensesMenuItem.setText(bundle.getString("menu.admin.manageExpenses")); // Added
        productCategoryManagementMenuItem.setText(bundle.getString("productcategory.management.title"));
        appSettingsManagementMenuItem.setText(bundle.getString("menu.admin.applicationSettings"));
        viewAuditLogsMenuItem.setText(bundle.getString("menu.admin.viewAuditLogs"));
        menuHelp.setText(bundle.getString("menu.help"));
        viewUserManualMenuItem.setText(bundle.getString("menu.help.viewUserManual"));
        menuHelpAbout.setText(bundle.getString("menu.help.about"));

        // Refresh texts for new Sales menu items
        if (menuItemSalesNewSale != null) menuItemSalesNewSale.setText(bundle.getString("menu.sales.newSale"));
        if (menuItemManageAppointments != null) menuItemManageAppointments.setText(bundle.getString("menu.sales.manageAppointments"));

        // Refresh texts for new Reports menu and items
        if (menuReports != null) menuReports.setText(bundle.getString("menu.reports"));
        if (menuItemSalesReport != null) menuItemSalesReport.setText(bundle.getString("menu.reports.sales"));
        if (menuItemPLStatement != null) menuItemPLStatement.setText(bundle.getString("menu.reports.plStatement"));
        if (menuItemExpenseReport != null) menuItemExpenseReport.setText(bundle.getString("menu.reports.expense"));
        if (menuItemInventoryValuationReport != null) menuItemInventoryValuationReport.setText(bundle.getString("menu.reports.inventoryValuation"));
        if (menuItemLowStockReport != null) menuItemLowStockReport.setText(bundle.getString("menu.reports.lowStock"));
        if (menuItemOutstandingPaymentsReport != null) menuItemOutstandingPaymentsReport.setText(bundle.getString("menu.reports.outstandingPayments"));
        if (menuItemPatientPurchaseHistory != null) menuItemPatientPurchaseHistory.setText(bundle.getString("menu.reports.patientPurchaseHistory"));
        if (menuItemShiftReport != null) menuItemShiftReport.setText(bundle.getString("menu.reports.shift"));


        if(welcomeLabel != null) welcomeLabel.setText(bundle.getString("label.welcomeToBasariatPOS"));
        if(startShiftButton != null) startShiftButton.setText(bundle.getString("mainframe.button.startShift"));
        if(pauseShiftButton != null) pauseShiftButton.setText(bundle.getString("mainframe.button.pauseShift"));
        if(resumeShiftButton != null) resumeShiftButton.setText(bundle.getString("mainframe.button.resumeShift"));

        if(shiftStatusLabel != null) shiftStatusLabel.setText(bundle.getString("label.shiftStatusNotActive")); // Default text
        logger.debug("Menu and UI texts refreshed for locale: {}", LocaleManager.getCurrentLocale());
    }

    public void updateShiftStatusDisplayAndControls() {
        if (userSessionService == null || shiftService == null || shiftStatusLabel == null || startShiftButton == null || pauseShiftButton == null || resumeShiftButton == null) {
            logger.warn("Services or FXML elements for shift status are not yet initialized. Skipping update.");
            if(shiftStatusLabel != null && MessageProvider.getBundle() != null) shiftStatusLabel.setText(MessageProvider.getString("label.shiftStatusLoading"));
            if(startShiftButton != null) startShiftButton.setDisable(true);
            if(pauseShiftButton != null) {pauseShiftButton.setDisable(true); pauseShiftButton.setVisible(false); pauseShiftButton.setManaged(false);}
            if(resumeShiftButton != null) {resumeShiftButton.setDisable(true); resumeShiftButton.setVisible(false); resumeShiftButton.setManaged(false);}
            return;
        }
        UserDTO currentUser = userSessionService.getCurrentUser();
        if (currentUser == null) {
            shiftStatusLabel.setText(MessageProvider.getString("mainframe.shiftstatus.loggedOut"));
            startShiftButton.setDisable(true); startShiftButton.setVisible(true); startShiftButton.setManaged(true);
            pauseShiftButton.setDisable(true); pauseShiftButton.setVisible(false); pauseShiftButton.setManaged(false);
            resumeShiftButton.setDisable(true); resumeShiftButton.setVisible(false); resumeShiftButton.setManaged(false);
            return;
        }
        try {
            Optional<ShiftDTO> shiftOpt = shiftService.getIncompleteShiftForUser(currentUser.getUserId());
            if (shiftOpt.isPresent()) {
                ShiftDTO currentShift = shiftOpt.get();
                userSessionService.setActiveShift(currentShift);
                String statusKey; String shiftUserDisplay = currentShift.getStartedByUsername() != null ? currentShift.getStartedByUsername() : currentUser.getUsername();
                boolean isActive = "Active".equalsIgnoreCase(currentShift.getStatus());
                boolean isPaused = "Paused".equalsIgnoreCase(currentShift.getStatus());
                boolean isInterrupted = "Interrupted".equalsIgnoreCase(currentShift.getStatus());
                if (isActive) statusKey = "mainframe.shiftstatus.active";
                else if (isPaused) statusKey = "mainframe.shiftstatus.paused";
                else if (isInterrupted) statusKey = "mainframe.shiftstatus.interrupted";
                else statusKey = "mainframe.shiftstatus.unknown";
                shiftStatusLabel.setText(MessageProvider.getString(statusKey, String.valueOf(currentShift.getShiftId()), shiftUserDisplay));
                startShiftButton.setDisable(true); startShiftButton.setVisible(false); startShiftButton.setManaged(false);
                pauseShiftButton.setDisable(!isActive); pauseShiftButton.setVisible(isActive); pauseShiftButton.setManaged(isActive);
                resumeShiftButton.setDisable(!(isPaused || isInterrupted)); resumeShiftButton.setVisible(isPaused || isInterrupted); resumeShiftButton.setManaged(isPaused || isInterrupted);
            } else {
                userSessionService.clearActiveShift();
                shiftStatusLabel.setText(MessageProvider.getString("mainframe.shiftstatus.noActiveShift"));
                startShiftButton.setDisable(false); startShiftButton.setVisible(true); startShiftButton.setManaged(true);
                pauseShiftButton.setDisable(true); pauseShiftButton.setVisible(false); pauseShiftButton.setManaged(false);
                resumeShiftButton.setDisable(true); resumeShiftButton.setVisible(false); resumeShiftButton.setManaged(false);
            }
        } catch (ShiftException e) {
            logger.error("Error updating shift status: {}", e.getMessage(), e);
            shiftStatusLabel.setText(MessageProvider.getString("mainframe.shiftstatus.error"));
            startShiftButton.setDisable(true);
            pauseShiftButton.setDisable(true); pauseShiftButton.setVisible(false); pauseShiftButton.setManaged(false);
            resumeShiftButton.setDisable(true); resumeShiftButton.setVisible(false); resumeShiftButton.setManaged(false);
        }
    }

    public void handleInterruptedShift(ShiftDTO interruptedShift) {
        logger.info("Handling interrupted shift ID: {}", interruptedShift.getShiftId());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/InterruptedShiftDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();
            InterruptedShiftDialogController dialogController = loader.getController();
            dialogController.setShiftService(shiftService);
            dialogController.setUserSessionService(userSessionService);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("interruptedshift.dialog.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getStage());
            dialogController.initializeDialog(interruptedShift, dialogStage);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
            InterruptedShiftDialogController.InterruptedShiftAction choice = dialogController.getResult();
            UserDTO currentUser = userSessionService.getCurrentUser();
            if (choice == InterruptedShiftDialogController.InterruptedShiftAction.RESUME) {
                ShiftDTO resumedShift = shiftService.resumePausedShift(interruptedShift.getShiftId(), currentUser.getUserId());
                userSessionService.setActiveShift(resumedShift);
                showGenericInfoAlert("Shift Resumed", MessageProvider.getString("shift.success.resumed", String.valueOf(resumedShift.getShiftId())));
            } else if (choice == InterruptedShiftDialogController.InterruptedShiftAction.FORCIBLY_END) {
                showEndShiftDialog(interruptedShift, true);
            } else {
                logger.warn("User chose to decide later or closed interrupted shift dialog. Application will exit for safety.");
                Platform.exit();
            }
        } catch (Exception e) {
            logger.error("Error handling interrupted shift: {}", e.getMessage(), e);
            showGenericErrorAlert("Shift Error", "Failed to handle interrupted shift: " + e.getMessage() + "\nApplication will exit.");
            Platform.exit();
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
            dialogController.setShiftService(this.shiftService);
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
        } catch (Exception e) {
            logger.error("Error during end shift process: {}", e.getMessage(), e);
            showGenericErrorAlert(isForcedEnd ? MessageProvider.getString("endshiftdialog.title.forced") : MessageProvider.getString("endshiftdialog.title"), e.getMessage());
        } finally {
            updateShiftStatusDisplayAndControls();
        }
    }

    private void reloadScene() {
        try {
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            AppLauncher.reloadMainScene(stage);
            logger.info("Main scene reload requested via AppLauncher for locale: {}", LocaleManager.getCurrentLocale());
        } catch (Exception e) {
            logger.error("Failed to request main scene reload from AppLauncher.", e);
            showGenericErrorAlert("Error", "Failed to apply language change. Please restart the application.");
        }
    }

    //<editor-fold desc="Menu Action Handlers">
    @FXML private void handleSwitchToEnglish(ActionEvent event) { LocaleManager.setCurrentLocale(Locale.ENGLISH); }
    @FXML private void handleSwitchToArabic(ActionEvent event) { LocaleManager.setCurrentLocale(LocaleManager.ARABIC); }
    @FXML private void handleFileExit(ActionEvent event) { Platform.exit(); }

    @FXML
    private void handleHelpAboutAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/AboutDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent aboutDialogRoot = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("about.dialog.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getStage());
            dialogStage.setScene(new Scene(aboutDialogRoot));
            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.error("Failed to load AboutDialog.fxml", e);
            showGenericErrorAlert("Error", "Could not open About Dialog.");
        }
    }

    @FXML
    private void handleViewUserManual(ActionEvent event) {
        String langCode = LocaleManager.getCurrentLocale().getLanguage();
        String manualFile = "/com/basariatpos/help/UserManual_" + langCode + ".html";
        URL manualUrl = getClass().getResource(manualFile);
        if (manualUrl == null && !Locale.ENGLISH.getLanguage().equals(langCode)) {
            manualFile = "/com/basariatpos/help/UserManual_en.html";
            manualUrl = getClass().getResource(manualFile);
        }
        if (manualUrl != null) {
            DesktopActions.openUrl(manualUrl.toExternalForm(), getStage());
        } else {
            logger.error("User manual not found: {}", manualFile);
            showGenericErrorAlert("Error", "User manual file not found.");
        }
    }

    private <T> void loadViewIntoCenter(String fxmlPath, String titleKey, Consumer<T> controllerInitializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setResources(MessageProvider.getBundle());
            Parent viewRoot = loader.load();
            if (controllerInitializer != null) {
                T loadedController = loader.getController();
                if (loadedController != null) {
                    controllerInitializer.accept(loadedController);
                } else {
                    logger.warn("Controller not found for FXML path: {}", fxmlPath);
                }
            }
            mainContentArea.getChildren().setAll(viewRoot);
            if (getStage() != null) {
                getStage().setTitle(MessageProvider.getString("app.title") + " - " + MessageProvider.getString(titleKey));
            }
             if (welcomeLabel != null) welcomeLabel.setVisible(false);
        } catch (IOException e) {
            logger.error("Failed to load view: {} - {}", fxmlPath, e.getMessage(), e);
            showGenericErrorAlert("Error Loading View", "Could not open: " + MessageProvider.getString(titleKey) + "\n" + e.getMessage());
            if (welcomeLabel != null) welcomeLabel.setVisible(true);
        }
    }

    @FXML private void handlePatientManagementAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/PatientManagementView.fxml", "patientmanagement.title",
            (PatientManagementController c) -> {
                c.setPatientService(this.patientService);
                c.setOpticalDiagnosticService(this.opticalDiagnosticService);
                c.setStage(getStage());
                c.loadInitialData();
            });
    }

    @FXML private void handleSalesOrderListAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/SalesOrderListView.fxml", "salesorder.list.title",
            (SalesOrderListController c) -> {
                c.setSalesOrderService(this.salesOrderService);
                c.setPatientService(this.patientService);
                c.setWhatsAppNotificationService(this.whatsAppNotificationService);
                c.setPaymentService(this.paymentService);
                c.setCenterProfileService(this.centerProfileService);
                c.setCurrentStage(getStage());
                c.loadInitialData();
            });
    }

    @FXML private void handleProductManagementAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/ProductManagementView.fxml", "product.management.title",
            (ProductManagementController c) -> {
                c.setProductService(this.productService);
                c.setProductCategoryService(this.productCategoryService);
                c.setStage(getStage());
                c.loadInitialData();
            });
    }

    @FXML private void handleInventoryItemManagementAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/InventoryManagementView.fxml", "inventoryitem.management.title",
            (InventoryItemManagementController c) -> {
                c.setInventoryItemService(this.inventoryItemService);
                c.setProductService(this.productService);
                c.setStage(getStage());
                c.loadInitialData();
            });
    }

    @FXML private void handlePurchaseOrderManagementAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/PurchaseOrderListView.fxml", "purchaseorder.list.title",
            (PurchaseOrderListController c) -> {
                c.setPurchaseOrderService(this.purchaseOrderService);
                c.setProductService(this.productService);
                c.setStage(getStage());
                c.loadInitialData();
            });
    }

    @FXML private void handleStockAdjustmentAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/StockAdjustmentDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();
            StockAdjustmentDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("stockadjustment.dialog.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getStage());
            controller.setServices(this.inventoryItemService, this.userSessionService, this.auditLogService);
            controller.setDialogStage(dialogStage);
            controller.loadInventoryItems();
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.error("Failed to load StockAdjustmentDialog.fxml", e);
            showGenericErrorAlert("Error", "Could not open Stock Adjustment Dialog.");
        }
    }

    @FXML private void handleUserManagementAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/UserManagementView.fxml", "usermanagement.title",
            (UserManagementController c) -> {
                c.setUserService(AppLauncher.getUserService());
                c.setStage(getStage());
                c.loadInitialData();
            });
    }

    @FXML private void handleEditCenterProfileAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/CenterProfileEditorView.fxml", "centerprofile.editor.title",
            (CenterProfileEditorController c) -> c.setCenterProfileService(this.centerProfileService));
    }

    @FXML private void handleBankNameManagementAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/BankNameManagementView.fxml", "bankname.management.title",
            (BankNameManagementController c) -> {
                c.setBankNameService(this.bankNameService);
                c.setStage(getStage());
                c.loadBankNames();
            });
    }

    @FXML private void handleExpenseCategoryManagementAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/ExpenseCategoryManagementView.fxml", "expensecategory.management.title",
            (ExpenseCategoryManagementController c) -> {
                c.setExpenseCategoryService(this.expenseCategoryService);
                c.setStage(getStage());
                c.loadExpenseCategories();
            });
    }

    @FXML private void handleManageExpensesAction(ActionEvent event) { // Added handler
        loadViewIntoCenter("/com/basariatpos/ui/view/ExpenseManagementView.fxml", "expensemanagement.title",
            (ExpenseManagementController c) -> {
                c.setServices(this.expenseService, this.expenseCategoryService, this.bankNameService);
            });
    }

    @FXML private void handleProductCategoryManagementAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/ProductCategoryManagementView.fxml", "productcategory.management.title",
            (ProductCategoryManagementController c) -> {
                c.setProductCategoryService(this.productCategoryService);
                c.setStage(getStage());
                c.loadProductCategories();
            });
    }

    @FXML private void handleAppSettingsManagementAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/AppSettingsManagementView.fxml", "appsettings.management.title",
            (AppSettingsManagementController c) -> c.setApplicationSettingsService(this.applicationSettingsService));
    }

    @FXML private void handleViewAuditLogsAction(ActionEvent event) {
        loadViewIntoCenter("/com/basariatpos/ui/view/AuditLogView.fxml", "auditlog.view.title",
            (AuditLogViewController c) -> {
                c.setAuditLogService(this.auditLogService);
                c.loadInitialData();
            });
    }
    //</editor-fold>

    //<editor-fold desc="Shift Action Handlers">
    @FXML
    private void handleStartShiftAction(ActionEvent event) {
        UserDTO currentUser = userSessionService.getCurrentUser();
        if (currentUser == null) { showGenericErrorAlert("Error", MessageProvider.getString("shift.error.noUser")); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/StartShiftDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();
            StartShiftDialogController dialogController = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("startshiftdialog.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(getStage());
            dialogController.setDialogStage(dialogStage);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
            if (dialogController.isSaved()) {
                ShiftDTO startedShift = shiftService.startNewShift(currentUser.getUserId(), dialogController.getOpeningFloat());
                userSessionService.setActiveShift(startedShift);
                showGenericInfoAlert("Shift Started", MessageProvider.getString("shift.success.started", String.valueOf(startedShift.getShiftId())));
            }
        } catch (Exception e) {
            logger.error("Error starting shift: {}", e.getMessage(), e);
            showGenericErrorAlert("Start Shift Error", e.getMessage());
        } finally {
            updateShiftStatusDisplayAndControls();
        }
    }

    @FXML
    private void handlePauseShiftAction(ActionEvent event) {
        ShiftDTO activeShift = userSessionService.getActiveShift();
        UserDTO currentUser = userSessionService.getCurrentUser();
        if (activeShift == null || currentUser == null || !"Active".equalsIgnoreCase(activeShift.getStatus())) {
            showGenericErrorAlert("Error", MessageProvider.getString("shift.error.notActiveToPause")); return;
        }
        try {
            shiftService.pauseActiveShift(activeShift.getShiftId(), currentUser.getUserId());
            showGenericInfoAlert("Shift Paused", MessageProvider.getString("shift.success.paused", String.valueOf(activeShift.getShiftId())));
        } catch (ShiftException e) {
            logger.error("Error pausing shift: {}", e.getMessage(), e);
            showGenericErrorAlert("Pause Shift Error", e.getMessage());
        } finally {
            updateShiftStatusDisplayAndControls();
        }
    }

    @FXML
    private void handleResumeShiftAction(ActionEvent event) {
        UserDTO currentUser = userSessionService.getCurrentUser();
        if (currentUser == null) { showGenericErrorAlert("Error", MessageProvider.getString("shift.error.noUser")); return; }
        Optional<ShiftDTO> shiftToResumeOpt;
        try {
            shiftToResumeOpt = shiftService.getIncompleteShiftForUser(currentUser.getUserId());
            if (shiftToResumeOpt.isEmpty() || !("Paused".equalsIgnoreCase(shiftToResumeOpt.get().getStatus()) || "Interrupted".equalsIgnoreCase(shiftToResumeOpt.get().getStatus()))) {
                 showGenericErrorAlert("Error", MessageProvider.getString("shift.error.notPausedToResume")); return;
            }
            ShiftDTO resumedShift = shiftService.resumePausedShift(shiftToResumeOpt.get().getShiftId(), currentUser.getUserId());
            showGenericInfoAlert("Shift Resumed", MessageProvider.getString("shift.success.resumed", String.valueOf(resumedShift.getShiftId())));
        } catch (ShiftException e) {
            logger.error("Error resuming shift: {}", e.getMessage(), e);
            showGenericErrorAlert("Resume Shift Error", e.getMessage());
        } finally {
            updateShiftStatusDisplayAndControls();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Utility Methods (Alerts, Stage)">
    private void showGenericErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(getStage());
        alert.getDialogPane().setNodeOrientation(mainBorderPane.getNodeOrientation()); // Apply RTL/LTR to dialog
        alert.showAndWait();
    }

    private void showGenericInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(getStage());
        alert.getDialogPane().setNodeOrientation(mainBorderPane.getNodeOrientation()); // Apply RTL/LTR to dialog
        alert.showAndWait();
    }

    private Stage getStage() {
        if (mainBorderPane != null && mainBorderPane.getScene() != null) {
            return (Stage) mainBorderPane.getScene().getWindow();
        }
        logger.warn("Could not get stage from mainBorderPane.");
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Placeholder Action Handlers for New MenuItems">
    @FXML private void handleNewSalesOrderAction(ActionEvent event) {
        logger.info("New Sales Order action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "New Sales Order functionality not yet implemented.");
        // Example of loading a view:
        // loadViewIntoCenter("/com/basariatpos/ui/view/NewSalesOrderView.fxml", "newsalesorder.title", (NewSalesOrderController c) -> { /* init controller */ });
    }

    @FXML private void handleManageAppointmentsAction(ActionEvent event) {
        logger.info("Manage Appointments action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "Manage Appointments functionality not yet implemented.");
        // loadViewIntoCenter("/com/basariatpos/ui/view/AppointmentManagementView.fxml", "appointmentmanagement.title", (AppointmentManagementController c) -> { /* init controller */ });
    }

    @FXML private void handleSalesReportAction(ActionEvent event) {
        logger.info("Sales Report action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "Sales Report functionality not yet implemented.");
    }

    @FXML private void handlePLStatementAction(ActionEvent event) {
        logger.info("P&L Statement action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "P&L Statement functionality not yet implemented.");
    }

    @FXML private void handleExpenseReportAction(ActionEvent event) {
        logger.info("Expense Report action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "Expense Report functionality not yet implemented.");
    }

    @FXML private void handleInventoryValuationReportAction(ActionEvent event) {
        logger.info("Inventory Valuation Report action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "Inventory Valuation Report functionality not yet implemented.");
    }

    @FXML private void handleLowStockReportAction(ActionEvent event) {
        logger.info("Low Stock Report action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "Low Stock Report functionality not yet implemented.");
    }

    @FXML private void handleOutstandingPaymentsReportAction(ActionEvent event) {
        logger.info("Outstanding Payments Report action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "Outstanding Payments Report functionality not yet implemented.");
    }

    @FXML private void handlePatientPurchaseHistoryAction(ActionEvent event) {
        logger.info("Patient Purchase History action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "Patient Purchase History functionality not yet implemented.");
    }

    @FXML private void handleShiftReportAction(ActionEvent event) {
        logger.info("Shift Report action triggered (placeholder).");
        showGenericInfoAlert("Placeholder", "Shift Report functionality not yet implemented.");
        // This one likely opens a dialog or a dedicated view.
        // Example: loadViewIntoCenter("/com/basariatpos/ui/view/ShiftReportChooserView.fxml", "shiftreport.title", (controller) -> {});
    }
    //</editor-fold>
}
