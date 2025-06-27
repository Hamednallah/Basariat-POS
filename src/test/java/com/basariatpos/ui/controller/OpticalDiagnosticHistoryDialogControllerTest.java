package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.OpticalDiagnosticDTO;
import com.basariatpos.service.DiagnosticServiceException;
import com.basariatpos.service.OpticalDiagnosticService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OpticalDiagnosticHistoryDialogControllerTest {

    @Mock private Label patientNameLabel;
    @Mock private TableView<OpticalDiagnosticDTO> diagnosticsTable;
    // Mock individual columns if their setup needs specific verification beyond PropertyValueFactory
    @Mock private TableColumn<OpticalDiagnosticDTO, String> dateColumn;
    @Mock private BorderPane diagnosticHistoryRootPane; // For RTL
    @Mock private Stage mockDialogStage;
    @Mock private OpticalDiagnosticService mockDiagnosticService;

    @Spy
    private ObservableList<OpticalDiagnosticDTO> diagnosticObservableList = FXCollections.observableArrayList();

    @InjectMocks
    private OpticalDiagnosticHistoryDialogController controller;

    private static ResourceBundle resourceBundle;
    private final int testPatientId = 1;
    private final String testPatientName = "Test Patient";

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
        // Manual FXML injection
        controller.patientNameLabel = patientNameLabel;
        controller.diagnosticsTable = diagnosticsTable;
        controller.dateColumn = dateColumn; // Example, mock others if directly used
        controller.diagnosticHistoryRootPane = diagnosticHistoryRootPane;

        // For PropertyValueFactory columns, we usually don't mock them directly unless testing cell factories
        // Initialize them here if they are null, to avoid NPEs if setupTableColumns is called.
        controller.odSphColumn = new TableColumn<>();
        controller.odCylColumn = new TableColumn<>();
        controller.odAxisColumn = new TableColumn<>();
        controller.osSphColumn = new TableColumn<>();
        controller.osCylColumn = new TableColumn<>();
        controller.osAxisColumn = new TableColumn<>();
        controller.odAddColumn = new TableColumn<>();
        controller.osAddColumn = new TableColumn<>();
        controller.ipdColumn = new TableColumn<>();
        controller.isCLRxColumn = new TableColumn<>();
        controller.remarksColumn = new TableColumn<>();


        when(diagnosticsTable.getItems()).thenReturn(diagnosticObservableList);
        // Mock selection model for listeners
        TableView.TableViewSelectionModel<OpticalDiagnosticDTO> mockSelectionModel = mock(TableView.TableViewSelectionModel.class);
        when(diagnosticsTable.getSelectionModel()).thenReturn(mockSelectionModel);


        // Call initializeDialog as it's the entry point for this controller
        controller.initializeDialog(mockDiagnosticService, testPatientId, testPatientName, mockDialogStage);
    }

    @Test
    void initializeDialog_setsPatientNameAndLoadsDiagnostics() throws DiagnosticServiceException {
        verify(patientNameLabel).setText(MessageProvider.getString("opticaldiagnostics.history.forPatient", testPatientName));
        verify(diagnosticHistoryRootPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);

        List<OpticalDiagnosticDTO> testDiagnostics = List.of(
            createSampleDiagnostic(LocalDate.of(2023, 1, 10), "-1.00")
        );
        when(mockDiagnosticService.getDiagnosticsForPatient(testPatientId)).thenReturn(testDiagnostics);

        controller.loadDiagnostics(); // Called by initializeDialog, or call again for verification

        assertEquals(1, diagnosticObservableList.size());
        assertEquals(new BigDecimal("-1.00"), diagnosticObservableList.get(0).getOdSphDist());
        // Verify column setup (at least one to confirm setupTableColumns was effective)
        verify(dateColumn).setCellValueFactory(any());
    }

    @Test
    void loadDiagnostics_handlesServiceException() throws DiagnosticServiceException {
        when(mockDiagnosticService.getDiagnosticsForPatient(testPatientId))
            .thenThrow(new DiagnosticServiceException("DB Error"));
        when(diagnosticsTable.getScene()).thenReturn(mock(javafx.scene.Scene.class)); // For showErrorAlert
        when(diagnosticsTable.getScene().getWindow()).thenReturn(mockDialogStage);


        assertDoesNotThrow(() -> controller.loadDiagnostics());
        assertTrue(diagnosticObservableList.isEmpty());
    }

    // Tests for add/edit/delete buttons would require mocking FXMLLoader and dialog interactions,
    // similar to UserManagementController tests.

    @Test
    void initializeDialog_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle(); // Reload bundle

        // Re-initialize or call a method that updates orientation
        controller.initializeDialog(mockDiagnosticService, testPatientId, testPatientName, mockDialogStage);

        verify(diagnosticHistoryRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }

    private OpticalDiagnosticDTO createSampleDiagnostic(LocalDate date, String odSph) {
        OpticalDiagnosticDTO dto = new OpticalDiagnosticDTO();
        dto.setDiagnosticDate(date);
        if (odSph != null) dto.setOdSphDist(new BigDecimal(odSph));
        // ... set other fields if needed for specific tests
        return dto;
    }
}
