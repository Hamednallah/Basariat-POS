package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.main.AppLauncher;
import com.basariatpos.model.CenterProfileDTO;
import com.basariatpos.service.CenterProfileService;
import com.basariatpos.service.ProfileServiceException;
import com.basariatpos.service.ProfileValidationException;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane; // For rootPane

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CenterProfileEditorControllerTest {

    @Mock private CenterProfileService mockCenterProfileService;
    @Mock private TextField centerNameField;
    @Mock private TextField addressLine1Field;
    @Mock private TextField phonePrimaryField;
    @Mock private TextField currencySymbolField;
    @Mock private TextField currencyCodeField;
    // Add mocks for other @FXML fields if their interactions are specifically tested
    @Mock private TextArea receiptFooterMessageArea;
    @Mock private Button saveChangesButton;
    @Mock private Button browseLogoButton;
    @Mock private AnchorPane centerProfileEditorRootPane;


    @InjectMocks
    private CenterProfileEditorController controller;

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
        // Manual injection of @FXML mocks
        controller.centerNameField = centerNameField;
        controller.addressLine1Field = addressLine1Field;
        controller.phonePrimaryField = phonePrimaryField;
        controller.currencySymbolField = currencySymbolField;
        controller.currencyCodeField = currencyCodeField;
        controller.receiptFooterMessageArea = receiptFooterMessageArea;
        controller.saveChangesButton = saveChangesButton;
        controller.browseLogoButton = browseLogoButton;
        controller.centerProfileEditorRootPane = centerProfileEditorRootPane;

        // Mock AppLauncher to provide the service
        mockAppLauncherStatic = Mockito.mockStatic(AppLauncher.class);
        mockAppLauncherStatic.when(AppLauncher::getCenterProfileService).thenReturn(mockCenterProfileService);

        // Call initialize after mocks are ready, AppLauncher is mocked
        // controller.initialize(null, resourceBundle);
        // For this controller, service can also be set by setter.
        // Let's simulate that MainFrameController sets it.
        controller.setCenterProfileService(mockCenterProfileService);
        controller.initialize(null, resourceBundle);
    }

    @AfterEach
    void tearDown() {
        mockAppLauncherStatic.close();
    }

    @Test
    void initialize_loadsProfile_whenServiceReturnsProfile() throws ProfileServiceException {
        CenterProfileDTO profile = new CenterProfileDTO();
        profile.setCenterName("Test Clinic");
        when(mockCenterProfileService.getCenterProfile()).thenReturn(Optional.of(profile));

        // initialize is called in setUp, which calls loadProfileData
        // To be certain, call loadProfileData again or ensure setUp calls initialize AFTER service is set
        controller.loadProfileData();

        verify(centerNameField).setText("Test Clinic");
        verify(saveChangesButton, atLeastOnce()).setDisable(false); // Check if it's enabled
        verify(centerProfileEditorRootPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void initialize_disablesForm_whenNoProfileFound() throws ProfileServiceException {
        when(mockCenterProfileService.getCenterProfile()).thenReturn(Optional.empty());
        // Ensure relevant fields are mocked if disableFormFields tries to access them
        when(centerNameField.getScene()).thenReturn(mock(javafx.scene.Scene.class)); // For getStage in showErrorAlert

        controller.loadProfileData();


        // Check if a key field and save button are disabled
        verify(centerNameField, atLeastOnce()).setDisable(true);
        verify(saveChangesButton, atLeastOnce()).setDisable(true);
    }

    @Test
    void initialize_handlesServiceExceptionOnLoad() throws ProfileServiceException {
        when(mockCenterProfileService.getCenterProfile()).thenThrow(new ProfileServiceException("DB Load Error"));
        when(centerNameField.getScene()).thenReturn(mock(javafx.scene.Scene.class)); // For getStage in showErrorAlert

        assertDoesNotThrow(() -> controller.loadProfileData());

        verify(centerNameField, atLeastOnce()).setDisable(true);
        verify(saveChangesButton, atLeastOnce()).setDisable(true);
    }


    @Test
    void handleSaveChanges_validData_callsService() throws ProfileServiceException, ProfileValidationException {
        when(centerNameField.getText()).thenReturn("Valid Name");
        when(phonePrimaryField.getText()).thenReturn("1234567890");
        when(currencySymbolField.getText()).thenReturn("$");
        when(currencyCodeField.getText()).thenReturn("USD");
        // Mock other fields as necessary for collectFormData()
        when(receiptFooterMessageArea.getText()).thenReturn("Thank you");
        when(centerNameField.getScene()).thenReturn(mock(javafx.scene.Scene.class)); // For getStage in showSuccessAlert

        controller.handleSaveChanges(null);

        ArgumentCaptor<CenterProfileDTO> captor = ArgumentCaptor.forClass(CenterProfileDTO.class);
        verify(mockCenterProfileService).saveProfile(captor.capture());
        assertEquals("Valid Name", captor.getValue().getCenterName());
        assertEquals("Thank you", captor.getValue().getReceiptFooterMessage());
    }

    @Test
    void handleSaveChanges_invalidData_showsError() {
        when(centerNameField.getText()).thenReturn(""); // Invalid
        when(phonePrimaryField.getText()).thenReturn("1234567890");
        when(currencySymbolField.getText()).thenReturn("$");
        when(currencyCodeField.getText()).thenReturn("USD");
        when(centerNameField.getScene()).thenReturn(mock(javafx.scene.Scene.class)); // For getStage in showErrorAlert


        controller.handleSaveChanges(null);

        verify(mockCenterProfileService, never()).saveProfile(any(CenterProfileDTO.class));
        // Verify error alert was shown (difficult without TestFX)
    }

    @Test
    void initialize_setsRTL_forArabicLocale() throws ProfileServiceException {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle(); // Reload for Arabic
        when(mockCenterProfileService.getCenterProfile()).thenReturn(Optional.empty()); // Assume no profile for simplicity

        controller.initialize(null, resourceBundle); // Re-initialize

        verify(centerProfileEditorRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }

    // Test for handleBrowseLogo would require mocking FileChooser and Stage,
    // which is more involved for a simple unit test.
}
