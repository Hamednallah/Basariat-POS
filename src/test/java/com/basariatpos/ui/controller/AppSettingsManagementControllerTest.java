package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ApplicationSettingDTO;
import com.basariatpos.service.ApplicationSettingsService;
import com.basariatpos.service.SettingNotFoundException;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;

@ExtendWith(ApplicationExtension.class)
class AppSettingsManagementControllerTest {

    @Mock
    private ApplicationSettingsService mockAppSettingsService;

    private AppSettingsManagementController controller;
    private Stage stage;
    private MockedStatic<AppLauncher> appLauncherMockedStatic;

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        appLauncherMockedStatic = Mockito.mockStatic(AppLauncher.class);
        appLauncherMockedStatic.when(AppLauncher::getApplicationSettingsService).thenReturn(mockAppSettingsService);

        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/AppSettingsManagementView.fxml"));
        loader.setResources(bundle);
        Parent root = loader.load();
        controller = loader.getController();
        // Service is set in controller's initialize via AppLauncher static getter

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws Exception {
        ApplicationSettingDTO setting1 = new ApplicationSettingDTO("app.name", "Basariat POS", "Application Name");
        ApplicationSettingDTO setting2 = new ApplicationSettingDTO("app.version", "1.0.0", "Application Version");
        when(mockAppSettingsService.getAllApplicationSettings()).thenReturn(Arrays.asList(setting1, setting2));
        // Controller's initialize calls loadSettings() which uses the service.
        // Static mock for AppLauncher should provide the service at that point.
    }

    @AfterEach
    void tearDown(FxRobot robot) {
        if (appLauncherMockedStatic != null) {
            appLauncherMockedStatic.close();
        }
        robot.listWindows().stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL)
            .forEach(w -> robot.targetWindow(w).close());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void tableIsPopulated_onInitialize(FxRobot robot) {
        TableView<ApplicationSettingDTO> settingsTable = robot.lookup("#settingsTable").queryTableView();
        assertNotNull(settingsTable);
        robot.waitUntil(() -> settingsTable.getItems().size() == 2, 2000);
        assertEquals(2, settingsTable.getItems().size());

        ObservableList<ApplicationSettingDTO> items = settingsTable.getItems();
        assertTrue(items.stream().anyMatch(s -> s.getSettingKey().equals("app.name") && s.getSettingValue().equals("Basariat POS")));
        assertTrue(items.stream().anyMatch(s -> s.getSettingKey().equals("app.version") && s.getSettingValue().equals("1.0.0")));
    }

    @Test
    void editValueColumn_successfulUpdate_callsService(FxRobot robot) throws Exception {
        TableView<ApplicationSettingDTO> settingsTable = robot.lookup("#settingsTable").queryTableView();
        robot.waitUntil(() -> settingsTable.getItems().size() == 2, 1000);

        ApplicationSettingDTO settingToEdit = settingsTable.getItems().stream()
            .filter(s -> s.getSettingKey().equals("app.name"))
            .findFirst().orElseThrow(() -> new AssertionError("Test setting 'app.name' not found in table"));

        doNothing().when(mockAppSettingsService).updateSettingValue(eq("app.name"), anyString());

        // Simulate editing the "app.name" setting's value
        // TestFX way to edit a cell:
        Node cell = robot.from(settingsTable).lookup(".table-cell").nth(1).query(); // 0=key, 1=value for first row
        robot.doubleClickOn(cell);
        WaitForAsyncUtils.waitForFxEvents(); // Wait for TextField to appear

        // Assuming the first row is 'app.name' after sorting (or ensure it)
        // For more robust test, find row by DTO content.
        // This example assumes 'app.name' is in the first visible row if table isn't sorted,
        // or is the first item in the observable list if table reflects that order.
        // Let's target the cell more directly based on its DTO.

        // For simplicity, we'll assume the first row displayed corresponds to the first item in our mocked list,
        // which should be "app.name".

        robot.write("Basariat POS Updated").push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockAppSettingsService).updateSettingValue("app.name", "Basariat POS Updated");

        // Verify success alert (simplified check)
        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage &&
                                              ((Stage)w).getTitle().equals(MessageProvider.getString("appsettings.management.title")) &&
                                              robot.lookup(".alert.information").queryAll().size() > 0 ),
                   "Success alert should be shown.");
        robot.targetWindow(windows.stream().filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL).findFirst().get()).close(); // Close alert
    }

    @Test
    void editValueColumn_serviceThrowsSettingNotFound_showsErrorAlert(FxRobot robot) throws Exception {
        TableView<ApplicationSettingDTO> settingsTable = robot.lookup("#settingsTable").queryTableView();
        robot.waitUntil(() -> settingsTable.getItems().size() == 2, 1000);

        doThrow(new SettingNotFoundException("app.name"))
            .when(mockAppSettingsService).updateSettingValue(eq("app.name"), anyString());

        Node cell = robot.from(settingsTable).lookup(".table-cell").nth(1).query();
        robot.doubleClickOn(cell);
        WaitForAsyncUtils.waitForFxEvents();
        robot.write("Attempted Update").push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockAppSettingsService).updateSettingValue("app.name", "Attempted Update");

        // Verify error alert
        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage &&
                                              ((Stage)w).getTitle().equals(MessageProvider.getString("appsettings.error.updateFailed")) &&
                                              robot.lookup(".alert.error").queryAll().size() > 0 ),
                   "Error alert for setting not found should be shown.");
        robot.targetWindow(windows.stream().filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL).findFirst().get()).close();

        // Verify value in table is reverted (or not changed from original mock)
        assertEquals("Basariat POS", settingsTable.getItems().get(0).getSettingValue());
    }

    // Add more tests for other service exceptions (ValidationException, general SettingException)
    // and for specific validation rules like app.version format.
}
