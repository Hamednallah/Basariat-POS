package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.UserException;
import com.basariatpos.service.UserService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserManagementControllerTest {

    @Mock private UserService mockUserService;
    @Mock private TableView<UserDTO> usersTable;
    @Mock private TableColumn<UserDTO, String> usernameColumn;
    @Mock private TableColumn<UserDTO, String> fullNameColumn;
    @Mock private TableColumn<UserDTO, String> roleColumn;
    @Mock private TableColumn<UserDTO, String> statusColumn;
    @Mock private Button editUserButton;
    @Mock private Button toggleActivityButton;
    @Mock private Button resetPasswordButton;
    @Mock private BorderPane userManagementRootPane; // For RTL

    @Spy // Use Spy to allow partial mocking if TableView's internal list is used directly
    private ObservableList<UserDTO> userObservableList = FXCollections.observableArrayList();

    @InjectMocks
    private UserManagementController controller;

    private static ResourceBundle resourceBundle;
    private MockedStatic<AppLauncher> mockAppLauncherStatic;
    private MockedStatic<Platform> mockPlatformStatic;


    @BeforeAll
    static void setUpClass() {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        resourceBundle = MessageProvider.getBundle();
        try {
            new javafx.embed.swing.JFXPanel(); // Initializes JavaFX toolkit
        } catch (Exception e) {
            // Ignore if already initialized or running in a TestFX environment
        }
    }

    @BeforeEach
    void setUp() {
        // Manual injection of @FXML mocks
        controller.usersTable = usersTable;
        controller.usernameColumn = usernameColumn;
        controller.fullNameColumn = fullNameColumn;
        controller.roleColumn = roleColumn;
        controller.statusColumn = statusColumn;
        controller.editUserButton = editUserButton;
        controller.toggleActivityButton = toggleActivityButton;
        controller.resetPasswordButton = resetPasswordButton;
        controller.userManagementRootPane = userManagementRootPane;
        // controller.userObservableList is already part of the controller instance due to @Spy

        // Mock AppLauncher static calls
        mockAppLauncherStatic = Mockito.mockStatic(AppLauncher.class);
        mockAppLauncherStatic.when(AppLauncher::getUserService).thenReturn(mockUserService);

        mockPlatformStatic = Mockito.mockStatic(Platform.class);
        mockPlatformStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        });


        // Simulate that the usersTable uses our spy list
        when(usersTable.getItems()).thenReturn(userObservableList);
        // Mock selection model for listeners
        TableView.TableViewSelectionModel<UserDTO> mockSelectionModel = mock(TableView.TableViewSelectionModel.class);
        when(usersTable.getSelectionModel()).thenReturn(mockSelectionModel);


        controller.initialize(null, resourceBundle);
    }

    @AfterEach
    void tearDown() {
        mockAppLauncherStatic.close();
        mockPlatformStatic.close();
    }

    @Test
    void initialize_setsUpTableColumnsAndLoadsUsers() throws UserException {
        // Verify column setup (PropertyValueFactory means we can't easily verify the exact factory instance)
        verify(usernameColumn).setCellValueFactory(any(PropertyValueFactory.class));
        verify(fullNameColumn).setCellValueFactory(any(PropertyValueFactory.class));
        verify(roleColumn).setCellValueFactory(any(PropertyValueFactory.class));
        verify(statusColumn).setCellValueFactory(any()); // For SimpleStringProperty lambda

        // Verify users are loaded
        List<UserDTO> testUsers = List.of(new UserDTO(1L, "test1", "Test User 1", "Admin", "hash", true, null));
        when(mockUserService.getAllUsers()).thenReturn(testUsers);

        // Re-trigger loadUsers as initialize might have already called it with a different mock state for userService
        // if AppLauncher.getUserService() was not fully mocked before controller.initialize() in setup().
        // Best to ensure service is fully mocked before initialize() is called.
        // For this test, explicitly calling loadUsers() after setting up the mock for getAllUsers().
        controller.loadUsers();

        assertEquals(1, userObservableList.size());
        assertEquals("test1", userObservableList.get(0).getUsername());

        verify(userManagementRootPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void loadUsers_handlesUserException() throws UserException {
        when(mockUserService.getAllUsers()).thenThrow(new UserException("DB connection failed"));

        // Act: loadUsers is called in initialize, or can be called directly
        // For this test, let's ensure it's called and doesn't throw upwards
        assertDoesNotThrow(() -> controller.loadUsers());

        // Verify that an error alert would be shown (cannot directly test JavaFX alerts easily)
        // but we can check that the list remains empty or unchanged.
        assertTrue(userObservableList.isEmpty());
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initialize(null, resourceBundle); // Re-initialize for Arabic

        verify(userManagementRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }

    // Tests for handleAddUserButtonAction, handleEditUserButtonAction, etc.
    // would require mocking FXMLLoader and the dialog controllers.
    // These become more like integration tests or require a UI testing framework like TestFX
    // to properly manage dialog stages and user interactions.

    // Example: Sketch for handleAddUserButtonAction test (would need FXML loader mocking)
    /*
    @Test
    void handleAddUserButtonAction_opensDialogAndRefreshesTableOnSave() throws IOException, UserException {
        // Mock FXMLLoader and UserFormDialogController
        // ...

        // Simulate dialog being saved
        // when(mockDialogController.isSaved()).thenReturn(true);
        // when(mockDialogController.getUserData()).thenReturn(newUser);
        // when(mockDialogController.getPassword()).thenReturn("newPass");
        // when(mockDialogController.getSelectedPermissions()).thenReturn(List.of("perm1"));
        // when(mockUserService.createUser(any(UserDTO.class), anyString())).thenReturn(createdUser);

        // controller.handleAddUserButtonAction(mock(ActionEvent.class));

        // verify(mockUserService).createUser(any(), any());
        // verify(mockUserService).grantPermission(anyLong(), eq("perm1"));
        // verify(mockUserService, times(2)).getAllUsers(); // Initial load + refresh
    }
    */

    // Similarly, tests for edit, toggle activity, reset password would need dialog interaction mocking.
    // The focus of this unit test is primarily the UserManagementController's own logic,
    // like table setup, data loading, and main UI state changes (button disable/enable based on selection).
}
