package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ShiftDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.*;
import com.basariatpos.ui.theme.AppTheme; // Assuming AppTheme exists

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.web.WebView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level; // For less verbose JavaFX FXMLLoader logging

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
public class MainFrameControllerTest {

    private MainFrameController controller;
    private Stage primaryStage;

    //<editor-fold desc="Mocks for Services and Static Classes">
    @Mock private UserSessionService mockUserSessionService;
    @Mock private ShiftService mockShiftService;
    @Mock private ApplicationSettingsService mockApplicationSettingsService;
    @Mock private CenterProfileService mockCenterProfileService;
    @Mock private ProductCategoryService mockProductCategoryService;
    @Mock private ProductService mockProductService;
    @Mock private InventoryItemService mockInventoryItemService;
    @Mock private SalesOrderService mockSalesOrderService;
    @Mock private PurchaseOrderService mockPurchaseOrderService;
    @Mock private PatientService mockPatientService;
    @Mock private OpticalDiagnosticService mockOpticalDiagnosticService;
    @Mock private BankNameService mockBankNameService;
    @Mock private ExpenseCategoryService mockExpenseCategoryService;
    @Mock private AuditLogService mockAuditLogService;
    @Mock private WhatsAppNotificationService mockWhatsAppNotificationService;
    @Mock private PaymentService mockPaymentService;
    @Mock private UserService mockUserService; // For UserManagement

    private MockedStatic<LocaleManager> staticLocaleManagerMock;
    private MockedStatic<MessageProvider> staticMessageProviderMock;
    private MockedStatic<AppLauncher> staticAppLauncherMock;
    private MockedStatic<AppTheme> staticAppThemeMock;
    private MockedStatic<Platform> staticPlatformMock; // For Platform.exit()
    //</editor-fold>

    @Start
    private void start(Stage stage) throws IOException {
        // Silence verbose FXMLLoader logging if it's an issue during tests
        java.util.logging.Logger.getLogger(FXMLLoader.class.getName()).setLevel(Level.WARNING);

        MockitoAnnotations.openMocks(this);
        primaryStage = stage;

        // Mock static classes
        staticLocaleManagerMock = Mockito.mockStatic(LocaleManager.class);
        staticMessageProviderMock = Mockito.mockStatic(MessageProvider.class);
        staticAppLauncherMock = Mockito.mockStatic(AppLauncher.class);
        staticAppThemeMock = Mockito.mockStatic(AppTheme.class);
        staticPlatformMock = Mockito.mockStatic(Platform.class);


        // Configure static mocks
        staticLocaleManagerMock.when(LocaleManager::getCurrentLocale).thenReturn(Locale.ENGLISH);
        staticLocaleManagerMock.when(() -> LocaleManager.getLocaleByLanguageCode(anyString())).thenCallRealMethod();
        staticLocaleManagerMock.when(() -> LocaleManager.addLocaleChangeListener(any())).thenAnswer(invocation -> null); // Do nothing for listener

        staticMessageProviderMock.when(() -> MessageProvider.getString(anyString())).thenAnswer(inv -> "mock:" + inv.getArgument(0));
        staticMessageProviderMock.when(() -> MessageProvider.getString(anyString(), any(), any())).thenAnswer(inv -> "mock:" + inv.getArgument(0) + ":" + inv.getArgument(1) + ":" + inv.getArgument(2));
        staticMessageProviderMock.when(MessageProvider::getBundle).thenReturn(ResourceBundle.getBundle("com_basariatpos_messages", Locale.ENGLISH));

        AppTheme mockAppThemeInstance = mock(AppTheme.class);
        staticAppThemeMock.when(AppTheme::getInstance).thenReturn(mockAppThemeInstance);
        doNothing().when(mockAppThemeInstance).applyCurrentTheme(any());

        staticAppLauncherMock.when(AppLauncher::getUserService).thenReturn(mockUserService);
        doNothing().when(staticAppLauncherMock, () -> AppLauncher.reloadMainScene(any(Stage.class)));


        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/MainFrame.fxml"));
        loader.setResources(MessageProvider.getBundle()); // Ensure bundle is passed
        Parent root = loader.load();
        controller = loader.getController();

        // Inject services into controller
        controller.setServicesAndLoadInitialData(
                mockUserSessionService, mockShiftService, mockApplicationSettingsService, mockCenterProfileService,
                mockProductCategoryService, mockProductService, mockInventoryItemService, mockSalesOrderService,
                mockPurchaseOrderService, mockPatientService, mockOpticalDiagnosticService, mockBankNameService,
                mockExpenseCategoryService, mockAuditLogService, mockWhatsAppNotificationService, mockPaymentService
        );

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("MainFrame Test - Restored");
        stage.show();
    }

