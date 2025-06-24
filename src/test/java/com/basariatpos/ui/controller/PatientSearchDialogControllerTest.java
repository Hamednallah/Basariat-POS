package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.PatientDTO;
import com.basariatpos.service.PatientService;
import com.basariatpos.service.exception.PatientServiceException;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class PatientSearchDialogControllerTest {

    @Mock
    private PatientService mockPatientService;

    private PatientSearchDialogController controller;
    private Stage stage;

    @BeforeAll
    static void setUpClass() throws Exception {
        LocaleManager.setCurrentLocale(Locale.ENGLISH);
        MessageProvider.loadBundle(LocaleManager.getCurrentLocale());
         if (System.getProperty("os.name", "").toLowerCase().startsWith("linux")) {
            System.setProperty("java.awt.headless", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (stage != null && stage.isShowing()) {
             org.testfx.api.FxToolkit.cleanupStages();
        }
    }

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        MockitoAnnotations.openMocks(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/PatientSearchDialog.fxml"));
        loader.setResources(MessageProvider.getBundle());
        Parent root = loader.load();
        controller = loader.getController();
        controller.setServices(mockPatientService);
        controller.setDialogStage(stage);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void handleSearchPatientButtonAction_callsServiceAndPopulatesList(FxRobot robot) throws PatientServiceException {
        List<PatientDTO> patients = new ArrayList<>();
        PatientDTO patient1 = new PatientDTO(); patient1.setPatientId(1); patient1.setFullNameEn("John Doe"); patient1.setSystemPatientId("P001");
        patients.add(patient1);

        when(mockPatientService.searchPatients(anyString())).thenReturn(patients);

        robot.clickOn("#patientSearchInput").write("John");
        robot.clickOn("#searchPatientBtn");
        WaitForAsyncUtils.waitForFxEvents();

        ListView<PatientDTO> patientList = robot.lookup("#patientResultsList").queryListView();
        assertEquals(1, patientList.getItems().size());
        assertEquals("John Doe (ID: P001)", patientList.getItems().get(0).getDisplayFullNameWithId()); // Assumes getDisplayFullNameWithId exists
        verify(mockPatientService).searchPatients("John");
    }

    @Test
    void handleSearchPatientButtonAction_emptyQuery_showsWarning(FxRobot robot) {
         try (var alertMock = mockConstruction(javafx.scene.control.Alert.class)) {
            robot.clickOn("#searchPatientBtn"); // Click with empty input
            WaitForAsyncUtils.waitForFxEvents();

            assertTrue(alertMock.constructed().size() >= 1);
            assertEquals(javafx.scene.control.Alert.AlertType.WARNING, alertMock.constructed().get(0).getAlertType());
        }
    }


    @Test
    void handleSelectPatientButtonAction_noSelection_showsWarning(FxRobot robot) {
        // Ensure no selection
        robot.interact(() -> robot.lookup("#patientResultsList").queryListView().getSelectionModel().clearSelection());

        try (var alertMock = mockConstruction(javafx.scene.control.Alert.class)) {
            robot.clickOn("#selectPatientButton");
            WaitForAsyncUtils.waitForFxEvents();

            assertTrue(alertMock.constructed().size() >= 1);
            assertEquals(javafx.scene.control.Alert.AlertType.WARNING, alertMock.constructed().get(0).getAlertType());
            assertTrue(alertMock.constructed().get(0).getContentText().contains("Please select a patient"));
        }
        assertNull(controller.getSelectedPatient()); // No patient should be selected
        assertTrue(stage.isShowing()); // Dialog should remain open
    }

    @Test
    void handleSelectPatientButtonAction_withSelection_setsPatientAndCloses(FxRobot robot) throws PatientServiceException {
        PatientDTO patient1 = new PatientDTO(); patient1.setPatientId(1); patient1.setFullNameEn("Jane Doe"); patient1.setSystemPatientId("P002");
        // Populate list for selection
        robot.interact(() -> controller.patientData.add(patient1));
        WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> robot.lookup("#patientResultsList").queryListView().getSelectionModel().select(patient1));
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#selectPatientButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(controller.getSelectedPatient());
        assertEquals(1, controller.getSelectedPatient().getPatientId());
        assertFalse(stage.isShowing()); // Dialog should close
    }

    @Test
    void handleCancelButtonAction_closesDialogAndNullifiesSelection(FxRobot robot) {
        robot.clickOn("#cancelSearchButton");
        WaitForAsyncUtils.waitForFxEvents();

        assertNull(controller.getSelectedPatient());
        assertFalse(stage.isShowing());
    }
}
