package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.PurchaseorderitemsRecord;
import com.basariatpos.db.generated.tables.records.PurchaseordersRecord;
import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;

import org.jooq.*;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.*; // Import all tables
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderRepositoryImplTest {

    @InjectMocks
    private PurchaseOrderRepositoryImpl poRepository;

    private DSLContext dslContext;
    private TestDataProvider mockDataProvider;
    private MockedStatic<DBManager> mockDBManagerStatic;

    private PurchaseOrderDTO testPoDto;
    private Record testPoHeaderRecord; // For findById, findAllSummaries (includes Users.FULL_NAME)
    private List<Record> testPoItemRecords; // For findById (includes joined names)


    @BeforeEach
    void setUp() {
        mockDataProvider = new TestDataProvider();
        Connection connection = new MockConnection(mockDataProvider);
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // Setup for testPoDto
        testPoDto = new PurchaseOrderDTO(1, LocalDate.now(), "Test Supplier",
                                         new BigDecimal("120.00"), "Pending", 1, "Admin User", now,
                                         new ArrayList<>());
        PurchaseOrderItemDTO item1 = new PurchaseOrderItemDTO();
        item1.setPoItemId(10); item1.setPurchaseOrderId(1); item1.setInventoryItemId(101);
        item1.setInventoryItemProductCode("PCODE1"); item1.setInventoryItemProductNameEn("Product 1 EN");
        item1.setInventoryItemSpecificNameEn("Specific 1 EN"); item1.setInventoryItemUnitOfMeasure("pcs");
        item1.setQuantityOrdered(10); item1.setPurchasePricePerUnit(new BigDecimal("12.00"));
        testPoDto.getItems().add(item1);

        // Setup for testPoHeaderRecord (simulates result of JOIN with USERS)
        DSLContext create = DSL.using(SQLDialect.POSTGRES);
        testPoHeaderRecord = create.newRecord(PURCHASEORDERS.asterisk(), USERS.FULL_NAME);
        testPoHeaderRecord.setValue(PURCHASEORDERS.PURCHASE_ORDER_ID, 1);
        testPoHeaderRecord.setValue(PURCHASEORDERS.ORDER_DATE, LocalDate.now());
        testPoHeaderRecord.setValue(PURCHASEORDERS.SUPPLIER_NAME, "Test Supplier");
        testPoHeaderRecord.setValue(PURCHASEORDERS.TOTAL_AMOUNT, new BigDecimal("120.00"));
        testPoHeaderRecord.setValue(PURCHASEORDERS.STATUS, "Pending");
        testPoHeaderRecord.setValue(PURCHASEORDERS.CREATED_BY_USER_ID, 1);
        testPoHeaderRecord.setValue(PURCHASEORDERS.CREATED_AT, now);
        testPoHeaderRecord.setValue(USERS.FULL_NAME, "Admin User");

        // Setup for testPoItemRecords (simulates result of JOIN with INVENTORYITEMS and PRODUCTS)
        testPoItemRecords = new ArrayList<>();
        Record itemRecord1 = create.newRecord(PURCHASEORDERITEMS.asterisk(), PRODUCTS.PRODUCT_CODE, PRODUCTS.PRODUCT_NAME_EN, INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN, INVENTORYITEMS.UNIT_OF_MEASURE);
        itemRecord1.setValue(PURCHASEORDERITEMS.PO_ITEM_ID, 10);
        itemRecord1.setValue(PURCHASEORDERITEMS.PURCHASE_ORDER_ID, 1);
        itemRecord1.setValue(PURCHASEORDERITEMS.INVENTORY_ITEM_ID, 101);
        itemRecord1.setValue(PURCHASEORDERITEMS.QUANTITY_ORDERED, 10);
        itemRecord1.setValue(PURCHASEORDERITEMS.QUANTITY_RECEIVED, 0);
        itemRecord1.setValue(PURCHASEORDERITEMS.PURCHASE_PRICE_PER_UNIT, new BigDecimal("12.00"));
        itemRecord1.setValue(PURCHASEORDERITEMS.SUBTOTAL, new BigDecimal("120.00"));
        itemRecord1.setValue(PRODUCTS.PRODUCT_CODE, "PCODE1");
        itemRecord1.setValue(PRODUCTS.PRODUCT_NAME_EN, "Product 1 EN");
        itemRecord1.setValue(INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN, "Specific 1 EN");
        itemRecord1.setValue(INVENTORYITEMS.UNIT_OF_MEASURE, "pcs");
        testPoItemRecords.add(itemRecord1);
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close();
    }

    private static class TestDataProvider implements MockDataProvider {
        Record poHeaderToReturn;
        List<Record> poItemsToReturn = new ArrayList<>();
        List<Record> poSummariesToReturn = new ArrayList<>();
        PurchaseorderitemsRecord poItemToReturnForSaveUpdate; // For single item save/update
        String lastSQL;
        int nextPoId = 200;
        int nextPoItemId = 300;

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("SELECT")) {
                if (lastSQL.contains("FROM \"PUBLIC\".\"PURCHASEORDERS\"") && lastSQL.contains("JOIN \"PUBLIC\".\"USERS\"")) { // findById (header) or findAllSummaries
                    if (lastSQL.contains("WHERE \"PUBLIC\".\"PURCHASEORDERS\".\"PURCHASE_ORDER_ID\" = ?")) { // findById header part
                        Result<Record> result = create.newResult(PURCHASEORDERS.fields()); // Simplified, should match projection
                        if (poHeaderToReturn != null && poHeaderToReturn.get(PURCHASEORDERS.PURCHASE_ORDER_ID).equals(ctx.bindings()[0])) {
                            result.add(poHeaderToReturn);
                        }
                        mock[0] = new MockResult(result.size(), result);
                    } else { // findAllSummaries
                        Result<Record> result = create.newResult(PURCHASEORDERS.fields()); // Simplified
                        result.addAll(poSummariesToReturn);
                        mock[0] = new MockResult(result.size(), result);
                    }
                } else if (lastSQL.contains("FROM \"PUBLIC\".\"PURCHASEORDERITEMS\"")) { // findById items part
                     Result<Record> result = create.newResult(PURCHASEORDERITEMS.fields()); // Simplified
                     result.addAll(poItemsToReturn);
                     mock[0] = new MockResult(result.size(), result);
                } else {
                     mock[0] = new MockResult(0, create.newResult());
                }
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"PURCHASEORDERS\"")) {
                PurchaseordersRecord insertedPo = create.newRecord(PURCHASEORDERS);
                insertedPo.setPurchaseOrderId(nextPoId++);
                // map bindings...
                Result<PurchaseordersRecord> result = create.newResult(PURCHASEORDERS);
                result.add(insertedPo);
                mock[0] = new MockResult(1, result);
                this.poHeaderToReturn = insertedPo; // So it can be "retrieved" if findById is called in same transaction mock
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"PURCHASEORDERITEMS\"")) {
                 PurchaseorderitemsRecord insertedItem = create.newRecord(PURCHASEORDERITEMS);
                 insertedItem.setPoItemId(nextPoItemId++);
                 // map bindings...
                 Result<PurchaseorderitemsRecord> result = create.newResult(PURCHASEORDERITEMS);
                 result.add(insertedItem);
                 mock[0] = new MockResult(1, result);
                 this.poItemToReturnForSaveUpdate = insertedItem;
            } else if (lastSQL.startsWith("UPDATE \"PUBLIC\".\"PURCHASEORDERS\"") || lastSQL.startsWith("UPDATE \"PUBLIC\".\"PURCHASEORDERITEMS\"")) {
                mock[0] = new MockResult(1, create.newResult()); // Assume 1 row updated
            } else if (lastSQL.startsWith("DELETE FROM \"PUBLIC\".\"PURCHASEORDERITEMS\"")) {
                mock[0] = new MockResult(1, create.newResult());
            }
            else {
                System.err.println("Unhandled SQL in MockDataProvider (PO): " + lastSQL);
                mock[0] = new MockResult(0, create.newResult());
            }
            return mock;
        }
        public void primePoHeader(Record r) { this.poHeaderToReturn = r; }
        public void primePoItems(List<Record> items) { this.poItemsToReturn = items; }
        public void primePoSummaries(List<Record> summaries) { this.poSummariesToReturn = summaries; }
        public void primePoItemForSave(PurchaseorderitemsRecord item) {this.poItemToReturnForSaveUpdate = item;}
    }

    @Test
    void findById_poExists_returnsDtoWithItemsAndNames() {
        mockDataProvider.primePoHeader(testPoHeaderRecord);
        mockDataProvider.primePoItems(testPoItemRecords);

        Optional<PurchaseOrderDTO> result = poRepository.findById(1);

        assertTrue(result.isPresent());
        PurchaseOrderDTO po = result.get();
        assertEquals(1, po.getPurchaseOrderId());
        assertEquals("Test Supplier", po.getSupplierName());
        assertEquals("Admin User", po.getCreatedByName());
        assertEquals(1, po.getItems().size());
        PurchaseOrderItemDTO item = po.getItems().get(0);
        assertEquals(10, item.getPoItemId());
        assertEquals("Product 1 EN", item.getInventoryItemProductNameEn());
        assertEquals("Specific 1 EN", item.getInventoryItemSpecificNameEn());
    }

    @Test
    void saveNewOrderWithItems_validDto_savesHeaderAndItems() {
        // This test is complex because of the transaction and multiple inserts.
        // The MockDataProvider needs to handle the sequence of SQL statements.
        // For simplicity, we'll verify the last SQL was for items, assuming header was first.
        // A true test of transactional behavior often requires an in-memory DB or more complex mocking.

        PurchaseOrderDTO newPo = new PurchaseOrderDTO(LocalDate.now(), "New Supplier", 1, "TestUser");
        PurchaseOrderItemDTO newItem = new PurchaseOrderItemDTO();
        newItem.setInventoryItemId(102); newItem.setQuantityOrdered(5); newItem.setPurchasePricePerUnit(new BigDecimal("10.00"));
        newPo.getItems().add(newItem);

        // Simulate return values for generated IDs (not directly supported by this simple MockDataProvider for multi-statement transactions)
        // The repository method itself constructs and returns the DTO with updated IDs from record.store() results.

        PurchaseOrderDTO savedPo = poRepository.saveNewOrderWithItems(newPo);

        assertNotNull(savedPo);
        assertTrue(savedPo.getPurchaseOrderId() >= 200); // From nextPoId
        assertEquals(1, savedPo.getItems().size());
        assertTrue(savedPo.getItems().get(0).getPoItemId() >= 300); // From nextPoItemId

        // Check if the last SQL was an INSERT into items (very basic check)
        // A more robust mock would track all SQLs in the transaction.
        assertTrue(mockDataProvider.lastSQL.contains("INSERT INTO \"PUBLIC\".\"PURCHASEORDERITEMS\""));
    }

    @Test
    void updateOrderItemReceivedQuantityAndPrice_updatesItem() {
        int poItemIdToUpdate = 10;
        int newQtyReceived = 5;
        BigDecimal newPrice = new BigDecimal("11.50");

        poRepository.updateOrderItemReceivedQuantityAndPrice(poItemIdToUpdate, newQtyReceived, newPrice);

        assertTrue(mockDataProvider.lastSQL.startsWith("UPDATE \"PUBLIC\".\"PURCHASEORDERITEMS\""));
        // A more thorough mock would allow verifying the SET clauses with bound values.
        // For now, checking the SQL type is a basic confirmation.
    }

    // Add tests for findAllSummaries, updateOrderHeader, saveOrderItem, deleteOrderItem, updateOrderStatus
}
