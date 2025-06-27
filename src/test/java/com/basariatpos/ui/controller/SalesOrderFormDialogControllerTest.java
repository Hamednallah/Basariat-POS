package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.*;
import com.basariatpos.service.*;
import com.basariatpos.service.exception.SalesOrderServiceException;
import com.basariatpos.ui.utilui.DialogUtil; // Assuming this is where PatientSearchDialog is launched

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalesOrderFormDialogControllerTest {

    @Mock private Label dialogTitleLabel;
    @Mock private TextField patientDisplayField;
    @Mock private DatePicker orderDateField;
    @Mock private ComboBox<String> statusComboBox;
    @Mock private TableView<SalesOrderItemDTO> salesOrderItemsTable;
    @Mock private TextField discountField;
    @Mock private TextField amountPaidField;
    @Mock private Label balanceDueLabel, subtotalAmountLabel, totalAmountLabel;
    @Mock private Button saveOrderButton, recordPaymentButton, addItemButton, removeItemButton, configureLensButton, abandonOrderButton, notifyOrderReadyButton;
    @Mock private BorderPane salesOrderFormRootPane;
    @Mock private Stage mockDialogStage;

    @Mock private SalesOrderService mockSalesOrderService;
    @Mock private PatientService mockPatientService;
    @Mock private InventoryItemService mockInventoryItemService;
    @Mock private ProductService mockProductService;
    @Mock private UserSessionService mockUserSessionService;
    @Mock private WhatsAppNotificationService mockWhatsAppNotificationService;
    @Mock private CenterProfileService mockCenterProfileService;

    @Spy
    private ObservableList<SalesOrderItemDTO> currentOrderItems = FXCollections.observableArrayList();

    @InjectMocks
    private SalesOrderFormDialogController controller;

    private static ResourceBundle resourceBundle;
    private MockedStatic<DialogUtil> mockDialogUtil;
    private MockedStatic<AppLauncher> mockAppLauncher;


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
        controller.dialogTitleLabel = dialogTitleLabel;
        controller.patientDisplayField = patientDisplayField;
        controller.orderDateField = orderDateField;
        controller.statusComboBox = statusComboBox;
        controller.salesOrderItemsTable = salesOrderItemsTable;
        controller.discountField = discountField;
        controller.amountPaidField = amountPaidField;
        controller.balanceDueLabel = balanceDueLabel;
        controller.subtotalAmountLabel = subtotalAmountLabel;
        controller.totalAmountLabel = totalAmountLabel;
        controller.saveOrderButton = saveOrderButton;
        controller.recordPaymentButton = recordPaymentButton;
        controller.addItemButton = addItemButton;
        controller.removeItemButton = removeItemButton;
        controller.configureLensButton = configureLensButton;
        controller.abandonOrderButton = abandonOrderButton;
        controller.notifyOrderReadyButton = notifyOrderReadyButton;
        controller.salesOrderFormRootPane = salesOrderFormRootPane;

        controller.currentOrderItems = currentOrderItems; // Inject spy

        // Mock services passed via setServices
        controller.setServices(mockSalesOrderService, mockPatientService, mockInventoryItemService,
                               mockProductService, mockUserSessionService, mockWhatsAppNotificationService, mockCenterProfileService);

        // Mock static DialogUtil for Patient Search
        mockDialogUtil = Mockito.mockStatic(DialogUtil.class);

        // Mock AppLauncher if services are fetched again internally (e.g. for RecordPaymentDialog)
        mockAppLauncher = Mockito.mockStatic(AppLauncher.class);
        mockAppLauncher.when(AppLauncher::getSalesOrderService).thenReturn(mockSalesOrderService);
        mockAppLauncher.when(AppLauncher::getPatientService).thenReturn(mockPatientService);
        mockAppLauncher.when(AppLauncher::getInventoryItemService).thenReturn(mockInventoryItemService);
        mockAppLauncher.when(AppLauncher::getProductService).thenReturn(mockProductService);
        mockAppLauncher.when(AppLauncher::getUserSessionService).thenReturn(mockUserSessionService);
        mockAppLauncher.when(AppLauncher::getWhatsAppNotificationService).thenReturn(mockWhatsAppNotificationService);
        mockAppLauncher.when(AppLauncher::getCenterProfileService).thenReturn(mockCenterProfileService);


        // Mock UserSessionService behavior
        when(mockUserSessionService.hasPermission(anyString())).thenReturn(true); // Assume all permissions for basic tests
        ShiftDTO activeShift = new ShiftDTO(); activeShift.setStatus("Active");
        when(mockUserSessionService.getActiveShift()).thenReturn(activeShift);


        // Call initialize after mocks are ready
        controller.initialize(null, resourceBundle); // For @FXML related init
        controller.setDialogStage(mockDialogStage);  // For dialog operations
    }

    @AfterEach
    void tearDown() {
        mockDialogUtil.close();
        mockAppLauncher.close();
    }

    @Test
    void initializeDialogData_addMode_setsUpCorrectly() {
        controller.initializeDialogData(null); // Add mode

        verify(dialogTitleLabel).setText(MessageProvider.getString("salesorder.form.add.title"));
        verify(orderDateField).setValue(LocalDate.now());
        // Default status is set in DTO, then to ComboBox
        // verify(statusComboBox).setValue(new SalesOrderDTO().getStatus());
        verify(discountField).setText("0.00");
        verify(amountPaidField).setText("0.00");
        verify(salesOrderFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
        // Verify active shift enables save button
        verify(saveOrderButton).setDisable(false);
    }

    @Test
    void initializeDialogData_editMode_populatesFields() throws PatientServiceException {
        SalesOrderDTO order = new SalesOrderDTO();
        order.setSalesOrderId(1L);
        order.setPatientId(10L);
        order.setPatientFullName("Test Patient"); // Summary DTO might have this
        order.setOrderDate(OffsetDateTime.now().minusDays(1));
        order.setStatus("Pending");
        order.setDiscountAmount(new BigDecimal("5.00"));
        order.setAmountPaid(new BigDecimal("50.00"));
        order.setItems(new ArrayList<>()); // Initialize items list

        PatientDTO mockPatientFull = new PatientDTO();
        mockPatientFull.setPatientId(10L);
        mockPatientFull.setFullNameEn("Test Patient Full Name");
        mockPatientFull.setSystemPatientId("PAT-FULL-001");
        mockPatientFull.setPhoneNumber("1231231234");
        mockPatientFull.setWhatsappOptIn(true);

        when(mockPatientService.getPatientById(10L)).thenReturn(Optional.of(mockPatientFull));


        controller.initializeDialogData(order);

        verify(dialogTitleLabel).setText(MessageProvider.getString("salesorder.form.edit.title", "1"));
        verify(patientDisplayField).setText(mockPatientFull.getDisplayFullNameWithId());
        verify(orderDateField).setValue(order.getOrderDate().toLocalDate());
        verify(statusComboBox).setValue("Pending");
        verify(discountField).setText("5.00");
        verify(amountPaidField).setText("50.00");
    }

    @Test
    void handleSaveOrderButtonAction_addMode_validInput_createsOrder() throws Exception {
        controller.initializeDialogData(null); // Add mode

        PatientDTO patient = new PatientDTO(); patient.setPatientId(1L);
        controller.selectedPatient = patient; // Simulate patient selection
        when(orderDateField.getValue()).thenReturn(LocalDate.now());
        when(statusComboBox.getValue()).thenReturn("Pending");
        when(discountField.getText()).thenReturn("0");
        when(amountPaidField.getText()).thenReturn("0");

        // Add a valid item
        SalesOrderItemDTO item = new SalesOrderItemDTO();
        item.setItemTypeDisplay(controller.ITEM_TYPE_STOCK); // Use constant from controller
        item.setInventoryItemId(1); // Valid item ID
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setItemSubtotal(new BigDecimal("10.00")); // Ensure subtotal is set
        currentOrderItems.add(item);

        SalesOrderDTO savedOrder = new SalesOrderDTO(); savedOrder.setSalesOrderId(123L);
        when(mockSalesOrderService.createSalesOrder(any(SalesOrderDTO.class))).thenReturn(savedOrder);
        // For AlertUtil success message
        when(saveOrderButton.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(saveOrderButton.getScene().getWindow()).thenReturn(mockDialogStage);


        controller.handleSaveOrderButtonAction(null);

        assertTrue(controller.isSaved());
        // verify(mockSalesOrderService).createSalesOrder(any(SalesOrderDTO.class)); // Captured below
        // verify(mockDialogStage).close(); // Closes if not "Ready for Pickup" - this behavior might vary
        ArgumentCaptor<SalesOrderDTO> captor = ArgumentCaptor.forClass(SalesOrderDTO.class);
        verify(mockSalesOrderService).createSalesOrder(captor.capture());
        assertEquals(patient.getPatientId(), captor.getValue().getPatientId());
        assertEquals(1, captor.getValue().getItems().size());
    }

    @Test
    void checkActiveShiftAndPermissions_noActiveShift_disablesControls() {
        when(mockUserSessionService.getActiveShift()).thenReturn(null); // No active shift
        controller.initializeDialogData(null); // This will call checkActiveShiftAndPermissions

        verify(saveOrderButton).setDisable(true);
        verify(recordPaymentButton, atLeastOnce()).setDisable(true); // Also true because no active shift
        verify(addItemButton).setDisable(true);
        verify(discountField).setDisable(true);
    }


    @Test
    void initializeDialogData_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initializeDialogData(null);

        verify(salesOrderFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
