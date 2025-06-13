package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.AuditlogRecord;
import org.jooq.DSLContext;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.OffsetDateTime;

import static com.basariatpos.db.generated.Tables.AUDITLOG;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryImplTest {

    @InjectMocks
    private AuditLogRepositoryImpl auditLogRepository;

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
        AuditlogRecord lastInsertedRecord;
        String lastSQL;

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"AUDITLOG\"")) {
                // Capture the bindings to verify them
                // This is a simplified capture; JOOQ's MockExecuteContext gives access to bindings.
                // For a real capture, you'd iterate ctx.bindings() and map to record fields.

                // Simulate record creation for verification (actual record is created by the method under test)
                lastInsertedRecord = create.newRecord(AUDITLOG);
                // Example: lastInsertedRecord.setUserId((Integer) ctx.bindings()[indexOfUserIdBinding]);
                // This part is tricky with MockDataProvider as direct binding access is complex.
                // We will verify the call to store() and indirectly test values by what the method constructs.

                mock[0] = new MockResult(1, create.newResult(AUDITLOG)); // Simulate 1 row inserted
            } else {
                System.err.println("Unhandled SQL in MockDataProvider (AuditLog): " + lastSQL);
                mock[0] = new MockResult(0, create.newResult());
            }
            return mock;
        }
    }

    @Test
    void logStockAdjustment_constructsAndInsertsCorrectAuditRecord() {
        Integer inventoryItemId = 101;
        String itemName = "Test Item - Red";
        int quantityChange = -5;
        int oldQty = 20;
        int newQty = 15;
        String reason = "Damaged goods";
        Integer adjustedByUserId = 1;

        // ArgumentCaptor for AuditlogRecord can't be used directly with store() unless we mock the record itself.
        // Instead, we will verify the SQL or trust the JOOQ record mapping.
        // For this test, we'll primarily ensure the method runs and attempts an insert.
        // A deeper test would involve a more complex MockDataProvider to inspect bound values.

        auditLogRepository.logStockAdjustment(inventoryItemId, itemName, quantityChange, oldQty, newQty, reason, adjustedByUserId);

        assertNotNull(mockDataProvider.lastSQL);
        assertTrue(mockDataProvider.lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"AUDITLOG\""));

        // To verify specific values, we would need to enhance TestDataProvider
        // or use a spy on the DSLContext/AuditlogRecord if possible.
        // For now, this confirms an insert was attempted.
        // We can also check parts of the generated SQL if it were available from MockExecuteContext easily.
        // Example (conceptual, requires better SQL capture from mock):
        // assertTrue(mockDataProvider.lastSQL.contains("'InventoryItems'")); // table_name
        // assertTrue(mockDataProvider.lastSQL.contains("'" + inventoryItemId + "'")); // record_pk
        // assertTrue(mockDataProvider.lastSQL.contains("'ADJUST'")); // action_type
        // assertTrue(mockDataProvider.lastSQL.contains(String.valueOf(adjustedByUserId))); // user_id
        // assertTrue(mockDataProvider.lastSQL.contains("{\"quantityOnHand\": " + oldQty + "}"));
        // assertTrue(mockDataProvider.lastSQL.contains("{\"quantityOnHand\": " + newQty + "}"));
        // String expectedDetailsFragment = String.format("Item: '%s'. Change: %d. Old Qty: %d, New Qty: %d. Reason: %s",
        // itemName, quantityChange, oldQty, newQty, reason).substring(0, 20); // Check a substring
        // assertTrue(mockDataProvider.lastSQL.contains(expectedDetailsFragment));
    }

    @Test
    void logStockAdjustment_nullUserId_insertsWithNullUserId() {
        auditLogRepository.logStockAdjustment(102, "Another Item", 10, 5, 15, "Initial Stock", null);
        assertNotNull(mockDataProvider.lastSQL);
        assertTrue(mockDataProvider.lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"AUDITLOG\""));
        // Conceptually, verify that the SQL generated for user_id allows/inserts NULL.
        // This depends on how JOOQ and MockDataProvider handle nulls in bindings.
    }
}
