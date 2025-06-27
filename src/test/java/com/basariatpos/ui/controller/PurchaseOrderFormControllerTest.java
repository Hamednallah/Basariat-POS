package com.basariatpos.ui.controller;

import com.basariatpos.i18n.LocaleManager;
import com.basariatpos.i18n.MessageProvider;
import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;
import com.basariatpos.model.UserDTO;
import com.basariatpos.service.InventoryItemService;
import com.basariatpos.service.PurchaseOrderService;
import com.basariatpos.service.UserSessionService;
import com.basariatpos.service.exception.PurchaseOrderException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseOrderFormControllerTest {

    @Mock private Label dialogTitleLabel, poIdValueLabel, grandTotalLabel;
    @Mock private TextField supplierNameField;
    @Mock private DatePicker orderDateField;
    @Mock private ComboBox<String> statusComboBox;
    @Mock private TableView<PurchaseOrderItemDTO> poItemsTable;
    @Mock private TableColumn<PurchaseOrderItemDTO, InventoryItemDTO> inventoryItemColumn;
    @Mock private TableColumn<PurchaseOrderItemDTO, Integer> qtyOrderedColumn;
    @Mock private TableColumn<PurchaseOrderItemDTO, BigDecimal> pricePerUnitColumn;
    @Mock private TableColumn<PurchaseOrderItemDTO, BigDecimal> subtotalColumn;
    @Mock private Button addItemButton, removeItemButton, savePOButton;
    @Mock private BorderPane poFormRootPane;
    @Mock private Stage mockDialogStage;

    @Mock private PurchaseOrderService mockPoService;
    @Mock private InventoryItemService mockInventoryItemService;
    @Mock private UserSessionService mockUserSessionService;

    @Spy
    private ObservableList<PurchaseOrderItemDTO> poItemsList = FXCollections.observableArrayList();
    @Spy
    private ObservableList<InventoryItemDTO> availableInventoryItems = FXCollections.observableArrayList();

    @InjectMocks
    private PurchaseOrderFormController controller;

    private static ResourceBundle resourceBundle;
    private InventoryItemDTO sampleInvItem1, sampleInvItem2;
    private UserDTO mockCurrentUser;


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
        controller.supplierNameField = supplierNameField;
        controller.orderDateField = orderDateField;
        controller.statusComboBox = statusComboBox;
        controller.poIdValueLabel = poIdValueLabel;
        controller.poItemsTable = poItemsTable;
        controller.inventoryItemColumn = inventoryItemColumn;
        controller.qtyOrderedColumn = qtyOrderedColumn;
        controller.pricePerUnitColumn = pricePerUnitColumn;
        controller.subtotalColumn = subtotalColumn;
        controller.addItemButton = addItemButton;
        controller.removeItemButton = removeItemButton;
        controller.savePOButton = savePOButton;
        controller.grandTotalLabel = grandTotalLabel;
        controller.poFormRootPane = poFormRootPane;

        // Spy injection for lists
        controller.poItemsList = poItemsList;
        controller.availableInventoryItems = availableInventoryItems;

        sampleInvItem1 = new InventoryItemDTO(); sampleInvItem1.setInventoryItemId(1); sampleInvItem1.setItemSpecificNameEn("Item 1"); sampleInvItem1.setCostPrice(new BigDecimal("10.00"));
        sampleInvItem2 = new InventoryItemDTO(); sampleInvItem2.setInventoryItemId(2); sampleInvItem2.setItemSpecificNameEn("Item 2"); sampleInvItem2.setCostPrice(new BigDecimal("20.00"));
        availableInventoryItems.addAll(sampleInvItem1, sampleInvItem2);

        try {
            when(mockInventoryItemService.getAllInventoryItems(false)).thenReturn(new ArrayList<>(availableInventoryItems));
        } catch (InventoryItemServiceException e) { fail("Mock setup failed: " + e.getMessage()); }

        // Mock selection model for removeItemButton binding
        TableView.TableViewSelectionModel<PurchaseOrderItemDTO> mockSelectionModel = mock(TableView.TableViewSelectionModel.class);
        when(poItemsTable.getSelectionModel()).thenReturn(mockSelectionModel);
        when(mockSelectionModel.selectedItemProperty()).thenReturn(mock(javafx.beans.property.ReadOnlyObjectProperty.class));

        mockCurrentUser = new UserDTO(1L, "testadmin", "Test Admin", "Admin", "pass", true, Collections.emptyList());
        when(mockUserSessionService.getCurrentUser()).thenReturn(mockCurrentUser);


        // initializeDialog is the main entry point
    }

    @Test
    void initializeDialog_addMode_setsUpCorrectly() {
        controller.initializeDialog(mockPoService, mockInventoryItemService, mockUserSessionService, mockDialogStage, null);

        verify(dialogTitleLabel).setText(MessageProvider.getString("po.dialog.add.title"));
        verify(orderDateField).setValue(LocalDate.now());
        verify(statusComboBox).setValue("Pending");
        verify(poFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.LEFT_TO_RIGHT);
        verify(poItemsTable).setItems(poItemsList);
    }

    @Test
    void initializeDialog_editMode_populatesFields() {
        PurchaseOrderDTO po = new PurchaseOrderDTO();
        po.setPurchaseOrderId(10L);
        po.setSupplierName("Test Supplier");
        po.setOrderDate(LocalDate.of(2023,1,1));
        po.setStatus("Partial");
        PurchaseOrderItemDTO item1 = new PurchaseOrderItemDTO(); item1.setInventoryItemId(1); item1.setQuantityOrdered(5); item1.setPurchasePricePerUnit(new BigDecimal("10"));
        po.setItems(new ArrayList<>(List.of(item1)));

        controller.initializeDialog(mockPoService, mockInventoryItemService, mockUserSessionService, mockDialogStage, po);

        verify(dialogTitleLabel).setText(MessageProvider.getString("po.dialog.edit.title"));
        verify(supplierNameField).setText("Test Supplier");
        verify(orderDateField).setValue(LocalDate.of(2023,1,1));
        verify(statusComboBox).setValue("Partial");
        verify(poIdValueLabel).setText("10");
        assertEquals(1, poItemsList.size());
    }

    @Test
    void handleAddItemButtonAction_addsNewItemToTable() {
        controller.initializeDialog(mockPoService, mockInventoryItemService, mockUserSessionService, mockDialogStage, null);
        controller.handleAddItemButtonAction(null);
        assertEquals(1, poItemsList.size());
        assertEquals(1, poItemsList.get(0).getQuantityOrdered()); // Default quantity
    }

    @Test
    void handleSavePurchaseOrderButtonAction_addMode_validInput_savesPO() throws Exception {
        controller.initializeDialog(mockPoService, mockInventoryItemService, mockUserSessionService, mockDialogStage, null);

        when(supplierNameField.getText()).thenReturn("Supplier X");
        when(orderDateField.getValue()).thenReturn(LocalDate.now());
        when(statusComboBox.getValue()).thenReturn("Pending");

        PurchaseOrderItemDTO newItem = new PurchaseOrderItemDTO();
        newItem.setInventoryItemId(sampleInvItem1.getInventoryItemId());
        newItem.setQuantityOrdered(2);
        newItem.setPurchasePricePerUnit(new BigDecimal("10.50"));
        controller.poItemsList.add(newItem); // Manually add item for test
        controller.updateGrandTotal(); // Manually update total

        PurchaseOrderDTO savedPO = new PurchaseOrderDTO(); // Mock returned PO
        when(mockPoService.createNewPurchaseOrder(any(PurchaseOrderDTO.class))).thenReturn(savedPO);

        controller.handleSavePurchaseOrderButtonAction(null);

        assertTrue(controller.isSaved());
        assertEquals(savedPO, controller.getSavedPurchaseOrder());
        verify(mockDialogStage).close();
        ArgumentCaptor<PurchaseOrderDTO> captor = ArgumentCaptor.forClass(PurchaseOrderDTO.class);
        verify(mockPoService).createNewPurchaseOrder(captor.capture());
        assertEquals("Supplier X", captor.getValue().getSupplierName());
        assertEquals(1, captor.getValue().getItems().size());
    }

    @Test
    void initializeDialog_setsRTL_forArabicLocale() {
        LocaleManager.setCurrentLocale(LocaleManager.ARABIC);
        resourceBundle = MessageProvider.getBundle();

        controller.initializeDialog(mockPoService, mockInventoryItemService, mockUserSessionService, mockDialogStage, null);

        verify(poFormRootPane, atLeastOnce()).setNodeOrientation(javafx.scene.NodeOrientation.RIGHT_TO_LEFT);

        LocaleManager.setCurrentLocale(Locale.ENGLISH); // Reset
    }
}
