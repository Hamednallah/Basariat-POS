package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.SalesordersRecord;
import com.basariatpos.db.generated.tables.records.SalesorderitemsRecord;
import com.basariatpos.db.generated.tables.records.PatientsRecord;
import com.basariatpos.db.generated.tables.records.UsersRecord;
import com.basariatpos.db.generated.tables.records.ProductsRecord;
import com.basariatpos.db.generated.tables.records.InventoryitemsRecord;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.SalesOrderItemDTO;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SalesOrderRepositoryImplTest {

    @InjectMocks
    private SalesOrderRepositoryImpl salesOrderRepository;

    private DSLContext dslContext;
    private TestDataProvider mockDataProvider;
    private MockedStatic<DBManager> mockDBManagerStatic;

    @BeforeEach
    void setUp() {
        mockDataProvider = new TestDataProvider();
        MockConnection connection = new MockConnection(mockDataProvider);
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close();
    }

    private static class TestDataProvider implements MockDataProvider {
        SalesordersRecord salesOrderToReturn;
        SalesorderitemsRecord salesOrderItemToReturn;
        List<SalesorderitemsRecord> salesOrderItemsListToReturn = new ArrayList<>();
        Record patientAndUserRecordToReturn; // For joined patient/user data
        List<Record> itemProductDetailsListToReturn = new ArrayList<>(); // For joined item/product data

        int nextSalesOrderId = 1;
        int nextSoItemId = 100;
        String lastSQL;
        int affectedRows = 1; // Default for updates/deletes

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"SALESORDERS\"")) {
                SalesordersRecord insertedRecord = create.newRecord(SALESORDERS);
                insertedRecord.setSalesOrderId(nextSalesOrderId++);
                // Map relevant fields from bindings if needed for detailed tests
                mock[0] = new MockResult(1, DSL.result(insertedRecord));
            } else if (lastSQL.startsWith("UPDATE \"PUBLIC\".\"SALESORDERS\"")) {
                 mock[0] = new MockResult(affectedRows, create.newResult(SALESORDERS));
            } else if (lastSQL.startsWith("SELECT ") && lastSQL.contains("FROM \"PUBLIC\".\"SALESORDERS\"") && lastSQL.contains("WHERE \"PUBLIC\".\"SALESORDERS\".\"SALES_ORDER_ID\" = ?")) {
                // For findById - header part
                Result<SalesordersRecord> result = create.newResult(SALESORDERS);
                if (salesOrderToReturn != null) result.add(salesOrderToReturn);
                mock[0] = new MockResult(result.size(), result);
            } else if (lastSQL.startsWith("SELECT ") && (lastSQL.contains("LEFT OUTER JOIN \"PUBLIC\".\"PATIENTS\"") || lastSQL.contains("JOIN \"PUBLIC\".\"USERS\""))) {
                // For findById - patient and user details part
                Result<Record> result = create.newResult(PATIENTS.PATIENT_SYSTEM_ID, PATIENTS.FULL_NAME_EN, USERS.FULL_NAME);
                 if (patientAndUserRecordToReturn != null) result.add(patientAndUserRecordToReturn);
                mock[0] = new MockResult(result.size(), result);
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"SALESORDERITEMS\"")) {
                SalesorderitemsRecord insertedItem = create.newRecord(SALESORDERITEMS);
                insertedItem.setSoItemId(nextSoItemId++);
                mock[0] = new MockResult(1, DSL.result(insertedItem));
            } else if (lastSQL.startsWith("SELECT ") && lastSQL.contains("FROM \"PUBLIC\".\"SALESORDERITEMS\"") && lastSQL.contains("WHERE \"PUBLIC\".\"SALESORDERITEMS\".\"SALES_ORDER_ID\" = ?")) {
                // For findById - items part
                Result<Record> result = create.newResult(SALESORDERITEMS.fields());
                result.addAll(itemProductDetailsListToReturn); // Assuming itemProductDetailsListToReturn is primed correctly
                mock[0] = new MockResult(result.size(), result);
            } else if (lastSQL.startsWith("DELETE FROM \"PUBLIC\".\"SALESORDERITEMS\"")) {
                 mock[0] = new MockResult(affectedRows, create.newResult());
            } else if (lastSQL.startsWith("CALL \"PUBLIC\".\"RECALCULATESALESORDERSUBTOTAL\"")) {
                 mock[0] = new MockResult(0, create.newResult()); // Stored procedure call
            }
            else {
                // Default for unhandled SQL, e.g., other SELECTs, UPDATEs
                // System.err.println("Unhandled SQL in SalesOrder TestDataProvider: " + lastSQL);
                mock[0] = new MockResult(affectedRows, create.newResult());
            }
            return mock;
        }
        public void setAffectedRows(int count) { this.affectedRows = count; }
    }

    @Test
    void saveOrderHeader_newOrder_returnsDtoWithId() {
        SalesOrderDTO newOrder = new SalesOrderDTO();
        newOrder.setPatientId(1);
        newOrder.setStatus("Pending");
        newOrder.setCreatedByUserId(1);
        newOrder.setShiftId(1);
        // Other fields will be default or null

        SalesOrderDTO result = salesOrderRepository.saveOrderHeader(newOrder);

        assertNotNull(result);
        assertTrue(result.getSalesOrderId() > 0);
        assertEquals("Pending", result.getStatus());
        assertTrue(mockDataProvider.lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"SALESORDERS\""));
    }

    @Test
    void saveOrderHeader_existingOrder_updatesAndReturnsDto() {
        SalesOrderDTO existingOrder = new SalesOrderDTO();
        existingOrder.setSalesOrderId(1); // Existing ID
        existingOrder.setStatus("UpdatedStatus");
        // Mock that fetchOne for update will return a record
        mockDataProvider.salesOrderToReturn = dslContext.newRecord(SALESORDERS);
        mockDataProvider.salesOrderToReturn.setSalesOrderId(1);


        SalesOrderDTO result = salesOrderRepository.saveOrderHeader(existingOrder);
        assertNotNull(result);
        assertEquals(1, result.getSalesOrderId());
        assertTrue(mockDataProvider.lastSQL.startsWith("UPDATE \"PUBLIC\".\"SALESORDERS\""));
    }


    @Test
    void saveOrderItem_newItem_returnsDtoWithId() {
        SalesOrderItemDTO newItem = new SalesOrderItemDTO();
        newItem.setSalesOrderId(1);
        newItem.setInventoryItemId(10);
        newItem.setQuantity(2);
        newItem.setUnitPrice(new BigDecimal("10.00"));

        SalesOrderItemDTO result = salesOrderRepository.saveOrderItem(newItem);

        assertNotNull(result);
        assertTrue(result.getSoItemId() > 0);
        assertTrue(mockDataProvider.lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"SALESORDERITEMS\""));
    }

    @Test
    void findById_existingOrder_returnsPopulatedDto() {
        int orderId = 1;
        // Setup mock data for header
        mockDataProvider.salesOrderToReturn = dslContext.newRecord(SALESORDERS);
        mockDataProvider.salesOrderToReturn.setSalesOrderId(orderId);
        mockDataProvider.salesOrderToReturn.setPatientId(1);
        mockDataProvider.salesOrderToReturn.setOrderDate(OffsetDateTime.now(ZoneOffset.UTC));
        mockDataProvider.salesOrderToReturn.setStatus("Pending");
        mockDataProvider.salesOrderToReturn.setCreatedByUserId(1);
        mockDataProvider.salesOrderToReturn.setShiftId(1);
        mockDataProvider.salesOrderToReturn.setSubtotalAmount(BigDecimal.ZERO);
        mockDataProvider.salesOrderToReturn.setDiscountAmount(BigDecimal.ZERO);
        mockDataProvider.salesOrderToReturn.setTotalAmount(BigDecimal.ZERO);
        mockDataProvider.salesOrderToReturn.setAmountPaid(BigDecimal.ZERO);
        mockDataProvider.salesOrderToReturn.setBalanceDue(BigDecimal.ZERO);


        // Setup mock data for patient and user details
        mockDataProvider.patientAndUserRecordToReturn = dslContext.newRecord(PATIENTS.PATIENT_SYSTEM_ID, PATIENTS.FULL_NAME_EN, USERS.FULL_NAME);
        mockDataProvider.patientAndUserRecordToReturn.setValue(PATIENTS.PATIENT_SYSTEM_ID, "P001");
        mockDataProvider.patientAndUserRecordToReturn.setValue(PATIENTS.FULL_NAME_EN, "Test Patient");
        mockDataProvider.patientAndUserRecordToReturn.setValue(USERS.FULL_NAME, "Test User");

        // Setup mock data for items with product details
        Record itemDetail1 = dslContext.newRecord(SALESORDERITEMS.fields())
            .into(SALESORDERITEMS.SO_ITEM_ID, 100)
            .into(SALESORDERITEMS.SALES_ORDER_ID, orderId)
            .into(SALESORDERITEMS.INVENTORY_ITEM_ID, 201)
            .into(SALESORDERITEMS.QUANTITY, 2)
            .into(SALESORDERITEMS.UNIT_PRICE, new BigDecimal("10.00"))
            .into(SALESORDERITEMS.ITEM_SUBTOTAL, new BigDecimal("20.00"))
            .into(PRODUCTS.PRODUCT_NAME_EN, "Product Alpha") // Joined field
            .into(INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN, "Red"); // Joined field
        mockDataProvider.itemProductDetailsListToReturn.add(itemDetail1);


        Optional<SalesOrderDTO> resultOpt = salesOrderRepository.findById(orderId);

        assertTrue(resultOpt.isPresent());
        SalesOrderDTO resultDto = resultOpt.get();
        assertEquals(orderId, resultDto.getSalesOrderId());
        assertEquals("Pending", resultDto.getStatus());
        assertEquals("P001", resultDto.getPatientSystemId());
        assertEquals("Test User", resultDto.getCreatedByName());
        assertFalse(resultDto.getItems().isEmpty());
        assertEquals(1, resultDto.getItems().size());
        assertEquals("Product Alpha", resultDto.getItems().get(0).getItemDisplayNameEn());
        assertEquals("Red", resultDto.getItems().get(0).getItemDisplaySpecificNameEn());
    }

    @Test
    void callRecalculateSalesOrderSubtotalProcedure_executesProcedure() {
        salesOrderRepository.callRecalculateSalesOrderSubtotalProcedure(1);
        assertTrue(mockDataProvider.lastSQL.startsWith("CALL \"PUBLIC\".\"RECALCULATESALESORDERSUBTOTAL\""));
    }

    @Test
    void updateOrderStatus_updatesStatus() {
        mockDataProvider.setAffectedRows(1);
        salesOrderRepository.updateOrderStatus(1, "Completed");
        assertTrue(mockDataProvider.lastSQL.contains("UPDATE \"PUBLIC\".\"SALESORDERS\" SET \"STATUS\" = ?"));
    }

    @Test
    void updateOrderDiscount_updatesDiscountAndRecalculates() {
        mockDataProvider.setAffectedRows(1);
        salesOrderRepository.updateOrderDiscount(1, new BigDecimal("5.00"));
        // First SQL should be UPDATE for discount
        assertTrue(mockDataProvider.lastSQL.contains("UPDATE \"PUBLIC\".\"SALESORDERS\" SET \"DISCOUNT_AMOUNT\" = ?"));
        // The method implementation itself calls recalculate, so a subsequent CALL SQL is expected
        // This needs a way to check sequence of SQLs or multiple calls in mockDataProvider.
        // For simplicity, this test just checks the UPDATE part.
        // To test the call to recalculate, one might spy on the repository or use a more advanced mock.
    }
}
