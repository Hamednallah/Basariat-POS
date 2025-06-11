package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher; // Assuming AppLauncher provides UserService instance
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.UserException;
import com.basariatpos.service.UserService;
import com.basariatpos.service.UserServiceImpl; // Temporary for direct instantiation
import com.basariatpos.repository.UserRepositoryImpl; // Temporary for direct instantiation
import com.basariatpos.util.AppLogger;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(UserManagementController.class);

    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, String> usernameColumn;
    @FXML private TableColumn<UserDTO, String> fullNameColumn;
    @FXML private TableColumn<UserDTO, String> roleColumn;
    @FXML private TableColumn<UserDTO, String> statusColumn;

    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button toggleActivityButton;
    @FXML private Button resetPasswordButton;

    private UserService userService;
    private final ObservableList<UserDTO> userObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // In a real app, UserService would be injected by a DI framework.
        // For Sprint 0, we might get it from AppLauncher or instantiate directly.
        // userService = AppLauncher.getUserService(); // Ideal
        userService = new UserServiceImpl(new UserRepositoryImpl()); // Temporary direct instantiation

        setupTableColumns();
        loadUsers();

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean userSelected = newSelection != null;
            editUserButton.setDisable(!userSelected);
            toggleActivityButton.setDisable(!userSelected);
            resetPasswordButton.setDisable(!userSelected);
        });

        // Set button graphics if using FontAwesome or similar (later task)
        logger.info("UserManagementController initialized.");
    }

    // Public setter for UserService if needed for external initialization (e.g. by AppLauncher)
    public void setUserService(UserService userService) {
        this.userService = userService;
        // if already initialized, reload users
        if (usersTable != null) {
            loadUsers();
        }
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            String statusKey = isActive ? "usermanagement.status.active" : "usermanagement.status.inactive";
            return new SimpleStringProperty(MessageProvider.getString(statusKey));
        });
    }

    private void loadUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            userObservableList.setAll(users);
            usersTable.setItems(userObservableList);
            logger.info("Users loaded into table. Count: {}", users.size());
        } catch (UserException e) {
            logger.error("Failed to load users: {}", e.getMessage(), e);
            showErrorAlert("Error Loading Users", "Could not retrieve user list: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddUserButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/UserFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            UserFormDialogController controller = loader.getController();
            controller.setUserService(userService); // Pass service
            // controller.setAvailablePermissions(...); // Pass available permissions if dynamic

            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("usermanagement.dialog.addUser.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(((Button)event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(dialogRoot));

            controller.getDialogStage(dialogStage); // Pass stage to controller for closing

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                UserDTO newUser = controller.getUserData();
                List<String> permissions = controller.getSelectedPermissions();
                try {
                    UserDTO createdUser = userService.createUser(newUser, controller.getPassword()); // Assuming getPassword method in dialog
                    for (String perm : permissions) {
                        userService.grantPermission(createdUser.getUserId(), perm);
                    }
                    showSuccessAlert(MessageProvider.getString("usermanagement.success.userAdded"));
                    loadUsers(); // Refresh table
                } catch (Exception e) {
                    logger.error("Error adding user: {}", e.getMessage(), e);
                    showErrorAlert("Error Adding User", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load UserFormDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the add user form.");
        }
    }

    @FXML
    private void handleEditUserButtonAction(ActionEvent event) {
        UserDTO selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showErrorAlert("No User Selected", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/UserFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            UserFormDialogController controller = loader.getController();
            controller.setUserService(userService);
            List<String> currentPermissions = userService.getUserPermissions(selectedUser.getUserId());
            controller.setEditableUser(selectedUser, currentPermissions);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("usermanagement.dialog.editUser.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(((Button)event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(dialogRoot));
            controller.getDialogStage(dialogStage);


            dialogStage.showAndWait();

            if (controller.isSaved()) {
                UserDTO updatedUser = controller.getUserData();
                List<String> newPermissions = controller.getSelectedPermissions();
                try {
                    userService.updateUserDetails(updatedUser);
                    // Update permissions: revoke all, then grant selected. Or more fine-grained.
                    List<String> oldPermissions = userService.getUserPermissions(updatedUser.getUserId());
                    for(String p : oldPermissions) if(!newPermissions.contains(p)) userService.revokePermission(updatedUser.getUserId(), p);
                    for(String p : newPermissions) if(!oldPermissions.contains(p)) userService.grantPermission(updatedUser.getUserId(), p);

                    showSuccessAlert(MessageProvider.getString("usermanagement.success.userUpdated"));
                    loadUsers(); // Refresh table
                } catch (Exception e) {
                    logger.error("Error updating user: {}", e.getMessage(), e);
                    showErrorAlert("Error Updating User", e.getMessage());
                }
            }
        } catch (Exception e) { // Catch UserException for permissions or IOException for FXML
            logger.error("Failed to load UserFormDialog.fxml for edit: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the edit user form: " + e.getMessage());
        }
    }

    @FXML
    private void handleToggleActivityButtonAction(ActionEvent event) {
        UserDTO selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
             showErrorAlert("No User Selected", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }

        String confirmTitle = MessageProvider.getString("usermanagement.confirm.toggleActive.title");
        String confirmContent = selectedUser.isActive() ?
                                MessageProvider.getString("usermanagement.confirm.toggleActive.contentActive") :
                                MessageProvider.getString("usermanagement.confirm.toggleActive.contentInactive");

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, confirmContent, ButtonType.YES, ButtonType.NO);
        confirmation.setTitle(confirmTitle);
        confirmation.setHeaderText(null);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                if (selectedUser.isActive()) {
                    userService.deactivateUser(selectedUser.getUserId());
                } else {
                    userService.activateUser(selectedUser.getUserId());
                }
                showSuccessAlert(MessageProvider.getString("usermanagement.success.statusChanged"));
                loadUsers();
            } catch (UserException e) {
                logger.error("Error changing user status for user ID {}: {}", selectedUser.getUserId(), e.getMessage(), e);
                showErrorAlert("Error Changing Status", e.getMessage());
            }
        }
    }

    @FXML
    private void handleResetPasswordButtonAction(ActionEvent event) {
        UserDTO selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showErrorAlert("No User Selected", MessageProvider.getString("usermanagement.validation.selection.required"));
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/ResetPasswordDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            ResetPasswordDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(MessageProvider.getString("usermanagement.dialog.resetPassword.title") + " - " + selectedUser.getUsername());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(((Button)event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(dialogRoot));
            controller.setDialogStage(dialogStage); // Pass stage to controller

            dialogStage.showAndWait();

            if (controller.isPasswordSaved()) {
                String newPassword = controller.getNewPassword();
                try {
                    userService.changePassword(selectedUser.getUserId(), newPassword);
                    showSuccessAlert(MessageProvider.getString("usermanagement.success.passwordReset"));
                    // No table refresh needed as password change is not visible in table
                } catch (Exception e) {
                    logger.error("Error resetting password for user {}: {}", selectedUser.getUsername(), e.getMessage(), e);
                    showErrorAlert("Error Resetting Password", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load ResetPasswordDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the reset password form.");
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("usermanagement.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
