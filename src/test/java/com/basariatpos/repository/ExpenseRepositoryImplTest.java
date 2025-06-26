package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.ExpensesRecord;
import com.basariatpos.model.ExpenseDTO;

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

import static com.basariatpos.db.generated.Tables.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExpenseRepositoryImplTest {

    private ExpenseRepositoryImpl expenseRepository;
    private DSLContext dslContext;
    private TestDataProvider mockDataProvider;
    private MockedStatic<DBManager> mockDBManagerStatic;

    @BeforeEach
    void setUp() {
        mockDataProvider = new TestDataProvider();
        MockConnection connection = new MockConnection(mockDataProvider);
        // Use the actual dialect of your database for parsing and SQL generation consistency
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        // It seems ExpenseRepositoryImpl uses a default constructor that calls DBManager.getDSLContext()
        // So, we mock the static DBManager.getDSLContext() to return our test dslContext.
        // If ExpenseRepositoryImpl had a constructor for DSLContext injection, that would be cleaner for testing.
        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);

        expenseRepository = new ExpenseRepositoryImpl(); // This will now use the mocked DBManager
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close();
    }

    private static class TestDataProvider implements MockDataProvider {
        ExpensesRecord expenseToReturnOnFetch;
        List<Record> expenseListToReturn = new ArrayList<>();
        String lastSQL;
        int nextExpenseId = 1;

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"EXPENSES\"")) {
                ExpensesRecord insertedRecord = create.newRecord(EXPENSES);
                insertedRecord.setExpenseId(nextExpenseId++);
                // Simulate setting values from bindings if needed for more detailed tests
                mock[0] = new MockResult(1, DSL.result(insertedRecord));
            } else if (lastSQL.startsWith("UPDATE \"PUBLIC\".\"EXPENSES\"")) {
                mock[0] = new MockResult(1, create.newResult(EXPENSES)); // Assume 1 row updated
            } else if (lastSQL.startsWith("SELECT ") && lastSQL.contains("FROM \"PUBLIC\".\"EXPENSES\"") && lastSQL.contains("WHERE \"PUBLIC\".\"EXPENSES\".\"EXPENSE_ID\" = ?")) {
                Result<Record> result = create.newResult(EXPENSES.asterisk(), EXPENSECATEGORIES.CATEGORY_NAME_EN, EXPENSECATEGORIES.CATEGORY_NAME_AR, USERS.FULL_NAME, BANKNAMES.BANK_NAME_EN, BANKNAMES.BANK_NAME_AR);
                if (expenseToReturnOnFetch != null && expenseToReturnOnFetch.getExpenseId().equals(ctx.bindings()[0])) {
                    // Create a mock Record that includes joined fields
                    Record r = create.newRecord(EXPENSES.asterisk(), EXPENSECATEGORIES.CATEGORY_NAME_EN, EXPENSECATEGORIES.CATEGORY_NAME_AR, USERS.FULL_NAME, BANKNAMES.BANK_NAME_EN, BANKNAMES.BANK_NAME_AR);
                    r.from(expenseToReturnOnFetch);
                    r.setValue(EXPENSECATEGORIES.CATEGORY_NAME_EN, "Mock Category EN");
                    r.setValue(USERS.FULL_NAME, "Mock User");
                    result.add(r);
                }
                mock[0] = new MockResult(result.size(), result);
            } else if (lastSQL.startsWith("SELECT ") && lastSQL.contains("FROM \"PUBLIC\".\"EXPENSES\"")) { // For findAllFiltered
                Result<Record> result = create.newResult(EXPENSES.asterisk(), EXPENSECATEGORIES.CATEGORY_NAME_EN, EXPENSECATEGORIES.CATEGORY_NAME_AR, USERS.FULL_NAME, BANKNAMES.BANK_NAME_EN, BANKNAMES.BANK_NAME_AR);
                result.addAll(expenseListToReturn);
                mock[0] = new MockResult(result.size(), result);
            } else {
                // System.err.println("Unhandled SQL in Expense TestDataProvider: " + lastSQL);
                mock[0] = new MockResult(0, create.newResult());
            }
            return mock;
        }
    }

    @Test
    void save_newExpense_insertsAndReturnsDtoWithId() throws RepositoryException {
        ExpenseDTO newExpense = new ExpenseDTO();
        newExpense.setExpenseDate(LocalDate.now());
        newExpense.setExpenseCategoryId(1);
        newExpense.setDescription("Test New Expense");
        newExpense.setAmount(new BigDecimal("100.50"));
        newExpense.setPaymentMethod("Cash");
        newExpense.setCreatedByUserId(1); // Service should set this

        ExpenseDTO result = expenseRepository.save(newExpense);

        assertNotNull(result);
        assertTrue(result.getExpenseId() > 0); // ID should be generated
        assertEquals("Test New Expense", result.getDescription());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertTrue(mockDataProvider.lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"EXPENSES\""));
    }

    @Test
    void save_existingExpense_updatesAndReturnsDto() throws RepositoryException {
        int existingExpenseId = 1;
        ExpenseDTO existingExpense = new ExpenseDTO();
        existingExpense.setExpenseId(existingExpenseId);
        existingExpense.setExpenseDate(LocalDate.now());
        existingExpense.setExpenseCategoryId(1);
        existingExpense.setDescription("Updated Test Expense");
        existingExpense.setAmount(new BigDecimal("120.00"));
        existingExpense.setPaymentMethod("Cash");
        existingExpense.setCreatedByUserId(1);
        existingExpense.setCreatedAt(OffsetDateTime.now().minusDays(1)); // Existing created_at

        // Mock fetchOne for update
        ExpensesRecord dbRecord = dslContext.newRecord(EXPENSES);
        dbRecord.setExpenseId(existingExpenseId);
        dbRecord.setCreatedAt(existingExpense.getCreatedAt());
        // ... set other fields as they would be in DB ...
        mockDataProvider.expenseToReturnOnFetch = dbRecord;


        ExpenseDTO result = expenseRepository.save(existingExpense);

        assertNotNull(result);
        assertEquals(existingExpenseId, result.getExpenseId());
        assertEquals("Updated Test Expense", result.getDescription());
        assertTrue(result.getUpdatedAt().isAfter(result.getCreatedAt()));
        assertTrue(mockDataProvider.lastSQL.startsWith("UPDATE \"PUBLIC\".\"EXPENSES\""));
    }

    @Test
    void findById_existingExpense_returnsPopulatedDto() throws RepositoryException {
        int expenseId = 1;
        mockDataProvider.expenseToReturnOnFetch = dslContext.newRecord(EXPENSES);
        mockDataProvider.expenseToReturnOnFetch.setExpenseId(expenseId);
        mockDataProvider.expenseToReturnOnFetch.setExpenseDate(LocalDate.now());
        mockDataProvider.expenseToReturnOnFetch.setExpenseCategoryId(1);
        mockDataProvider.expenseToReturnOnFetch.setCreatedByUserId(1);
        mockDataProvider.expenseToReturnOnFetch.setAmount(BigDecimal.TEN);
        // ... set other necessary fields ...

        Optional<ExpenseDTO> resultOpt = expenseRepository.findById(expenseId);

        assertTrue(resultOpt.isPresent());
        ExpenseDTO resultDto = resultOpt.get();
        assertEquals(expenseId, resultDto.getExpenseId());
        assertEquals("Mock Category EN", resultDto.getCategoryNameEnDisplay());
        assertEquals("Mock User", resultDto.getCreatedByNameDisplay());
    }

    @Test
    void findById_nonExistingExpense_returnsEmpty() throws RepositoryException {
        int expenseId = 999; // Assumed not to exist
        mockDataProvider.expenseToReturnOnFetch = null; // Ensure no record is found

        Optional<ExpenseDTO> resultOpt = expenseRepository.findById(expenseId);

        assertFalse(resultOpt.isPresent());
    }

    @Test
    void findAllFiltered_returnsMatchingExpenses() throws RepositoryException {
        DSLContext create = DSL.using(SQLDialect.POSTGRES);
        Record r1 = create.newRecord(EXPENSES.asterisk(), EXPENSECATEGORIES.CATEGORY_NAME_EN, EXPENSECATEGORIES.CATEGORY_NAME_AR, USERS.FULL_NAME, BANKNAMES.BANK_NAME_EN, BANKNAMES.BANK_NAME_AR);
        ExpensesRecord e1 = create.newRecord(EXPENSES);
        e1.setExpenseId(1); e1.setExpenseDate(LocalDate.now()); e1.setExpenseCategoryId(1); e1.setCreatedByUserId(1); e1.setAmount(BigDecimal.TEN);
        r1.from(e1);
        r1.setValue(EXPENSECATEGORIES.CATEGORY_NAME_EN, "Category A");
        r1.setValue(USERS.FULL_NAME, "User A");
        mockDataProvider.expenseListToReturn.add(r1);

        List<ExpenseDTO> results = expenseRepository.findAllFiltered(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), null);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Category A", results.get(0).getCategoryNameEnDisplay());
    }
}
