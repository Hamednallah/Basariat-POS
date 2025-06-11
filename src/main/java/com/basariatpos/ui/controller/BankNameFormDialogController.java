package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.BankNameDTO;
import com.basariatpos.service.BankNameAlreadyExistsException;
import com.basariatpos.service.BankNameException;
import com.basariatpos.service.BankNameService;
import com.basariatpos.service.ValidationException; // Assuming this is defined, e.g. in UserService or a common place
import com.basariatpos.util.AppLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BankNameFormDialogController implements Initializable {

    private static final Logger logger = AppLogger.getLogger(BankNameFormDialogController.class);

    @FXML private Label dialogTitleLabel;
    @FXML private TextField nameEnField;
    @FXML private TextField nameArField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private BankNameService bankNameService;
    private BankNameDTO editableBankName;
    private boolean isEditMode = false;
    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activeCheckBox.setSelected(true); // Default for new bank name
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setBankNameService(BankNameService bankNameService) {
        this.bankNameService = bankNameService;
    }

    public void setEditableBankName(BankNameDTO bankName) {
        this.editableBankName = bankName;
        this.isEditMode = true;

        dialogTitleLabel.setText(MessageProvider.getString("bankname.dialog.edit.title"));
        nameEnField.setText(bankName.getBankNameEn());
        nameArField.setText(bankName.getBankNameAr());
        activeCheckBox.setSelected(bankName.isActive());
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        String nameEn = nameEnField.getText().trim();
        String nameAr = nameArField.getText().trim();
        boolean isActive = activeCheckBox.isSelected();

        BankNameDTO bankToSave;
        if (isEditMode) {
            bankToSave = editableBankName;
            bankToSave.setBankNameEn(nameEn);
            bankToSave.setBankNameAr(nameAr);
            bankToSave.setActive(isActive);
        } else {
            bankToSave = new BankNameDTO(nameEn, nameAr, isActive);
        }

        try {
            bankNameService.saveBankName(bankToSave);
            saved = true;
            closeDialog();
        } catch (ValidationException e) {
            logger.warn("Validation error saving bank name: {}", e.getErrors());
            showValidationErrorAlert(e.getErrors());
        } catch (BankNameAlreadyExistsException e) {
            logger.warn("Bank name already exists: {}", e.getMessage());
            List<String> errors = new ArrayList<>();
            if (e.getMessage().contains("English name")) { // Crude check, make exception more specific if needed
                errors.add(MessageProvider.getString("bankname.validation.nameEn.exists"));
            } else if (e.getMessage().contains("Arabic")) {
                 errors.add(MessageProvider.getString("bankname.validation.nameAr.exists"));
            } else {
                errors.add(e.getMessage());
            }
            showValidationErrorAlert(errors);
        } catch (BankNameException e) {
            logger.error("Error saving bank name: {}", e.getMessage(), e);
            showErrorAlert(MessageProvider.getString("bankname.error.generic"), e.getMessage());
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        saved = false;
        closeDialog();
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();
        if (nameEnField.getText() == null || nameEnField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("bankname.validation.nameEn.required"));
        }
        if (nameArField.getText() == null || nameArField.getText().trim().isEmpty()) {
            errors.add(MessageProvider.getString("bankname.validation.nameAr.required"));
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
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }


    public boolean isSaved() {
        return saved;
    }

    // No need to return DTO, parent controller will reload list
    // public BankNameDTO getBankNameData() { return editableBankName; }


    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            Stage stage = (Stage) saveButton.getScene().getWindow(); // Fallback
            stage.close();
        }
    }
}
