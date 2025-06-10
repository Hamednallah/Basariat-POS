package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.Centerprofile;
import com.basariatpos.db.generated.tables.records.CenterprofileRecord;
import com.basariatpos.model.CenterProfileDTO;

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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.CENTERPROFILE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CenterProfileRepositoryImplTest {

    @InjectMocks
    private CenterProfileRepositoryImpl centerProfileRepository;

    private DSLContext dslContext;
    private MockDataProvider mockDataProvider;
    private MockedStatic<DBManager> mockDBManagerStatic;

    private CenterProfileDTO testDto;

    @BeforeEach
    void setUp() {
        // Setup DTO for tests
        testDto = new CenterProfileDTO(
                "Test Center", "123 Main St", null, "Testville", "Testland",
                "12345", "555-1234", null, "test@example.com",
                "www.example.com", "/path/to/logo.png", "TAX123", "$",
                "USD", "Thanks for shopping!"
        );

        // Setup mock DSLContext using jOOQ's MockDataProvider
        // This is a more jOOQ-idiomatic way to test the repository layer
        // without involving Mockito for all jOOQ fluent API calls.
        mockDataProvider = new TestDataProvider();
        Connection connection = new MockConnection(mockDataProvider);
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        // Mock DBManager.getDSLContext() to return this mock dslContext
        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close(); // Release static mock
    }

    // Custom MockDataProvider for jOOQ
    private static class TestDataProvider implements MockDataProvider {
        CenterprofileRecord dbRecord; // Simulate database state
        boolean recordExists = false;
        int singleProfileId = 1;

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];
            String sql = ctx.sql().toUpperCase();

            if (sql.startsWith("SELECT")) {
                if (sql.contains("COUNT(*)") || sql.contains("EXISTS")) { // For exists()
                    Result<Record1<Integer>> result = create.newResult(DSL.field("count", SQLDataType.INTEGER));
                    if (recordExists) {
                        result.add(create.newRecord(DSL.field("count", SQLDataType.INTEGER)).values(1));
                    } else {
                        result.add(create.newRecord(DSL.field("count", SQLDataType.INTEGER)).values(0));
                    }
                     mock[0] = new MockResult(recordExists ? 1 : 0, result);
                } else { // For getProfile() or fetchOne() in save()
                    if (recordExists && dbRecord != null) {
                        Result<CenterprofileRecord> result = create.newResult(CENTERPROFILE);
                        result.add(dbRecord);
                        mock[0] = new MockResult(1, result);
                    } else {
                        mock[0] = new MockResult(0, create.newResult(CENTERPROFILE)); // No record found
                    }
                }
            } else if (sql.startsWith("INSERT")) {
                // Simulate insert: create the record, set exists to true
                dbRecord = create.newRecord(CENTERPROFILE);
                // Simulate setting fields from bind values (simplified here)
                // In a real scenario, you'd parse ctx.bindings()
                dbRecord.setProfileId(singleProfileId);
                dbRecord.setCenterName((String)ctx.bindings()[0]); // Example, order depends on INSERT statement
                recordExists = true;
                mock[0] = new MockResult(1, create.newResult(CENTERPROFILE)); // 1 row inserted
            } else if (sql.startsWith("UPDATE")) {
                if (recordExists) {
                    // Simulate update: modify dbRecord (simplified)
                     dbRecord.setCenterName((String)ctx.bindings()[0]);
                    mock[0] = new MockResult(1, create.newResult(CENTERPROFILE)); // 1 row updated
                } else {
                     mock[0] = new MockResult(0, create.newResult(CENTERPROFILE)); // No row updated
                }
            }
            return mock;
        }

        public void primeRecord(CenterprofileRecord record) {
            this.dbRecord = record;
            this.recordExists = (record != null);
        }
        public void setRecordExists(boolean exists) {
            this.recordExists = exists;
            if(!exists) this.dbRecord = null;
        }
    }


    @Test
    void save_should_insert_new_profile_if_not_exists() {
        // Arrange
        ((TestDataProvider) mockDataProvider).setRecordExists(false);

        // Act
        centerProfileRepository.save(testDto);

        // Assert: Verification is tricky with MockDataProvider.
        // We rely on the provider's internal logic to simulate DB behavior.
        // We can check the "simulated DB state" if TestDataProvider exposes it,
        // or assert based on subsequent calls if applicable.
        // For this test, we'd assume if no exception, it worked as per provider's logic.
        // A more complex provider could track SQLs executed.
        assertTrue(((TestDataProvider) mockDataProvider).recordExists);
        assertNotNull(((TestDataProvider) mockDataProvider).dbRecord);
        assertEquals(testDto.getCenterName(), ((TestDataProvider) mockDataProvider).dbRecord.getCenterName());
    }

    @Test
    void save_should_update_existing_profile() {
        // Arrange
        CenterprofileRecord existingRecord = new CenterprofileRecord();
        existingRecord.setProfileId(1);
        existingRecord.setCenterName("Old Name");
        ((TestDataProvider) mockDataProvider).primeRecord(existingRecord);

        testDto.setCenterName("New Updated Name");

        // Act
        centerProfileRepository.save(testDto);

        // Assert
        assertTrue(((TestDataProvider) mockDataProvider).recordExists);
        assertEquals("New Updated Name", ((TestDataProvider) mockDataProvider).dbRecord.getCenterName());
    }

    @Test
    void getProfile_should_return_dto_if_exists() {
        // Arrange
        CenterprofileRecord dbRecord = new CenterprofileRecord();
        dbRecord.setProfileId(1);
        dbRecord.setCenterName(testDto.getCenterName());
        dbRecord.setAddressLine1(testDto.getAddressLine1());
        // ... set other fields similarly from testDto
        ((TestDataProvider) mockDataProvider).primeRecord(dbRecord);

        // Act
        Optional<CenterProfileDTO> resultOpt = centerProfileRepository.getProfile();

        // Assert
        assertTrue(resultOpt.isPresent());
        assertEquals(testDto.getCenterName(), resultOpt.get().getCenterName());
    }

    @Test
    void getProfile_should_return_empty_optional_if_not_exists() {
        // Arrange
        ((TestDataProvider) mockDataProvider).setRecordExists(false);

        // Act
        Optional<CenterProfileDTO> resultOpt = centerProfileRepository.getProfile();

        // Assert
        assertFalse(resultOpt.isPresent());
    }

    @Test
    void exists_should_return_true_if_profile_exists() {
         // Arrange
        ((TestDataProvider) mockDataProvider).primeRecord(new CenterprofileRecord()); // Any record makes it exist

        // Act
        boolean result = centerProfileRepository.exists();

        // Assert
        assertTrue(result);
    }

    @Test
    void exists_should_return_false_if_profile_not_exists() {
        // Arrange
        ((TestDataProvider) mockDataProvider).setRecordExists(false);

        // Act
        boolean result = centerProfileRepository.exists();

        // Assert
        assertFalse(result);
    }

    @Test
    void save_null_dto_should_throw_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            centerProfileRepository.save(null);
        });
    }
}
