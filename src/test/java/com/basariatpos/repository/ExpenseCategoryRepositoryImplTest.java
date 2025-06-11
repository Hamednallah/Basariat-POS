package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.ExpensecategoriesRecord;
import com.basariatpos.model.ExpenseCategoryDTO;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Result;
import org.jooq.Record1;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.EXPENSECATEGORIES;
import static com.basariatpos.db.generated.Tables.EXPENSES;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryRepositoryImplTest {

    @InjectMocks
    private ExpenseCategoryRepositoryImpl categoryRepository;

    private DSLContext dslContext;
    private TestDataProvider mockDataProvider;
    private MockedStatic<DBManager> mockDBManagerStatic;

    private ExpenseCategoryDTO testCategoryDto;
    private ExpensecategoriesRecord testCategoryRecord;

    @BeforeEach
    void setUp() {
        mockDataProvider = new TestDataProvider();
        Connection connection = new MockConnection(mockDataProvider);
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);

        testCategoryDto = new ExpenseCategoryDTO(1, "Travel EN", "سفر", true);
        testCategoryRecord = new ExpensecategoriesRecord();
        testCategoryRecord.setExpenseCategoryId(1);
        testCategoryRecord.setCategoryNameEn("Travel EN");
        testCategoryRecord.setCategoryNameAr("سفر");
        testCategoryRecord.setIsActive(true);
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close();
    }

    private static class TestDataProvider implements MockDataProvider {
        ExpensecategoriesRecord recordToReturn;
        List<ExpensecategoriesRecord> recordsListToReturn = new ArrayList<>();
        boolean expectExists = false; // For isCategoryInUse
        String lastSQL;
        int nextId = 100;

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("SELECT")) {
                if (lastSQL.contains("FROM \"PUBLIC\".\"EXPENSECATEGORIES\"") && lastSQL.contains("WHERE \"PUBLIC\".\"EXPENSECATEGORIES\".\"EXPENSE_CATEGORY_ID\" = ?")) {
                    Result<ExpensecategoriesRecord> result = create.newResult(EXPENSECATEGORIES);
                    if (recordToReturn != null && recordToReturn.getExpenseCategoryId().equals(ctx.bindings()[0])) {
                        result.add(recordToReturn);
                    }
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("FROM \"PUBLIC\".\"EXPENSECATEGORIES\"") && lastSQL.contains("WHERE LOWER(\"PUBLIC\".\"EXPENSECATEGORIES\".\"CATEGORY_NAME_EN\") = ?")) {
                    Result<ExpensecategoriesRecord> result = create.newResult(EXPENSECATEGORIES);
                    if (recordToReturn != null && recordToReturn.getCategoryNameEn().equalsIgnoreCase((String)ctx.bindings()[0])) {
                        result.add(recordToReturn);
                    }
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("FROM \"PUBLIC\".\"EXPENSECATEGORIES\"")) { // findAll
                    Result<ExpensecategoriesRecord> result = create.newResult(EXPENSECATEGORIES);
                    result.addAll(recordsListToReturn);
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("SELECT EXISTS(SELECT 1 FROM \"PUBLIC\".\"EXPENSES\" WHERE \"PUBLIC\".\"EXPENSES\".\"EXPENSE_CATEGORY_ID\" = ?)")) { // isCategoryInUse
                    Result<Record1<Boolean>> result = create.newResult(DSL.field("exists", SQLDataType.BOOLEAN));
                    result.add(create.newRecord(DSL.field("exists", SQLDataType.BOOLEAN)).values(expectExists));
                    mock[0] = new MockResult(1, result);
                } else {
                     mock[0] = new MockResult(0, create.newResult(EXPENSECATEGORIES));
                }
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"EXPENSECATEGORIES\"")) {
                ExpensecategoriesRecord insertedRecord = create.newRecord(EXPENSECATEGORIES);
                insertedRecord.setExpenseCategoryId(nextId++);
                insertedRecord.setCategoryNameEn((String)ctx.bindings()[0]);
                insertedRecord.setCategoryNameAr((String)ctx.bindings()[1]);
                insertedRecord.setIsActive((Boolean)ctx.bindings()[2]);
                Result<ExpensecategoriesRecord> result = create.newResult(EXPENSECATEGORIES);
                result.add(insertedRecord);
                mock[0] = new MockResult(1, result);
                this.recordToReturn = insertedRecord;
            } else if (lastSQL.startsWith("UPDATE \"PUBLIC\".\"EXPENSECATEGORIES\"")) {
                mock[0] = new MockResult(1, create.newResult(EXPENSECATEGORIES));
            } else {
                System.err.println("Unhandled SQL in MockDataProvider (ExpenseCategory): " + lastSQL);
                mock[0] = new MockResult(0, create.newResult());
            }
            return mock;
        }
        public void setRecordToReturn(ExpensecategoriesRecord r) { this.recordToReturn = r; }
        public void setRecordsListToReturn(List<ExpensecategoriesRecord> l) { this.recordsListToReturn = l; }
        public void setExpectExists(boolean e) { this.expectExists = e; }
    }

    @Test
    void findById_exists_returnsDto() {
        mockDataProvider.setRecordToReturn(testCategoryRecord);
        Optional<ExpenseCategoryDTO> result = categoryRepository.findById(1);
        assertTrue(result.isPresent());
        assertEquals(testCategoryDto.getCategoryNameEn(), result.get().getCategoryNameEn());
    }

    @Test
    void save_newCategory_insertsAndReturnsDtoWithId() {
        ExpenseCategoryDTO newCategory = new ExpenseCategoryDTO("Food EN", "طعام", true);
        ExpenseCategoryDTO savedCategory = categoryRepository.save(newCategory);
        assertNotNull(savedCategory);
        assertTrue(savedCategory.getExpenseCategoryId() >= 100);
        assertEquals("Food EN", savedCategory.getCategoryNameEn());
    }

    @Test
    void isCategoryInUse_inUse_returnsTrue() {
        mockDataProvider.setExpectExists(true);
        boolean isInUse = categoryRepository.isCategoryInUse(1);
        assertTrue(isInUse);
        assertTrue(mockDataProvider.lastSQL.contains("SELECT EXISTS(SELECT 1 FROM \"PUBLIC\".\"EXPENSES\" WHERE \"PUBLIC\".\"EXPENSES\".\"EXPENSE_CATEGORY_ID\" = ?)"));
    }

    @Test
    void isCategoryInUse_notInUse_returnsFalse() {
        mockDataProvider.setExpectExists(false);
        boolean isInUse = categoryRepository.isCategoryInUse(1);
        assertFalse(isInUse);
    }

    // Add more tests for findByNameEn, findByNameAr, findAll, save (update), setActiveStatus
}
