package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.ProductcategoriesRecord;
import com.basariatpos.model.ProductCategoryDTO;
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

import static com.basariatpos.db.generated.Tables.PRODUCTCATEGORIES;
import static com.basariatpos.db.generated.Tables.PRODUCTS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductCategoryRepositoryImplTest {

    @InjectMocks
    private ProductCategoryRepositoryImpl categoryRepository;

    private DSLContext dslContext;
    private TestDataProvider mockDataProvider;
    private MockedStatic<DBManager> mockDBManagerStatic;

    private ProductCategoryDTO testCategoryDto;
    private ProductcategoriesRecord testCategoryRecord;

    @BeforeEach
    void setUp() {
        mockDataProvider = new TestDataProvider();
        Connection connection = new MockConnection(mockDataProvider);
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);

        testCategoryDto = new ProductCategoryDTO(1, "Frames", "إطارات");
        testCategoryRecord = new ProductcategoriesRecord();
        testCategoryRecord.setCategoryId(1);
        testCategoryRecord.setCategoryNameEn("Frames");
        testCategoryRecord.setCategoryNameAr("إطارات");
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close();
    }

    private static class TestDataProvider implements MockDataProvider {
        ProductcategoriesRecord recordToReturn;
        List<ProductcategoriesRecord> recordsListToReturn = new ArrayList<>();
        boolean expectExists = false; // For isCategoryInUse
        String lastSQL;
        int nextId = 200; // For inserts

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("SELECT")) {
                if (lastSQL.contains("FROM \"PUBLIC\".\"PRODUCTCATEGORIES\"") && lastSQL.contains("WHERE \"PUBLIC\".\"PRODUCTCATEGORIES\".\"CATEGORY_ID\" = ?")) {
                    Result<ProductcategoriesRecord> result = create.newResult(PRODUCTCATEGORIES);
                    if (recordToReturn != null && recordToReturn.getCategoryId().equals(ctx.bindings()[0])) {
                        result.add(recordToReturn);
                    }
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("FROM \"PUBLIC\".\"PRODUCTCATEGORIES\"") && lastSQL.contains("WHERE LOWER(\"PUBLIC\".\"PRODUCTCATEGORIES\".\"CATEGORY_NAME_EN\") = ?")) {
                    Result<ProductcategoriesRecord> result = create.newResult(PRODUCTCATEGORIES);
                    if (recordToReturn != null && recordToReturn.getCategoryNameEn().equalsIgnoreCase((String)ctx.bindings()[0])) {
                        result.add(recordToReturn);
                    }
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("FROM \"PUBLIC\".\"PRODUCTCATEGORIES\"")) { // findAll
                    Result<ProductcategoriesRecord> result = create.newResult(PRODUCTCATEGORIES);
                    result.addAll(recordsListToReturn);
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("SELECT EXISTS(SELECT 1 FROM \"PUBLIC\".\"PRODUCTS\" WHERE \"PUBLIC\".\"PRODUCTS\".\"CATEGORY_ID\" = ?)")) { // isCategoryInUse
                    Result<Record1<Boolean>> result = create.newResult(DSL.field("exists", SQLDataType.BOOLEAN));
                    result.add(create.newRecord(DSL.field("exists", SQLDataType.BOOLEAN)).values(expectExists));
                    mock[0] = new MockResult(1, result);
                } else {
                     mock[0] = new MockResult(0, create.newResult(PRODUCTCATEGORIES));
                }
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"PRODUCTCATEGORIES\"")) {
                ProductcategoriesRecord insertedRecord = create.newRecord(PRODUCTCATEGORIES);
                insertedRecord.setCategoryId(nextId++);
                insertedRecord.setCategoryNameEn((String)ctx.bindings()[0]);
                insertedRecord.setCategoryNameAr((String)ctx.bindings()[1]);
                Result<ProductcategoriesRecord> result = create.newResult(PRODUCTCATEGORIES);
                result.add(insertedRecord);
                mock[0] = new MockResult(1, result);
                this.recordToReturn = insertedRecord;
            } else if (lastSQL.startsWith("UPDATE \"PUBLIC\".\"PRODUCTCATEGORIES\"")) {
                mock[0] = new MockResult(1, create.newResult(PRODUCTCATEGORIES)); // Assume 1 row updated
            } else if (lastSQL.startsWith("DELETE FROM \"PUBLIC\".\"PRODUCTCATEGORIES\"")) {
                 mock[0] = new MockResult(1, create.newResult(PRODUCTCATEGORIES)); // Assume 1 row deleted
            }
            else {
                System.err.println("Unhandled SQL in MockDataProvider (ProductCategory): " + lastSQL);
                mock[0] = new MockResult(0, create.newResult());
            }
            return mock;
        }
        public void setRecordToReturn(ProductcategoriesRecord r) { this.recordToReturn = r; }
        public void setRecordsListToReturn(List<ProductcategoriesRecord> l) { this.recordsListToReturn = l; }
        public void setExpectExists(boolean e) { this.expectExists = e; }
    }

    @Test
    void findById_exists_returnsDto() {
        mockDataProvider.setRecordToReturn(testCategoryRecord);
        Optional<ProductCategoryDTO> result = categoryRepository.findById(1);
        assertTrue(result.isPresent());
        assertEquals(testCategoryDto.getCategoryNameEn(), result.get().getCategoryNameEn());
    }

    @Test
    void save_newCategory_insertsAndReturnsDtoWithId() {
        ProductCategoryDTO newCategory = new ProductCategoryDTO("Lenses", "عدسات");
        ProductCategoryDTO savedCategory = categoryRepository.save(newCategory);
        assertNotNull(savedCategory);
        assertTrue(savedCategory.getCategoryId() >= 200); // Default nextId
        assertEquals("Lenses", savedCategory.getCategoryNameEn());
    }

    @Test
    void deleteById_callsCorrectStatement() {
        categoryRepository.deleteById(1);
        assertTrue(mockDataProvider.lastSQL.startsWith("DELETE FROM \"PUBLIC\".\"PRODUCTCATEGORIES\""));
        assertTrue(mockDataProvider.lastSQL.contains("WHERE \"PUBLIC\".\"PRODUCTCATEGORIES\".\"CATEGORY_ID\" = 1"));
    }

    @Test
    void isCategoryInUse_inUse_returnsTrue() {
        mockDataProvider.setExpectExists(true);
        boolean isInUse = categoryRepository.isCategoryInUse(1);
        assertTrue(isInUse);
        assertTrue(mockDataProvider.lastSQL.contains("SELECT EXISTS(SELECT 1 FROM \"PUBLIC\".\"PRODUCTS\" WHERE \"PUBLIC\".\"PRODUCTS\".\"CATEGORY_ID\" = ?)"));
    }

    @Test
    void isCategoryInUse_notInUse_returnsFalse() {
        mockDataProvider.setExpectExists(false);
        boolean isInUse = categoryRepository.isCategoryInUse(1);
        assertFalse(isInUse);
    }

    // Add more tests for findByNameEn, findByNameAr, findAll, save (update)
}