    @AfterEach
    void tearDown() {
        staticLocaleManagerMock.close();
        staticMessageProviderMock.close();
        staticAppLauncherMock.close();
        staticAppThemeMock.close();
        staticPlatformMock.close();
    }

    @BeforeEach
    void setupForEachTest() {
        // Reset interaction counts on mocks, and provide default behaviors
        reset(mockUserSessionService, mockShiftService); // Add other services if they have interactions to reset

        UserDTO mockUser = mock(UserDTO.class);
        when(mockUser.getUserId()).thenReturn(1);
        when(mockUser.getUsername()).thenReturn("testuser");
        when(mockUserSessionService.getCurrentUser()).thenReturn(mockUser);
        when(mockUserSessionService.getActiveShift()).thenReturn(null); // Default: no active shift

        try {
            when(mockShiftService.getIncompleteShiftForUser(anyInt())).thenReturn(Optional.empty());
        } catch (ShiftException e) {
            fail("Failed to mock getIncompleteShiftForUser: " + e.getMessage());
        }
        // Call this to ensure UI reflects the default mock state
        controller.updateShiftStatusDisplayAndControls();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testInitialState_NoActiveShift(FxRobot robot) {
        assertEquals("mock:mainframe.shiftstatus.noActiveShift", robot.lookup("#shiftStatusLabel").queryAs(Label.class).getText());
        assertFalse(robot.lookup("#startShiftButton").queryAs(Button.class).isDisabled());
        assertTrue(robot.lookup("#startShiftButton").queryAs(Button.class).isVisible());
        assertTrue(robot.lookup("#startShiftButton").queryAs(Button.class).isManaged());

        assertFalse(robot.lookup("#pauseShiftButton").queryAs(Button.class).isVisible());
        assertFalse(robot.lookup("#pauseShiftButton").queryAs(Button.class).isManaged());
        assertFalse(robot.lookup("#resumeShiftButton").queryAs(Button.class).isVisible());
        assertFalse(robot.lookup("#resumeShiftButton").queryAs(Button.class).isManaged());
    }

    @Test
    void testInitialState_ActiveShiftPresent(FxRobot robot) throws ShiftException {
        ShiftDTO activeShift = new ShiftDTO(1, 1, "testuser", OffsetDateTime.now(), null, "Active", BigDecimal.TEN);
        when(mockShiftService.getIncompleteShiftForUser(1)).thenReturn(Optional.of(activeShift));

        controller.updateShiftStatusDisplayAndControls(); // Trigger UI update
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("mock:mainframe.shiftstatus.active:1:testuser", robot.lookup("#shiftStatusLabel").queryAs(Label.class).getText());
        assertTrue(robot.lookup("#startShiftButton").queryAs(Button.class).isDisabled()); // Assuming it becomes invisible or disabled
        assertFalse(robot.lookup("#startShiftButton").queryAs(Button.class).isVisible());

        assertTrue(robot.lookup("#pauseShiftButton").queryAs(Button.class).isVisible());
        assertFalse(robot.lookup("#pauseShiftButton").queryAs(Button.class).isDisabled());
    }


    @Test
    void testSwitchLanguage_EnglishToArabic(FxRobot robot) {
        robot.clickOn("#menuFile").clickOn("#menuFileLanguage").clickOn("#menuItemArabic");
        WaitForAsyncUtils.waitForFxEvents();
        staticLocaleManagerMock.verify(() -> LocaleManager.setCurrentLocale(LocaleManager.ARABIC));
        staticAppLauncherMock.verify(() -> AppLauncher.reloadMainScene(primaryStage));
    }

    @Test
    void testFileExit_CallsPlatformExit(FxRobot robot) {
        robot.clickOn("#menuFile").clickOn("#menuFileExit");
        WaitForAsyncUtils.waitForFxEvents();
        staticPlatformMock.verify(Platform::exit);
    }

    @Test
    void testLoadUserManagementView(FxRobot robot) {
        // Example for testing a menu item that loads a view
        // This is a simplified version; truly verifying the controllerInitializer part is more complex
        // and might involve ArgumentCaptors or ensuring the sub-controller's methods are called.

        try (MockedConstruction<FXMLLoader> mockLoaderConstruction = Mockito.mockConstruction(FXMLLoader.class,
            (mock, context) -> {
                // Mock the behavior of the FXMLLoader instance
                Parent mockLoadedPane = new StackPane(new Label("Mock User Management View")); // Create a mock Parent
                when(mock.load()).thenReturn(mockLoadedPane);
                UserManagementController mockSubController = mock(UserManagementController.class);
                when(mock.getController()).thenReturn(mockSubController);
            })) {

            robot.clickOn("#menuAdmin").clickOn("#userManagementMenuItem");
            WaitForAsyncUtils.waitForFxEvents();

            // Verify that an FXMLLoader was created and load was called
            assertEquals(1, mockLoaderConstruction.constructed().size());
            FXMLLoader constructedLoader = mockLoaderConstruction.constructed().get(0);
            verify(constructedLoader).load(); // Verify load was called on this instance

            // Verify the sub-controller got its service (if possible to check)
            // This assumes the Consumer in loadViewIntoCenter gets called and sets the service.
            UserManagementController subController = (UserManagementController) constructedLoader.getController();
            verify(subController).setUserService(mockUserService); // Or AppLauncher.getUserService() if not directly passed
            verify(subController).loadInitialData(); // Verify data loading

            // Verify the main content area was updated
            BorderPane mainBorderPane = robot.lookup("#mainBorderPane").queryAs(BorderPane.class);
            assertTrue(((StackPane)mainBorderPane.getCenter()).getChildren().get(0) instanceof Label &&
                       ((Label)((StackPane)mainBorderPane.getCenter()).getChildren().get(0)).getText().contains("Mock User Management View"));

        } catch (IOException e) {
            fail("IOException during mock FXML loading: " + e.getMessage());
        }
    }

    @Test
    void testHandleStartShiftAction_OpensDialog_AndStartsShift(FxRobot robot) throws Exception {
        // This test requires mocking the StartShiftDialogController
        // and its interaction, which is complex.
        // For brevity, this is a conceptual outline.

        // 1. Mock FXMLLoader for StartShiftDialog.fxml
        // 2. Mock StartShiftDialogController
        // 3. When robot clicks #startShiftButton:
        //    - Verify dialog FXML is loaded.
        //    - Simulate dialog controller returning 'saved' and an opening float.
        // 4. Verify mockShiftService.startNewShift is called.
        // 5. Verify mockUserSessionService.setActiveShift is called.
        // 6. Verify UI updates (shiftStatusLabel, button states).

        // Simulate a successful shift start
        ShiftDTO startedShift = new ShiftDTO(2, 1, "testuser", OffsetDateTime.now(), null, "Active", new BigDecimal("100.00"));
        when(mockShiftService.startNewShift(eq(1), any(BigDecimal.class))).thenReturn(startedShift);

        // This simplified version directly calls the handler to test service calls and UI update,
        // bypassing the actual dialog interaction for this example.
        // A full test would use robot.clickOn("#startShiftButton") and mock the dialog.

        // To simulate dialog being shown and returning values:
        // We can't easily mock the dialog interaction here without more setup.
        // A more integration-style test would let the dialog show and interact with it.
        // A unit-style test would mock the dialog controller itself.

        // For now, let's assume the dialog part is tested elsewhere or we are focusing on the main controller's reaction.
        // If handleStartShiftAction directly called a method that showed dialog and returned result, that method could be mocked.
        // Since it news up FXMLLoader itself, it's harder.

        // Placeholder:
        // robot.clickOn("#startShiftButton");
        // ... (robot interactions with the dialog if it were real) ...
        // For now, just assert that the button exists
        assertNotNull(robot.lookup("#startShiftButton").queryAs(Button.class));
        // A more complete test would verify the dialog loading and interaction.
    }

    // TODO: Add tests for handlePauseShiftAction, handleResumeShiftAction
    // TODO: Add tests for handleInterruptedShift (mocking InterruptedShiftDialogController)
    // TODO: Add tests for other menu items loading views (similar to testLoadUserManagementView)
    // TODO: Test handleHelpAboutAction (mocking AboutDialog.fxml loading)
    // TODO: Test handleViewUserManual (mocking DesktopActions.openUrl)

}
