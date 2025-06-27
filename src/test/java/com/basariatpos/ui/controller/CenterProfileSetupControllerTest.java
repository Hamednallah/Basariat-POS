package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.CenterProfileDTO;
import com.basariatpos.service.CenterProfileService;
import com.basariatpos.service.ProfileServiceException;
import com.basariatpos.service.ProfileValidationException;

import javafx.fxml.FXMLLoader; // Not used for direct loading in unit test without Application thread
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane; // For rootPane fx:id
import javafx.scene.layout.VBox; // For formVBox fx:id


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;


import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CenterProfileSetupControllerTest {

    @Mock private CenterProfileService mockCenterProfileService;
    @Mock private TextField centerNameField;
    @Mock private TextField addressLine1Field;
    @Mock private TextField addressLine2Field;
    @Mock private TextField cityField;
    @Mock private TextField countryField;
    @Mock private TextField postalCodeField;
    @Mock private TextField phonePrimaryField;
    @Mock private TextField phoneSecondaryField;
    @Mock private TextField emailAddressField;
    @Mock private TextField websiteField;
    @Mock private TextField logoImagePathField;
    @Mock private TextField taxIdentifierField;
    @Mock private TextField currencySymbolField;
    @Mock private TextField currencyCodeField;
    @Mock private TextArea receiptFooterMessageArea;
    @Mock private Button saveButton; // For getStage() or other direct interactions if any
    @Mock private AnchorPane rootPane; // Mock the root pane
    @Mock private VBox formVBox; // Mock the VBox if directly manipulated beyond rootPane


    @InjectMocks
    private CenterProfileSetupController controller;

    private static ResourceBundle resourceBundle;

    @BeforeAll
    static void setUpClass() {
        // Initialize LocaleManager and MessageProvider for test environment
        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Default to English for tests
        resourceBundle = MessageProvider.getBundle(); // Load bundle once
    }

    @BeforeEach
    void setUp() {
        // Injects mocks into 'controller' instance
        // This is handled by @ExtendWith(MockitoExtension.class) and @InjectMocks
        // For older Mockito: MockitoAnnotations.openMocks(this);

        // Simulate FXML injection by setting mock fields in controller if they are not private
        // If fields are private, this approach won't work directly without reflection
        // or making them package-private for testing.
        // For this example, we assume @InjectMocks handles this or fields are accessible.
        // If @FXML fields are private, @InjectMocks might not set them.
        // A common pattern is to have package-private access for @FXML fields for testing.
        // Let's assume for now that @InjectMocks works or we'd refactor for testability.

        // Manual injection for this test:
        controller.centerNameField = centerNameField;
        controller.addressLine1Field = addressLine1Field;
        controller.addressLine2Field = addressLine2Field;
        controller.cityField = cityField;
        controller.countryField = countryField;
        controller.postalCodeField = postalCodeField;
        controller.phonePrimaryField = phonePrimaryField;
        controller.phoneSecondaryField = phoneSecondaryField;
        controller.emailAddressField = emailAddressField;
        controller.websiteField = websiteField;
        controller.logoImagePathField = logoImagePathField;
        controller.taxIdentifierField = taxIdentifierField;
        controller.currencySymbolField = currencySymbolField;
        controller.currencyCodeField = currencyCodeField;
        controller.receiptFooterMessageArea = receiptFooterMessageArea;
        controller.saveButton = saveButton;
        controller.rootPane = rootPane; // Set the mocked rootPane
        controller.formVBox = formVBox;

        controller.setCenterProfileService(mockCenterProfileService);
    }

    @Test
    void initialize_loadsExistingProfile_whenProfileExists() throws ProfileServiceException {
        CenterProfileDTO existingProfile = new CenterProfileDTO();
        existingProfile.setCenterName("Test Center");
        when(mockCenterProfileService.getCenterProfile()).thenReturn(Optional.of(existingProfile));

        controller.initialize(null, resourceBundle); // URL is not used by controller

        verify(centerNameField).setText("Test Center");
        verify(rootPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT); // Default locale is EN
    }

    @Test
    void initialize_loadsNoProfile_whenProfileDoesNotExist() throws ProfileServiceException {
        when(mockCenterProfileService.getCenterProfile()).thenReturn(Optional.empty());

        controller.initialize(null, resourceBundle);

        verify(centerNameField, never()).setText(anyString()); // Should not be called if no profile
        verify(rootPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }

    @Test
    void initialize_handlesServiceException_onLoad() throws ProfileServiceException {
        when(mockCenterProfileService.getCenterProfile()).thenThrow(new ProfileServiceException("DB error"));

        // The controller logs the error and shows an alert, does not rethrow.
        // So we just ensure initialize completes without throwing an exception itself.
        assertDoesNotThrow(() -> controller.initialize(null, resourceBundle));
        verify(rootPane).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
    }


    @Test
    void handleSaveProfile_collectsDataAndCallsService() throws Exception {
        // Arrange: Mock UI field inputs
        when(centerNameField.getText()).thenReturn("My Optical Center");
        when(addressLine1Field.getText()).thenReturn("123 Main St");
        // ... mock other fields as needed, or assume empty strings if not critical for this test
        when(phonePrimaryField.getText()).thenReturn("555-1234");
        when(currencySymbolField.getText()).thenReturn("$");
        when(currencyCodeField.getText()).thenReturn("USD");
        // ...

        // Mock service to return true on save
        when(mockCenterProfileService.saveProfile(any(CenterProfileDTO.class))).thenReturn(true);

        // Act
        controller.handleSaveProfile(null); // ActionEvent is not used by handler

        // Assert
        ArgumentCaptor<CenterProfileDTO> dtoCaptor = ArgumentCaptor.forClass(CenterProfileDTO.class);
        verify(mockCenterProfileService).saveProfile(dtoCaptor.capture());

        CenterProfileDTO capturedDto = dtoCaptor.getValue();
        assertEquals("My Optical Center", capturedDto.getCenterName());
        assertEquals("123 Main St", capturedDto.getAddressLine1());
        assertEquals("555-1234", capturedDto.getPhonePrimary());
        assertEquals("$", capturedDto.getCurrencySymbol());
        assertEquals("USD", capturedDto.getCurrencyCode());

        // Verify success alert shown (cannot directly test JavaFX alerts without UI test framework)
        // Verify wizard closed (cannot directly test stage closing without UI test framework)
    }

    @Test
    void handleSaveProfile_showsValidationError_whenServiceThrowsValidationException() throws Exception {
        // Arrange: Mock UI field inputs
        when(centerNameField.getText()).thenReturn(""); // Invalid: Name is required by service (assumption)
        // ...
        ProfileValidationException pve = new ProfileValidationException(Collections.singletonList("validation.centerprofile.name.required"));
        when(mockCenterProfileService.saveProfile(any(CenterProfileDTO.class))).thenThrow(pve);

        // Act
        controller.handleSaveProfile(null);

        // Assert
        verify(mockCenterProfileService).saveProfile(any(CenterProfileDTO.class));
        // Verify error alert shown (cannot directly test JavaFX alerts)
        // Controller should catch ProfileValidationException and show an error alert.
    }

    @Test
    void handleSaveProfile_showsServiceError_whenServiceThrowsGenericServiceException() throws Exception {
        // Arrange
        when(mockCenterProfileService.saveProfile(any(CenterProfileDTO.class)))
            .thenThrow(new ProfileServiceException("Database connection failed"));

        // Act
        controller.handleSaveProfile(null);

        // Assert
        verify(mockCenterProfileService).saveProfile(any(CenterProfileDTO.class));
        // Verify error alert shown for service exception
    }

    @Test
    void initialize_setsRTL_forArabicLocale() throws ProfileServiceException {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC); // Switch to Arabic
        resourceBundle = MessageProvider.getBundle(); // Reload bundle for Arabic

        when(mockCenterProfileService.getCenterProfile()).thenReturn(Optional.empty());

        controller.initialize(null, resourceBundle);

        verify(rootPane).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset for other tests
        resourceBundle = MessageProvider.getBundle();
    }

    // Test for handleBrowseLogo would require mocking FileChooser and Stage,
    // which is more involved and better suited for UI testing frameworks like TestFX.
    // For a simple unit test, one might verify that FileChooser is constructed.
}
