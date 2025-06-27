package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.UserService; // Not directly used by controller logic being tested here

import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserFormDialogControllerTest {

    @Mock private Label dialogTitleLabel;
    @Mock private TextField usernameField;
    @Mock private TextField fullNameField;
    @Mock private ComboBox<String> roleComboBox;
    @Mock private Label passwordLabel;
    @Mock private PasswordField passwordField;
    @Mock private Label confirmPasswordLabel;
    @Mock private PasswordField confirmPasswordField;
    @Mock private CheckBox activeCheckBox;
    @Mock private VBox permissionsVBox;
    @Mock private VBox userFormRootPane; // For RTL
    @Mock private Stage mockDialogStage;


    @InjectMocks
    private UserFormDialogController controller;

    private static ResourceBundle resourceBundle;

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
        // Manual injection of @FXML mocks
        controller.dialogTitleLabel = dialogTitleLabel;
        controller.usernameField = usernameField;
        controller.fullNameField = fullNameField;
        controller.roleComboBox = roleComboBox;
        controller.passwordLabel = passwordLabel;
        controller.passwordField = passwordField;
        controller.confirmPasswordLabel = confirmPasswordLabel;
        controller.confirmPasswordField = confirmPasswordField;
        controller.activeCheckBox = activeCheckBox;
        controller.permissionsVBox = permissionsVBox;
        controller.userFormRootPane = userFormRootPane;

        // Mock ComboBox items
        when(roleComboBox.getItems()).thenReturn(FXCollections.observableArrayList("Admin", "Cashier"));
        when(permissionsVBox.getChildren()).thenReturn(FXCollections.observableArrayList());


        controller.initialize(null, resourceBundle);
        controller.setDialogStage(mockDialogStage); // Set the mocked stage
    }

    @Test
    void initialize_setsUpRolesAndPermissionsAndOrientation() {
        verify(roleComboBox).setItems(FXCollections.observableArrayList("Admin", "Cashier"));
        verify(activeCheckBox).setSelected(true);
        verify(permissionsVBox, atLeastOnce()).getChildren(); // populatePermissionCheckboxes called
        verify(userFormRootPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void setEditableUser_populatesFieldsAndHidesPasswordForEditMode() {
        UserDTO user = new UserDTO(1L, "edituser", "Edit User", "Admin", "hash", true, null);
        List<String> permissions = List.of("permission.MANAGE_USERS");

        // Simulate CheckBox creation for permissions
        CheckBox cbManageUsers = new CheckBox(MessageProvider.getString("permission.MANAGE_USERS"));
        cbManageUsers.setUserData("permission.MANAGE_USERS");
        controller.permissionCheckBoxes.add(cbManageUsers); // Add to controller's list

        controller.setEditableUser(user, permissions);

        verify(dialogTitleLabel).setText(MessageProvider.getString("usermanagement.dialog.editUser.title"));
        verify(usernameField).setText("edituser");
        verify(usernameField).setDisable(true);
        verify(fullNameField).setText("Edit User");
        verify(roleComboBox).setValue("Admin");
        verify(activeCheckBox).setSelected(true);

        verify(passwordField).setVisible(false);
        verify(confirmPasswordField).setVisible(false);
        assertTrue(cbManageUsers.isSelected());
    }

    @Test
    void handleSaveButtonAction_validInput_addUserMode_savesAndCloses() {
        when(usernameField.getText()).thenReturn("newuser");
        when(fullNameField.getText()).thenReturn("New User Full");
        when(roleComboBox.getValue()).thenReturn("Cashier");
        when(passwordField.getText()).thenReturn("password123");
        when(confirmPasswordField.getText()).thenReturn("password123");
        when(activeCheckBox.isSelected()).thenReturn(true);

        // Simulate a permission checkbox
        CheckBox permCheckbox = new CheckBox("Test Permission");
        permCheckbox.setUserData("test.permission");
        permCheckbox.setSelected(true);
        controller.permissionCheckBoxes.add(permCheckbox);


        controller.handleSaveButtonAction(null);

        assertTrue(controller.isSaved());
        UserDTO userData = controller.getUserData();
        assertEquals("newuser", userData.getUsername());
        assertEquals("New User Full", userData.getFullName());
        assertEquals("Cashier", userData.getRole());
        assertTrue(userData.isActive());
        assertEquals("password123", controller.getPassword());
        assertTrue(controller.getSelectedPermissions().contains("test.permission"));

        verify(mockDialogStage).close();
    }

    @Test
    void handleSaveButtonAction_invalidInput_showsError() {
        when(usernameField.getText()).thenReturn(""); // Invalid
        // ... other fields can be valid for this specific test path

        controller.handleSaveButtonAction(null);

        assertFalse(controller.isSaved());
        verify(mockDialogStage, never()).close();
        // Verification of showValidationErrorAlert is tricky without TestFX,
        // but we know it should have been called internally.
    }

    @Test
    void initialize_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        // Reset VBox children for permission population with new bundle
        when(permissionsVBox.getChildren()).thenReturn(FXCollections.observableArrayList());

        controller.initialize(null, resourceBundle);

        verify(userFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
