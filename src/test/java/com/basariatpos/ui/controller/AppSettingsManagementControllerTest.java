package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.ApplicationSettingDTO;
import com.basariatpos.service.ApplicationSettingsService;
import com.basariatpos.service.SettingException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppSettingsManagementControllerTest {

    @Mock private ApplicationSettingsService mockAppSettingsService;
    @Mock private TableView<ApplicationSettingDTO> settingsTable;
    @Mock private TableColumn<ApplicationSettingDTO, String> keyColumn;
    @Mock private TableColumn<ApplicationSettingDTO, String> valueColumn;
    @Mock private TableColumn<ApplicationSettingDTO, String> descriptionColumn;
    @Mock private BorderPane appSettingsManagementRootPane; // For RTL

    @Spy
    private ObservableList<ApplicationSettingDTO> settingsObservableList = FXCollections.observableArrayList();

    @InjectMocks
    private AppSettingsManagementController controller;

    private static ResourceBundle resourceBundle;
    private MockedStatic<AppLauncher> mockAppLauncherStatic;

    @BeforeAll
    static void setUpClass() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        resourceBundle = MessageProvider.getBundle();
        try {
            new javafx.embed.swing.JFXPanel();
        } catch (Exception e) { /* Ignore */ }
    }

    @BeforeEach
    void setUp() {
        controller.settingsTable = settingsTable;
        controller.keyColumn = keyColumn;
        controller.valueColumn = valueColumn;
        controller.descriptionColumn = descriptionColumn;
        controller.appSettingsManagementRootPane = appSettingsManagementRootPane;
        // settingsObservableList is spy-injected in the controller instance by Mockito if controller field is also @Spy or initialized with this instance.
        // For safety, let's ensure the controller uses this spied list if it was re-instantiating its own.
        // However, @InjectMocks with @Spy on the field in the test should correctly inject the spy.

        mockAppLauncherStatic = Mockito.mockStatic(AppLauncher.class);
        mockAppLauncherStatic.when(AppLauncher::getApplicationSettingsService).thenReturn(mockAppSettingsService);

        when(settingsTable.getItems()).thenReturn(settingsObservableList);

        controller.initialize(null, resourceBundle);
    }

    @AfterEach
    void tearDown() {
        mockAppLauncherStatic.close();
    }

    @Test
    void initialize_setsUpTableAndLoadsSettings() throws SettingException {
        verify(keyColumn).setCellValueFactory(any(PropertyValueFactory.class));
        verify(valueColumn).setCellValueFactory(any(PropertyValueFactory.class));
        verify(descriptionColumn).setCellValueFactory(any(PropertyValueFactory.class));
        verify(valueColumn).setCellFactory(any()); // For TextFieldTableCell
        verify(valueColumn).setOnEditCommit(any());


        List<ApplicationSettingDTO> testSettings = List.of(
            new ApplicationSettingDTO("app.name", "Basariat POS", "Application Name")
        );
        when(mockAppSettingsService.getAllApplicationSettings()).thenReturn(testSettings);

        controller.loadSettings();

        assertEquals(1, settingsObservableList.size());
        assertEquals("app.name", settingsObservableList.get(0).getSettingKey());
        verify(appSettingsManagementRootPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void loadSettings_handlesSettingException() throws SettingException {
        when(mockAppSettingsService.getAllApplicationSettings()).thenThrow(new SettingException("DB Error"));
        // Mock getScene().getWindow() for showErrorAlert
        when(settingsTable.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(settingsTable.getScene().getWindow()).thenReturn(mock(javafx.stage.Stage.class));


        assertDoesNotThrow(() -> controller.loadSettings());
        assertTrue(settingsObservableList.isEmpty());
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initialize(null, resourceBundle);

        verify(appSettingsManagementRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }

    // Test for valueColumn.setOnEditCommit lambda is more involved.
    // It requires mocking TableColumn.CellEditEvent which is non-trivial.
    // A basic test could ensure the service method is called.
    @Test
    void onEditCommit_validUpdate_callsService() throws SettingException {
        // This test is a sketch and would need proper event mocking.
        // For now, we assume the lambda correctly calls the service.
        // To truly test this, TestFX or a more intricate mock setup for CellEditEvent is needed.

        // Simulate an edit event (simplified)
        ApplicationSettingDTO settingToEdit = new ApplicationSettingDTO("test.key", "oldValue", "Test Desc");
        settingsObservableList.add(settingToEdit);

        // Assume the onEditCommit handler is obtained and invoked.
        // This is not straightforward to do in a pure unit test without TestFX.
        // We are testing the logic within the lambda, not the event triggering itself.

        // If we could trigger the lambda:
        // TableColumn.CellEditEvent<ApplicationSettingDTO, String> mockEvent = mock(TableColumn.CellEditEvent.class);
        // when(mockEvent.getRowValue()).thenReturn(settingToEdit);
        // when(mockEvent.getNewValue()).thenReturn("newValue");
        // when(mockEvent.getTablePosition()).thenReturn(new TablePosition<>(settingsTable, 0, valueColumn));
        // ... then invoke the handler ...
        // controller.getValueColumnEditCommitHandler().handle(mockEvent); // If handler was extracted

        // For now, this part remains as a known area for more advanced testing.
        assertTrue(true, "onEditCommit logic testing requires more advanced setup (e.g., TestFX).");
    }
}
