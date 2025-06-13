package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.ProductcategoriesRecord;
import com.basariatpos.db.generated.tables.records.ProductsRecord;
import com.basariatpos.model.ProductDTO;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.PRODUCTS;
import static com.basariatpos.db.generated.Tables.PRODUCTCATEGORIES;
import static com.basariatpos.db.generated.Tables.INVENTORYITEMS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryImplTest {

    @InjectMocks
    private ProductRepositoryImpl productRepository;

    private DSLContext dslContext;
    private TestDataProvider mockDataProvider;
    private MockedStatic<DBManager> mockDBManagerStatic;

    private ProductDTO testProductDto;
    private Record testJoinedRecord; // Use generic Record for joined results

    @BeforeEach
    void setUp() {
        mockDataProvider = new TestDataProvider();
        Connection connection = new MockConnection(mockDataProvider);
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);

        testProductDto = new ProductDTO(1, "P001", "Test Product EN", "منتج اختباري", 10,
                                       "Category EN", "فئة AR", "Desc EN", "وصف AR", false, true);

        // Simulate a joined record
        DSLContext create = DSL.using(SQLDialect.POSTGRES);
        testJoinedRecord = create.newRecord(PRODUCTS, PRODUCTCATEGORIES);
        testJoinedRecord.setValue(PRODUCTS.PRODUCT_ID, 1);
        testJoinedRecord.setValue(PRODUCTS.PRODUCT_CODE, "P001");
        testJoinedRecord.setValue(PRODUCTS.PRODUCT_NAME_EN, "Test Product EN");
        testJoinedRecord.setValue(PRODUCTS.PRODUCT_NAME_AR, "منتج اختباري");
        testJoinedRecord.setValue(PRODUCTS.CATEGORY_ID, 10);
        testJoinedRecord.setValue(PRODUCTS.DESCRIPTION_EN, "Desc EN");
        testJoinedRecord.setValue(PRODUCTS.DESCRIPTION_AR, "وصف AR");
        testJoinedRecord.setValue(PRODUCTS.IS_SERVICE, false);
        testJoinedRecord.setValue(PRODUCTS.IS_STOCK_ITEM, true);
        testJoinedRecord.setValue(PRODUCTCATEGORIES.CATEGORY_NAME_EN, "Category EN"); // This needs alias in mock provider
        testJoinedRecord.setValue(PRODUCTCATEGORIES.CATEGORY_NAME_AR, "فئة AR"); // This needs alias in mock provider
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close();
    }

    private static class TestDataProvider implements MockDataProvider {
        Record recordToReturn; // Use generic Record for flexibility with joins
        List<Record> recordsListToReturn = new ArrayList<>();
        boolean expectExists = false;
        String lastSQL;
        int nextId = 200; // For inserts

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("SELECT")) {
                // For queries that join PRODUCTS and PRODUCTCATEGORIES
                if (lastSQL.contains("FROM \"PUBLIC\".\"PRODUCTS\"") && lastSQL.contains("LEFT OUTER JOIN \"PUBLIC\".\"PRODUCTCATEGORIES\"")) {
                    Result<Record> result = create.newResult(PRODUCTS.fields()); // Simplified, should match actual projection
                     if (recordToReturn != null) {
                        // This simplified logic returns the single primed record if any specific WHERE matches,
                        // or the whole list for general queries.
                        boolean idMatch = lastSQL.contains("PRODUCTS\".\"PRODUCT_ID\" = ?") && recordToReturn.get(PRODUCTS.PRODUCT_ID).equals(ctx.bindings()[0]);
                        boolean codeMatch = lastSQL.contains("LOWER(\"PUBLIC\".\"PRODUCTS\".\"PRODUCT_CODE\") = ?") && recordToReturn.get(PRODUCTS.PRODUCT_CODE).toString().equalsIgnoreCase((String)ctx.bindings()[0]);

                        if (idMatch || codeMatch) {
                             result.add(recordToReturn);
                        } else if (!recordsListToReturn.isEmpty() && (lastSQL.contains("PRODUCTS\".\"CATEGORY_ID\" = ?") || !lastSQL.contains("WHERE"))) {
                            // For findByCategoryId or findAll
                            result.addAll(recordsListToReturn);
                        }
                    } else if (!recordsListToReturn.isEmpty() && !lastSQL.contains("WHERE")) { // findAll
                         result.addAll(recordsListToReturn);
                    }
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("SELECT EXISTS(SELECT 1 FROM \"PUBLIC\".\"INVENTORYITEMS\" WHERE \"PUBLIC\".\"INVENTORYITEMS\".\"PRODUCT_ID\" = ?)")) { // isProductInUse
                    Result<Record1<Boolean>> existsResult = create.newResult(DSL.field("exists", SQLDataType.BOOLEAN));
                    existsResult.add(create.newRecord(DSL.field("exists", SQLDataType.BOOLEAN)).values(expectExists));
                    mock[0] = new MockResult(1, existsResult);
                } else {
                     mock[0] = new MockResult(0, create.newResult(PRODUCTS));
                }
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"PRODUCTS\"")) {
                ProductsRecord insertedRecord = create.newRecord(PRODUCTS);
                insertedRecord.setProductId(nextId++);
                // Simplified mapping from bindings
                insertedRecord.setProductCode((String)ctx.bindings()[0]);
                insertedRecord.setProductNameEn((String)ctx.bindings()[1]);
                // ... map others
                Result<ProductsRecord> result = create.newResult(PRODUCTS);
                result.add(insertedRecord);
                mock[0] = new MockResult(1, result);
                this.recordToReturn = insertedRecord;
            } else if (lastSQL.startsWith("UPDATE \"PUBLIC\".\"PRODUCTS\"")) {
                mock[0] = new MockResult(1, create.newResult(PRODUCTS));
            } else if (lastSQL.startsWith("DELETE FROM \"PUBLIC\".\"PRODUCTS\"")) {
                 mock[0] = new MockResult(1, create.newResult(PRODUCTS));
            } else {
                System.err.println("Unhandled SQL in MockDataProvider (Product): " + lastSQL);
                mock[0] = new MockResult(0, create.newResult());
            }
            return mock;
        }
        public void setRecordToReturn(Record r) { this.recordToReturn = r; }
        public void setRecordsListToReturn(List<Record> l) { this.recordsListToReturn = l; }
        public void setExpectProductInUse(boolean e) { this.expectExists = e; }
    }

    @Test
    void findById_exists_returnsDtoWithCategoryNames() {
        // Adjust testJoinedRecord to use aliased category names as in mapJoinedRecordToDto
        DSLContext create = DSL.using(SQLDialect.POSTGRES);
        Record r = create.newRecord(PRODUCTS.PRODUCT_ID, PRODUCTS.PRODUCT_CODE, PRODUCTS.PRODUCT_NAME_EN, PRODUCTS.PRODUCT_NAME_AR, PRODUCTS.CATEGORY_ID, PRODUCTS.IS_SERVICE, PRODUCTS.IS_STOCK_ITEM,
                                    DSL.field("category_name_en_alias", String.class), DSL.field("category_name_ar_alias", String.class));
        r.setValue(PRODUCTS.PRODUCT_ID, 1);
        r.setValue(PRODUCTS.PRODUCT_CODE, "P001");
        r.setValue(PRODUCTS.PRODUCT_NAME_EN, "Test Product EN");
        r.setValue(PRODUCTS.PRODUCT_NAME_AR, "منتج اختباري");
        r.setValue(PRODUCTS.CATEGORY_ID, 10);
        r.setValue(PRODUCTS.IS_SERVICE, false);
        r.setValue(PRODUCTS.IS_STOCK_ITEM, true);
        r.setValue(DSL.field("category_name_en_alias", String.class), "Category EN");
        r.setValue(DSL.field("category_name_ar_alias", String.class), "فئة AR");
        mockDataProvider.setRecordToReturn(r);

        Optional<ProductDTO> result = productRepository.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Test Product EN", result.get().getProductNameEn());
        assertEquals("Category EN", result.get().getCategoryNameEn());
        assertEquals("فئة AR", result.get().getCategoryNameAr());
    }

    @Test
    void save_newProduct_insertsAndReturnsDtoWithId() {
        ProductDTO newProduct = new ProductDTO("P002", "New Product", "منتج جديد", 10, null, null, false, true);

        // For save, the repository calls findById after store to get joined category names.
        // So, mockDataProvider needs to be primed for that findById call.
        DSLContext create = DSL.using(SQLDialect.POSTGRES);
        Record r = create.newRecord(PRODUCTS.PRODUCT_ID, PRODUCTS.PRODUCT_CODE, PRODUCTS.PRODUCT_NAME_EN, PRODUCTS.PRODUCT_NAME_AR, PRODUCTS.CATEGORY_ID, PRODUCTS.IS_SERVICE, PRODUCTS.IS_STOCK_ITEM,
                                    DSL.field("category_name_en_alias", String.class), DSL.field("category_name_ar_alias", String.class));
        r.setValue(PRODUCTS.PRODUCT_ID, mockDataProvider.nextId); // This will be the ID assigned during INSERT simulation
        r.setValue(PRODUCTS.PRODUCT_CODE, "P002");
        r.setValue(PRODUCTS.PRODUCT_NAME_EN, "New Product");
        r.setValue(PRODUCTS.PRODUCT_NAME_AR, "منتج جديد");
        r.setValue(PRODUCTS.CATEGORY_ID, 10);
        r.setValue(PRODUCTS.IS_SERVICE, false);
        r.setValue(PRODUCTS.IS_STOCK_ITEM, true);
        r.setValue(DSL.field("category_name_en_alias", String.class), "Some Category EN"); // Mocked category name
        r.setValue(DSL.field("category_name_ar_alias", String.class), "فئة ما AR");
        mockDataProvider.setRecordToReturn(r); // This will be returned by findById post-insert

        ProductDTO savedProduct = productRepository.save(newProduct);

        assertNotNull(savedProduct);
        assertTrue(savedProduct.getProductId() >= 200);
        assertEquals("New Product", savedProduct.getProductNameEn());
        assertEquals("Some Category EN", savedProduct.getCategoryNameEn()); // Verify joined name
    }

    @Test
    void isProductInUse_productIsInUse_returnsTrue() {
        mockDataProvider.setExpectProductInUse(true);
        boolean inUse = productRepository.isProductInUse(1);
        assertTrue(inUse);
    }

    @Test
    void isProductInUse_productNotInUse_returnsFalse() {
        mockDataProvider.setExpectProductInUse(false);
        boolean inUse = productRepository.isProductInUse(1);
        assertFalse(inUse);
    }

    // Add more tests for findByCode, findAll, findByCategoryId, searchProducts, deleteById, save (update)
}
