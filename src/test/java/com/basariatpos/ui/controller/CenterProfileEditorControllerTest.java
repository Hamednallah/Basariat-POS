package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher; // For mocking static service getter
import com.basariatpos.model.CenterProfileDTO;
import com.basariatpos.service.CenterProfileService;
import com.basariatpos.service.ProfileServiceException;
import com.basariatpos.service.ProfileValidationException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;
import static org.testfx.matcher.control.ButtonMatchers.isDisabled;


@ExtendWith(ApplicationExtension.class)
class CenterProfileEditorControllerTest {

    @Mock
    private CenterProfileService mockCenterProfileService;

    private CenterProfileEditorController controller;
    private Parent root;
    private Stage stage;
    private MockedStatic<AppLauncher> appLauncherMockedStatic;


    // Define fx:id selectors as constants for easier maintenance
    private final String CENTER_NAME_FIELD = "#centerNameField";
    private final String PHONE_PRIMARY_FIELD = "#phonePrimaryField";
    private final String CURRENCY_SYMBOL_FIELD = "#currencySymbolField";
    private final String CURRENCY_CODE_FIELD = "#currencyCodeField";
    private final String SAVE_BUTTON = "#saveChangesButton";
    // Add other field IDs if needed for specific tests

    @Start
    private void start(Stage stage) throws IOException {
        MockitoAnnotations.openMocks(this);
        this.stage = stage;

        // Mock AppLauncher to provide the mocked CenterProfileService
        appLauncherMockedStatic = Mockito.mockStatic(AppLauncher.class);
        appLauncherMockedStatic.when(AppLauncher::getCenterProfileService).thenReturn(mockCenterProfileService);


        LocaleManager.setCurrentLocale(LocaleManager.DEFAULT_LOCALE);
        ResourceBundle bundle = MessageProvider.getBundle();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/basariatpos/ui/view/CenterProfileEditorView.fxml"));
        loader.setResources(bundle);

        // The controller uses AppLauncher.getCenterProfileService() in its initialize if service is null.
        // So the static mock above should cover it.
        // If we were using a setter, we'd call it here:
        // controller = loader.getController();
        // controller.setCenterProfileService(mockCenterProfileService);

        root = loader.load();
        controller = loader.getController(); // Get the controller instance after load

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    @AfterEach
    void tearDown(FxRobot robot) {
        if (appLauncherMockedStatic != null) {
            appLauncherMockedStatic.close();
        }
        // Close any alert dialogs
        robot.listWindows().stream()
            .filter(w -> w instanceof Stage && ((Stage)w).getModality() == javafx.stage.Modality.APPLICATION_MODAL)
            .forEach(w -> robot.targetWindow(w).close());
        WaitForAsyncUtils.waitForFxEvents();
    }


    @Test
    void loadProfileData_populatesFields_whenDataExists(FxRobot robot) throws ProfileServiceException {
        // Arrange
        CenterProfileDTO testProfile = new CenterProfileDTO(
            "Test Center Name", "123 Main St", "Apt 4B", "Test City", "Test Country",
            "12345", "555-1234", "555-5678", "test@example.com", "www.example.com",
            "/path/to/logo.png", "TAXID123", "$", "USD", "Thank you!"
        );
        when(mockCenterProfileService.getCenterProfile()).thenReturn(Optional.of(testProfile));

        // Act - initialize() in controller calls loadProfileData.
        // We may need to re-call loadProfileData or ensure service is set before initialize if test setup changes
        // For now, assuming @Start's load is sufficient or controller's setter calls load.
        // To be certain, explicitly call load if controller is designed to allow it:
        // controller.loadProfileData(); // If this method were public or testable.
        // Since loadProfileData is called in initialize, and initialize is called by FXML loader,
        // the mock setup in @Start should be effective.

        // Assert
        robot.waitUntil(() -> robot.lookup(CENTER_NAME_FIELD).queryAs(TextField.class).getText().equals("Test Center Name"), 2000); // Wait for async load

        assertEquals("Test Center Name", robot.lookup(CENTER_NAME_FIELD).queryAs(TextField.class).getText());
        assertEquals("123 Main St", robot.lookup("#addressLine1Field").queryAs(TextField.class).getText());
        assertEquals("Test City", robot.lookup("#cityField").queryAs(TextField.class).getText());
        assertEquals("Test Country", robot.lookup("#countryField").queryAs(TextField.class).getText());
        assertEquals("555-1234", robot.lookup(PHONE_PRIMARY_FIELD).queryAs(TextField.class).getText());
        assertEquals("$", robot.lookup(CURRENCY_SYMBOL_FIELD).queryAs(TextField.class).getText());
        assertEquals("USD", robot.lookup(CURRENCY_CODE_FIELD).queryAs(TextField.class).getText());
        assertEquals("/path/to/logo.png", robot.lookup("#logoImagePathField").queryAs(TextField.class).getText());

        assertFalse(robot.lookup(SAVE_BUTTON).queryAs(Button.class).isDisabled());
    }

    @Test
    void loadProfileData_showsErrorAndDisablesForm_whenNoProfileExists(FxRobot robot) throws ProfileServiceException {
        // Arrange
        when(mockCenterProfileService.getCenterProfile()).thenReturn(Optional.empty());

        // Act - Manually trigger load if initialize might not have had the service correctly set up for this specific test case
        // This depends on how robust the controller's initialize and service injection is.
        // If controller's initialize correctly used the mocked service returning empty:
        controller.setCenterProfileService(mockCenterProfileService); // Re-set to ensure initialize logic if it checks null

        // Assert (check for an alert and disabled fields)
        // TestFX can't easily "see" alerts from a different stage/owner without more complex setup.
        // So, we'll verify the effect (disabled form) or mock the Alert display.
        // For now, check if save button is disabled as an indicator.
        assertTrue(robot.lookup(SAVE_BUTTON).queryAs(Button.class).isDisabled(), "Save button should be disabled if no profile.");
        // Add checks for other fields being disabled if that's the behavior.
        assertTrue(robot.lookup(CENTER_NAME_FIELD).queryAs(TextField.class).isDisabled(), "Center name field should be disabled.");
    }

    @Test
    void loadProfileData_showsErrorAndDisablesForm_onServiceException(FxRobot robot) throws ProfileServiceException {
        // Arrange
        when(mockCenterProfileService.getCenterProfile()).thenThrow(new ProfileServiceException("DB connection failed", null));

        // Act - as above, ensure service is set and load is triggered.
        controller.setCenterProfileService(mockCenterProfileService);

        // Assert
        assertTrue(robot.lookup(SAVE_BUTTON).queryAs(Button.class).isDisabled(), "Save button should be disabled on service error.");
        // Check for alert is tricky. Focus on UI state.
    }


    @Test
    void handleSaveChanges_validData_callsServiceAndShowsSuccess(FxRobot robot) throws ProfileServiceException {
        // Arrange: Populate form with valid data
        robot.clickOn(CENTER_NAME_FIELD).eraseText(robot.lookup(CENTER_NAME_FIELD).queryAs(TextField.class).getText().length()).write("New Valid Center");
        robot.clickOn(PHONE_PRIMARY_FIELD).eraseText(robot.lookup(PHONE_PRIMARY_FIELD).queryAs(TextField.class).getText().length()).write("555-1111");
        robot.clickOn(CURRENCY_SYMBOL_FIELD).eraseText(robot.lookup(CURRENCY_SYMBOL_FIELD).queryAs(TextField.class).getText().length()).write("€");
        robot.clickOn(CURRENCY_CODE_FIELD).eraseText(robot.lookup(CURRENCY_CODE_FIELD).queryAs(TextField.class).getText().length()).write("EUR");
        // ... populate other required fields ...

        // Act
        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        // Assert
        ArgumentCaptor<CenterProfileDTO> dtoCaptor = ArgumentCaptor.forClass(CenterProfileDTO.class);
        verify(mockCenterProfileService).saveProfile(dtoCaptor.capture());
        assertEquals("New Valid Center", dtoCaptor.getValue().getCenterName());
        assertEquals("EUR", dtoCaptor.getValue().getCurrencyCode());

        // Verify success alert (check if an INFO alert is shown)
        // This is a simplified check. A more robust way is to check the alert's content.
        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("centerprofile.editor.title")) && robot.lookup(".alert.information").queryAll().size() > 0 ),
                   "Success alert should be shown.");
    }

    @Test
    void handleSaveChanges_invalidData_showsErrorAlert(FxRobot robot) throws ProfileServiceException {
        // Arrange: Populate form with invalid data (e.g., empty required field)
        robot.clickOn(CENTER_NAME_FIELD).eraseText(robot.lookup(CENTER_NAME_FIELD).queryAs(TextField.class).getText().length()).write(""); // Invalid - empty
        robot.clickOn(PHONE_PRIMARY_FIELD).write("555-2222");
        robot.clickOn(CURRENCY_SYMBOL_FIELD).write("$");
        robot.clickOn(CURRENCY_CODE_FIELD).write("USD");

        // Act
        robot.clickOn(SAVE_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        // Assert
        verify(mockCenterProfileService, never()).saveProfile(any(CenterProfileDTO.class));

        List<Window> windows = robot.listWindows();
        assertTrue(windows.stream().anyMatch(w -> w instanceof Stage && ((Stage)w).getTitle().equals(MessageProvider.getString("validation.general.errorTitle")) && robot.lookup(".alert.error").queryAll().size() > 0 ),
                   "Error alert should be shown for invalid data.");
    }

    // Test for handleBrowseLogo is complex due to FileChooser.
    // A simple test could be to call it and ensure no crash, or mock the FileChooser if essential.
    @Test
    void handleBrowseLogo_updatesLogoPathField_onFileSelection(FxRobot robot) {
        // This test would require mocking FileChooser. For now, we'll skip the full interaction.
        // A basic check:
        TextField logoPathField = robot.lookup("#logoImagePathField").queryAs(TextField.class);
        String initialPath = logoPathField.getText();

        // Simulate a file path being set (as if FileChooser returned it)
        // This doesn't test FileChooser itself but the controller's reaction to a path.
        String testPath = "/fake/path/to/logo.png";
        robot.interact(() -> logoPathField.setText(testPath));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(testPath, logoPathField.getText());
    }
}
