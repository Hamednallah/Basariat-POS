package com.basariatpos.ui.controller;

import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.OpticalDiagnosticDTO;
import com.basariatpos.service.DiagnosticException;
import com.basariatpos.service.OpticalDiagnosticService;
import com.basariatpos.util.AppLogger;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class OpticalDiagnosticHistoryDialogController {

    private static final Logger logger = AppLogger.getLogger(OpticalDiagnosticHistoryDialogController.class);

    @FXML private Label patientNameLabel;
    @FXML private TableView<OpticalDiagnosticDTO> diagnosticsTable;
    @FXML private TableColumn<OpticalDiagnosticDTO, String> dateColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, BigDecimal> odSphColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, BigDecimal> odCylColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, Integer> odAxisColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, BigDecimal> osSphColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, BigDecimal> osCylColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, Integer> osAxisColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, BigDecimal> odAddColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, BigDecimal> osAddColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, BigDecimal> ipdColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, String> isCLRxColumn;
    @FXML private TableColumn<OpticalDiagnosticDTO, String> remarksColumn;

    @FXML private Button addDiagnosticButton;
    @FXML private Button editDiagnosticButton;
    @FXML private Button deleteDiagnosticButton;
    @FXML private Button closeButton;

    private Stage dialogStage;
    private OpticalDiagnosticService diagnosticService;
    private int currentPatientId;
    private String currentPatientName;
    private final ObservableList<OpticalDiagnosticDTO> diagnosticObservableList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public void initializeDialog(OpticalDiagnosticService service, int patientId, String patientName, Stage stage) {
        this.diagnosticService = service;
        this.currentPatientId = patientId;
        this.currentPatientName = patientName;
        this.dialogStage = stage;

        // Update title label if it's meant to be dynamic with patient name in history view itself
        // The FXML title key `opticaldiagnostics.history.title` has a placeholder {0}
        // This can be set on the Stage title by the caller (PatientManagementController)
        // For the label inside the dialog:
        patientNameLabel.setText(MessageProvider.getString("opticaldiagnostics.history.title", patientName));


        setupTableColumns();
        loadDiagnostics();

        diagnosticsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean itemSelected = newSelection != null;
            editDiagnosticButton.setDisable(!itemSelected);
            deleteDiagnosticButton.setDisable(!itemSelected);
        });
        logger.info("OpticalDiagnosticHistoryDialogController initialized for patient ID: {}", patientId);
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDiagnosticDate().format(dateFormatter))
        );
        odSphColumn.setCellValueFactory(new PropertyValueFactory<>("odSphDist"));
        odCylColumn.setCellValueFactory(new PropertyValueFactory<>("odCylDist"));
        odAxisColumn.setCellValueFactory(new PropertyValueFactory<>("odAxisDist"));
        osSphColumn.setCellValueFactory(new PropertyValueFactory<>("osSphDist"));
        osCylColumn.setCellValueFactory(new PropertyValueFactory<>("osCylDist"));
        osAxisColumn.setCellValueFactory(new PropertyValueFactory<>("osAxisDist"));
        odAddColumn.setCellValueFactory(new PropertyValueFactory<>("odAdd"));
        osAddColumn.setCellValueFactory(new PropertyValueFactory<>("osAdd"));
        ipdColumn.setCellValueFactory(new PropertyValueFactory<>("ipd"));
        isCLRxColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isContactLensRx() ?
                                     MessageProvider.getString("patientmanagement.consent.yes") :
                                     MessageProvider.getString("patientmanagement.consent.no"))
        );
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));
    }

    private void loadDiagnostics() {
        try {
            List<OpticalDiagnosticDTO> diagnostics = diagnosticService.getDiagnosticsForPatient(currentPatientId);
            diagnosticObservableList.setAll(diagnostics);
            diagnosticsTable.setItems(diagnosticObservableList);
            logger.info("Diagnostics loaded for patient ID {}. Count: {}", currentPatientId, diagnostics.size());
        } catch (DiagnosticServiceException e) {
            logger.error("Failed to load diagnostics for patient ID {}: {}", currentPatientId, e.getMessage(), e);
            showErrorAlert("Error Loading Diagnostics", "Could not load diagnostic history: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddDiagnosticButtonAction(ActionEvent event) {
        showDiagnosticFormDialog(null);
    }

    @FXML
    private void handleEditDiagnosticButtonAction(ActionEvent event) {
        OpticalDiagnosticDTO selectedDiagnostic = diagnosticsTable.getSelectionModel().getSelectedItem();
        if (selectedDiagnostic == null) {
            showErrorAlert("No Selection", "Please select a diagnostic record to edit.");
            return;
        }
        showDiagnosticFormDialog(selectedDiagnostic);
    }

    private void showDiagnosticFormDialog(OpticalDiagnosticDTO diagnostic) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/OpticalDiagnosticFormDialog.fxml"));
            loader.setResources(MessageProvider.getBundle());
            Parent dialogRoot = loader.load();

            OpticalDiagnosticFormDialogController controller = loader.getController();
            // Pass patientId for context, even if diagnostic is null (for add mode)
            controller.initializeDialog(this.diagnosticService, dialogStage, diagnostic, this.currentPatientId);

            Stage formDialogStage = new Stage();
            formDialogStage.setTitle(diagnostic == null ?
                                 MessageProvider.getString("opticaldiagnostics.form.add.title") :
                                 MessageProvider.getString("opticaldiagnostics.form.edit.title"));
            formDialogStage.initModality(Modality.WINDOW_MODAL); // Modal to this history dialog
            formDialogStage.initOwner(this.dialogStage); // Owner is the history dialog
            formDialogStage.setScene(new Scene(dialogRoot));
            controller.setDialogStage(formDialogStage); // Pass the new stage to the form controller

            formDialogStage.showAndWait();

            if (controller.isSaved()) {
                loadDiagnostics(); // Refresh table
                String successKey = (diagnostic == null) ? "opticaldiagnostics.success.recorded" : "opticaldiagnostics.success.updated";
                showSuccessAlert(MessageProvider.getString(successKey));
            }
        } catch (IOException e) {
            logger.error("Failed to load OpticalDiagnosticFormDialog.fxml: {}", e.getMessage(), e);
            showErrorAlert("UI Error", "Could not open the diagnostic form.");
        }
    }

    @FXML
    private void handleDeleteDiagnosticButtonAction(ActionEvent event) {
        OpticalDiagnosticDTO selectedDiagnostic = diagnosticsTable.getSelectionModel().getSelectedItem();
        if (selectedDiagnostic == null) {
            showErrorAlert("No Selection", "Please select a diagnostic record to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                                       MessageProvider.getString("opticaldiagnostics.confirm.delete.content"),
                                       ButtonType.YES, ButtonType.NO);
        confirmation.setTitle(MessageProvider.getString("opticaldiagnostics.confirm.delete.title"));
        confirmation.setHeaderText(null);
        confirmation.initOwner(this.dialogStage);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                diagnosticService.deleteDiagnostic(selectedDiagnostic.getDiagnosticId());
                showSuccessAlert(MessageProvider.getString("opticaldiagnostics.success.deleted"));
                loadDiagnostics();
            } catch (DiagnosticException e) {
                logger.error("Error deleting diagnostic ID {}: {}", selectedDiagnostic.getDiagnosticId(), e.getMessage(), e);
                showErrorAlert("Error Deleting Diagnostic", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCloseButtonAction(ActionEvent event) {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MessageProvider.getString("opticaldiagnostics.history.title", currentPatientName)); // Use patient name in title
        alert.setHeaderText(null);
        alert.setContentText(message);
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
}
