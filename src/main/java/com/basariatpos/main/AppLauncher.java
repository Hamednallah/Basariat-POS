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
    private static CenterProfileService centerProfileService;
    private static UserService userService;
    private static BankNameService bankNameService;
    private static ExpenseCategoryService expenseCategoryService;
    private static ProductCategoryService productCategoryService;
    private static ApplicationSettingsService applicationSettingsService;
    private static ShiftService shiftService;
    private static UserSessionService userSessionService;
    private static SalesOrderService salesOrderService; // Added SalesOrderService
    private static PaymentService paymentService;       // Added PaymentService
    private static ExpenseService expenseService;       // Added ExpenseService


    @Override
    public void init() throws Exception {
        super.init();
        // Initialize services
        com.basariatpos.repository.UserRepository userRepository = new com.basariatpos.repository.UserRepositoryImpl();
        userSessionService = new com.basariatpos.service.UserSessionService(new com.basariatpos.repository.SessionRepositoryImpl());

        centerProfileService = new CenterProfileService(new CenterProfileRepositoryImpl());
        userService = new com.basariatpos.service.UserServiceImpl(userRepository);
        bankNameService = new com.basariatpos.service.BankNameServiceImpl(new com.basariatpos.repository.BankNameRepositoryImpl());
        expenseCategoryService = new com.basariatpos.service.ExpenseCategoryServiceImpl(new com.basariatpos.repository.ExpenseCategoryRepositoryImpl());
        productCategoryService = new com.basariatpos.service.ProductCategoryServiceImpl(new com.basariatpos.repository.ProductCategoryRepositoryImpl());
        applicationSettingsService = new com.basariatpos.service.ApplicationSettingsServiceImpl(new com.basariatpos.repository.ApplicationSettingsRepositoryImpl());
        shiftService = new com.basariatpos.service.ShiftServiceImpl(new com.basariatpos.repository.ShiftRepositoryImpl(), userRepository);

        // Instantiate new placeholder services
        salesOrderService = new SalesOrderServiceImpl(userSessionService);
        paymentService = new PaymentServiceImpl(userSessionService);
        // ExpenseServiceImpl constructor needs UserSessionService and later ExpenseRepository
        expenseService = new ExpenseServiceImpl(userSessionService /*, new com.basariatpos.repository.ExpenseRepositoryImpl() */);

        // Set default locale at the very beginning
        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        logger.info("Application initializing. Default locale set to: {}", LocaleManager.getCurrentLocale().toLanguageTag());
    }

    // Static getter for UserService
    public static UserService getUserService() {
        return userService;
    }

    // Static getter for CenterProfileService
    public static CenterProfileService getCenterProfileService() {
        return centerProfileService;
    }

    // Static getter for BankNameService
    public static BankNameService getBankNameService() {
        return bankNameService;
    }

    // Static getter for ExpenseCategoryService
    public static ExpenseCategoryService getExpenseCategoryService() {
        return expenseCategoryService;
    }

    // Static getter for ProductCategoryService
    public static ProductCategoryService getProductCategoryService() {
        return productCategoryService;
    }

    // Static getter for ApplicationSettingsService
    public static ApplicationSettingsService getApplicationSettingsService() {
        return applicationSettingsService;
    }

    // Static getter for ShiftService
    public static ShiftService getShiftService() {
        return shiftService;
    }

    // Static getter for UserSessionService
    public static UserSessionService getUserSessionService() {
        return userSessionService;
    }

    // Static getters for new placeholder services (optional, if needed by other parts immediately)
    public static SalesOrderService getSalesOrderService() {
        return salesOrderService;
    }

    public static PaymentService getPaymentService() {
        return paymentService;
    }

    public static ExpenseService getExpenseService() {
        return expenseService;
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

    public static void showUserManagementView() throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLauncher.class.getResource("/com/basariatpos/ui/view/UserManagementView.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();

        // If UserManagementController needs UserService, set it here (manual DI)
        com.basariatpos.ui.controller.UserManagementController controller = loader.getController();
        controller.setUserService(userService); // Assuming a setter exists

        Stage umStage = new Stage();
        umStage.initModality(Modality.APPLICATION_MODAL); // Or Modality.NONE if preferred
        umStage.setTitle(MessageProvider.getString("usermanagement.title"));
        // Set owner if primaryStage is suitable, or make it independent
        // umStage.initOwner(primaryStage);

        Scene scene = new Scene(root, 900, 700); // Example size
        umStage.setScene(scene);
        umStage.show(); // Or showAndWait() if modal behavior should block main frame interaction
        logger.info("User Management View displayed.");
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
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();
        // If LoginController needs UserSessionService or others, it should also get them from AppLauncher or DI
        com.basariatpos.ui.controller.LoginController loginController = loader.getController();
        // loginController.setUserSessionService(userSessionService); // Example if LoginController is refactored for this

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle(MessageProvider.getString("app.title.login"));
        primaryStage.show();
        logger.info("Login screen displayed.");
    }

    public static void showMainFrame() throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLauncher.class.getResource("/com/basariatpos/ui/view/MainFrame.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();

        // After loading MainFrame, explicitly update its shift status display
        com.basariatpos.ui.controller.MainFrameController mainFrameController = loader.getController();
        // Services are injected in MainFrameController's initialize via AppLauncher getters now.
        // Call updateShiftStatusDisplayAndControls if it's not automatically handled by property listeners yet.
        // For now, assuming initialize() handles the first call. If login flow needs explicit update:
        // mainFrameController.updateShiftStatusDisplayAndControls();

        Scene scene = new Scene(root, 1024, 768);
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
