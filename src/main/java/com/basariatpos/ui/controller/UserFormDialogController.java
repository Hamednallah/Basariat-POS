package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.UserService; // Assuming this will be passed
import com.basariatpos.util.AppLogger;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserFormDialogController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(UserFormDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label passwordLabel;
    @FXML private PasswordField passwordField;
    @FXML private Label confirmPasswordLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox activeCheckBox;
    @FXML private VBox permissionsVBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private UserService userService; // To be injected or set
    private UserDTO editableUser;
    private boolean isEditMode = false;
    private boolean saved = false;

    private List<String> availablePermissions = Arrays.asList(
        "permission.CAN_GIVE_DISCOUNT",
        "permission.VIEW_FINANCIAL_REPORTS",
        "permission.MANAGE_INVENTORY",
        "permission.MANAGE_USERS",
        "permission.MANAGE_SETTINGS"
        // Add more as they are defined in SRS FR1.4 and i18n files
    );
    private List<CheckBox> permissionCheckBoxes = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate roles (example roles, could come from a service or enum)
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Cashier", "InventoryManager"));
        activeCheckBox.setSelected(true); // Default for new user

        populatePermissionCheckboxes();
    }

    public void getDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setEditableUser(UserDTO user, List<String> userPermissions) {
        this.editableUser = user;
        this.isEditMode = true;

        dialogTitleLabel.setText(MessageProvider.getString("usermanagement.dialog.editUser.title"));
        usernameField.setText(user.getUsername());
        usernameField.setDisable(true); // Username typically not editable
        fullNameField.setText(user.getFullName());
        roleComboBox.setValue(user.getRole());
        activeCheckBox.setSelected(user.isActive());

        // Hide password fields in edit mode by default (unless changing password is part of this form)
        // For this design, password change is separate.
        passwordField.setVisible(false);
        passwordField.setManaged(false);
        passwordLabel.setVisible(false);
        passwordLabel.setManaged(false);
        confirmPasswordField.setVisible(false);
        confirmPasswordField.setManaged(false);
        confirmPasswordLabel.setVisible(false);
        confirmPasswordLabel.setManaged(false);

        // Set permission checkboxes
        for (CheckBox cb : permissionCheckBoxes) {
            String permissionKey = (String) cb.getUserData(); // Key stored in userData
            if (userPermissions.contains(permissionKey)) {
                cb.setSelected(true);
            }
        }
    }

    private void populatePermissionCheckboxes() {
        permissionsVBox.getChildren().clear();
        permissionCheckBoxes.clear();
        for (String permissionKey : availablePermissions) {
            CheckBox cb = new CheckBox(MessageProvider.getString(permissionKey));
            cb.setUserData(permissionKey); // Store the key itself
            permissionCheckBoxes.add(cb);
            permissionsVBox.getChildren().add(cb);
        }
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        if (editableUser == null) { // Add mode
            editableUser = new UserDTO();
        }

        editableUser.setUsername(usernameField.getText().trim());
        editableUser.setFullName(fullNameField.getText().trim());
        editableUser.setRole(roleComboBox.getValue());
        editableUser.setActive(activeCheckBox.isSelected());
        // Password is handled by UserManagementController when calling createUser

        saved = true;
        closeDialog();
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();
        if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("usermanagement.validation.username.required"));
        }
        if (fullNameField.getText() == null || fullNameField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("usermanagement.validation.fullName.required"));
        }
        if (roleComboBox.getValue() == null || roleComboBox.getValue().isEmpty()) {
            errors.add(MessageProvider.getString("usermanagement.validation.role.required"));
        }

        if (!isEditMode) { // Password validation only for add mode in this form
            if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
                errors.add(MessageProvider.getString("usermanagement.validation.password.required"));
            } else if (passwordField.getText().length() < 8) { // Example length validation
                 errors.add(MessageProvider.getString("usermanagement.validation.password.length"));
            }
            if (confirmPasswordField.getText() == null || confirmPasswordField.getText().isEmpty()) {
                 errors.add(MessageProvider.getString("usermanagement.validation.confirmPassword.required"));
            }
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                errors.add(MessageProvider.getString("usermanagement.error.passwordMismatch"));
            }
        }

        if (!errors.isEmpty()) {
            showValidationErrorAlert(errors);
            return false;
        }
        return true;
    }

    private void showValidationErrorAlert(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(MessageProvider.getString("validation.general.errorTitle"));
        alert.setHeaderText(null);
        alert.setContentText(String.join("\n", errors));
        alert.showAndWait();
    }

    public boolean isSaved() {
        return saved;
    }

    public UserDTO getUserData() {
        return editableUser; // Contains the data collected from form
    }

    public String getPassword() {
        // Only relevant in add mode from this dialog's perspective
        return isEditMode ? null : passwordField.getText();
    }

    public List<String> getSelectedPermissions() {
        return permissionCheckBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (String) cb.getUserData()) // Get the permission key
                .collect(Collectors.toList());
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
             // Fallback for safety, though dialogStage should always be set by UserManagementController
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        }
    }
}
