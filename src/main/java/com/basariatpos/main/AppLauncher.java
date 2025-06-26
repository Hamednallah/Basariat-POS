package com.basariatpos.main;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.SalesOrderDTO; // Required for MainFrameController call if passing DTO
import com.basariatpos.model.ShiftDTO;    // Required for MainFrameController call
import com.basariatpos.model.UserDTO;     // Required for MainFrameController call
import com.basariatpos.repository.*;      // Import all repositories
import com.basariatpos.service.*;       // Import all services
import com.basariatpos.ui.controller.MainFrameController; // Required for Platform.runLater
import com.basariatpos.util.AppLogger;


import javafx.application.Application;
import javafx.application.Platform; // Added for Platform.runLater
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

    // Declare all services and repositories needed application-wide
    private static ApplicationSettingsRepository applicationSettingsRepository;
    private static AuditLogRepository auditLogRepository;
    private static BankNameRepository bankNameRepository;
    private static CenterProfileRepository centerProfileRepository;
    private static ExpenseCategoryRepository expenseCategoryRepository;
    private static ExpenseRepository expenseRepository; // Uncommented/Added
    private static InventoryItemRepository inventoryItemRepository;
    private static OpticalDiagnosticRepository opticalDiagnosticRepository;
    private static PatientRepository patientRepository;
    private static PaymentRepository paymentRepository; // Added
    private static ProductCategoryRepository productCategoryRepository;
    private static ProductRepository productRepository;
    private static PurchaseOrderRepository purchaseOrderRepository;
    private static SalesOrderRepository salesOrderRepository;
    private static SessionRepository sessionRepository;
    private static ShiftRepository shiftRepository;
    private static UserRepository userRepository;
    // AuditLogRepository is already declared

    private static ApplicationSettingsService applicationSettingsService;
    private static BankNameService bankNameService;
    private static CenterProfileService centerProfileService;
    private static ExpenseCategoryService expenseCategoryService;
    private static ExpenseService expenseService;
    private static InventoryItemService inventoryItemService;
    private static OpticalDiagnosticService opticalDiagnosticService;
    private static PatientService patientService;
    private static PaymentService paymentService; // Added
    private static ProductCategoryService productCategoryService;
    private static ProductService productService;
    private static PurchaseOrderService purchaseOrderService;
    private static SalesOrderService salesOrderService;
    private static ShiftService shiftService;
    private static UserService userService;
    private static UserSessionService userSessionService;
    private static WhatsAppNotificationService whatsAppNotificationService;
    private static AuditLogService auditLogService; // Added AuditLogService field

    @Override
    public void init() throws Exception {
        super.init();

        // Initialize Repositories
        applicationSettingsRepository = new ApplicationSettingsRepositoryImpl();
        auditLogRepository = new AuditLogRepositoryImpl();
        bankNameRepository = new BankNameRepositoryImpl();
        centerProfileRepository = new CenterProfileRepositoryImpl();
        expenseCategoryRepository = new ExpenseCategoryRepositoryImpl();
        expenseRepository = new ExpenseRepositoryImpl(); // Uncommented/Added
        inventoryItemRepository = new InventoryItemRepositoryImpl();
        opticalDiagnosticRepository = new OpticalDiagnosticRepositoryImpl();
        patientRepository = new PatientRepositoryImpl();
        paymentRepository = new PaymentRepositoryImpl(); // New
        productCategoryRepository = new ProductCategoryRepositoryImpl();
        productRepository = new ProductRepositoryImpl();
        purchaseOrderRepository = new PurchaseOrderRepositoryImpl();
        salesOrderRepository = new SalesOrderRepositoryImpl();
        sessionRepository = new SessionRepositoryImpl();
        shiftRepository = new ShiftRepositoryImpl();
        userRepository = new UserRepositoryImpl();

        // Initialize Services (order matters for dependencies)
        applicationSettingsService = new ApplicationSettingsServiceImpl(applicationSettingsRepository);
        userService = new UserServiceImpl(userRepository);
        userSessionService = new UserSessionService(sessionRepository, userService);
        centerProfileService = new CenterProfileService(centerProfileRepository); // Simple, no other service deps
        bankNameService = new BankNameServiceImpl(bankNameRepository);
        expenseCategoryService = new ExpenseCategoryServiceImpl(expenseCategoryRepository);
        shiftService = new ShiftServiceImpl(shiftRepository, userRepository);
        productCategoryService = new ProductCategoryServiceImpl(productCategoryRepository);
        productService = new ProductServiceImpl(productRepository, productCategoryService);

        patientService = new PatientServiceImpl(patientRepository, applicationSettingsService, userSessionService);
        opticalDiagnosticService = new OpticalDiagnosticServiceImpl(opticalDiagnosticRepository, userSessionService);
        inventoryItemService = new InventoryItemServiceImpl(inventoryItemRepository, productService, auditLogRepository, userSessionService);

        // SalesOrderService depends on several other services
        salesOrderService = new SalesOrderServiceImpl(
            salesOrderRepository, userSessionService, inventoryItemService,
            productService, patientService
        );
        auditLogService = new AuditLogServiceImpl(auditLogRepository); // Initialize AuditLogService

        // PaymentService depends on SalesOrderService
        paymentService = new PaymentServiceImpl(
            paymentRepository, userSessionService, salesOrderService
        );

        // ExpenseService depends on ExpenseRepository, UserSessionService, ExpenseCategoryService, BankNameService
        expenseService = new ExpenseServiceImpl(
            expenseRepository,
            userSessionService,
            expenseCategoryService,
            bankNameService
        );

        // WhatsAppNotificationService (assuming LocaleManager is a singleton or accessible statically)
        whatsAppNotificationService = new WhatsAppNotificationServiceImpl(
            applicationSettingsService,
            LocaleManager.getInstance(), // Assuming a static getInstance() or pass it if it's managed
            centerProfileService
        );

        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        logger.info("Application initializing. Default locale set to: {}", LocaleManager.getCurrentLocale().toLanguageTag());
    }

    // Static Getters for all services
    public static ApplicationSettingsService getApplicationSettingsService() { return applicationSettingsService; }
    public static AuditLogService getAuditLogService() { return auditLogService; } // Added getter for AuditLogService
    public static BankNameService getBankNameService() { return bankNameService; }
    public static CenterProfileService getCenterProfileService() { return centerProfileService; }
    public static ExpenseCategoryService getExpenseCategoryService() { return expenseCategoryService; }
    public static ExpenseService getExpenseService() { return expenseService; }
    public static InventoryItemService getInventoryItemService() { return inventoryItemService; }
    public static OpticalDiagnosticService getOpticalDiagnosticService() { return opticalDiagnosticService; }
    public static PatientService getPatientService() { return patientService; }
    public static PaymentService getPaymentService() { return paymentService; } // Added getter
    public static ProductCategoryService getProductCategoryService() { return productCategoryService; }
    public static ProductService getProductService() { return productService; }
    public static PurchaseOrderService getPurchaseOrderService() { return purchaseOrderService; } // Assuming it will be added
    public static SalesOrderService getSalesOrderService() { return salesOrderService; }
    public static ShiftService getShiftService() { return shiftService; }
    public static UserService getUserService() { return userService; }
    public static UserSessionService getUserSessionService() { return userSessionService; }
    public static WhatsAppNotificationService getWhatsAppNotificationService() { return whatsAppNotificationService; }


    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Primary stage is closing. Application will exit.");
        });
        checkProfileAndLaunch();
    }

    private void checkProfileAndLaunch() throws Exception {
        showLoginScreen();
    }

    public static void showUserManagementView() throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLauncher.class.getResource("/com/basariatpos/ui/view/UserManagementView.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();

        com.basariatpos.ui.controller.UserManagementController controller = loader.getController();
        controller.setUserService(userService);

        Stage umStage = new Stage();
        umStage.initModality(Modality.APPLICATION_MODAL);
        umStage.setTitle(MessageProvider.getString("usermanagement.title"));

        Scene scene = new Scene(root, 900, 700);
        umStage.setScene(scene);
        umStage.show();
        logger.info("User Management View displayed.");
    }

    public static void showCenterProfileSetupWizard() throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLauncher.class.getResource("/com/basariatpos/ui/view/CenterProfileSetupWizard.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();

        com.basariatpos.ui.controller.CenterProfileSetupController controller = loader.getController();
        controller.setCenterProfileService(centerProfileService);

        Stage wizardStage = new Stage();
        wizardStage.initModality(Modality.APPLICATION_MODAL);
        wizardStage.initOwner(primaryStage);

        Scene scene = new Scene(root);
        wizardStage.setScene(scene);
        wizardStage.setTitle(MessageProvider.getString("app.title.centerProfileSetup"));

        wizardStage.setOnCloseRequest(event -> {
            try {
                if (centerProfileService.isProfileConfigured()) {
                    showLoginScreen();
                } else {
                    logger.warn("Center profile setup was closed without completing configuration. Application might not function correctly.");
                    showLoginScreen();
                }
            } catch (Exception e) {
                logger.error("Error after closing center profile wizard.", e);
            }
        });
        wizardStage.showAndWait();
    }


    public static void showLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLauncher.class.getResource("/com/basariatpos/ui/view/LoginView.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();
        com.basariatpos.ui.controller.LoginController loginController = loader.getController();
        // No explicit service injection here, LoginController uses AppLauncher.getXYZService()

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle(MessageProvider.getString("app.title.login"));
        primaryStage.show();
        logger.info("Login screen displayed.");
    }

    public static void showMainFrame(UserDTO authenticatedUser, ShiftDTO incompleteShift) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLauncher.class.getResource("/com/basariatpos/ui/view/MainFrame.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();

        MainFrameController mainFrameController = loader.getController();
        // Call setServicesAndLoadInitialData on MainFrameController
        mainFrameController.setServicesAndLoadInitialData(
            userSessionService, shiftService, applicationSettingsService, centerProfileService,
            productCategoryService, productService, inventoryItemService, salesOrderService,
            purchaseOrderService, patientService, opticalDiagnosticService, bankNameService,
            expenseCategoryService, expenseService, // Ensure expenseService is passed
            auditLogService, // Pass AuditLogService
            whatsAppNotificationService, paymentService
        );

        if (incompleteShift != null) {
            Platform.runLater(() -> mainFrameController.handleInterruptedShift(incompleteShift));
        } else {
             Platform.runLater(() -> mainFrameController.updateShiftStatusDisplayAndControls());
        }

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle(MessageProvider.getString("app.title.main") + " - " + authenticatedUser.getFullName());
        primaryStage.show();
        logger.info("Main application frame displayed for user: {}", authenticatedUser.getUsername());
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        logger.info("Application main method started.");
        launch(args);
    }
}
