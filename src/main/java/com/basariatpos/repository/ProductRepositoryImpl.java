package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.ProductsRecord;
import com.basariatpos.model.ProductDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.Record; // For handling joined results
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.PRODUCTS;
import static com.basariatpos.db.generated.Tables.PRODUCTCATEGORIES;
import static com.basariatpos.db.generated.Tables.INVENTORYITEMS;


public class ProductRepositoryImpl implements ProductRepository {

    private static final Logger logger = AppLogger.getLogger(ProductRepositoryImpl.class);

    @Override
    public Optional<ProductDTO> findById(int id) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            return Optional.ofNullable(
                dsl.select(PRODUCTS.asterisk(),
                           PRODUCTCATEGORIES.CATEGORY_NAME_EN.as("category_name_en_alias"), // Alias to avoid name clash if any
                           PRODUCTCATEGORIES.CATEGORY_NAME_AR.as("category_name_ar_alias"))
                   .from(PRODUCTS)
                   .leftOuterJoin(PRODUCTCATEGORIES).on(PRODUCTS.CATEGORY_ID.eq(PRODUCTCATEGORIES.CATEGORY_ID))
                   .where(PRODUCTS.PRODUCT_ID.eq(id))
                   .fetchOne(this::mapJoinedRecordToDto)
            );
        } catch (DataAccessException e) {
            logger.error("Error finding product by ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<ProductDTO> findByCode(String code) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return Optional.ofNullable(
                dsl.select(PRODUCTS.asterisk(),
                           PRODUCTCATEGORIES.CATEGORY_NAME_EN.as("category_name_en_alias"),
                           PRODUCTCATEGORIES.CATEGORY_NAME_AR.as("category_name_ar_alias"))
                   .from(PRODUCTS)
                   .leftOuterJoin(PRODUCTCATEGORIES).on(PRODUCTS.CATEGORY_ID.eq(PRODUCTCATEGORIES.CATEGORY_ID))
                   .where(DSL.lower(PRODUCTS.PRODUCT_CODE).eq(code.toLowerCase()))
                   .fetchOne(this::mapJoinedRecordToDto)
            );
        } catch (DataAccessException e) {
            logger.error("Error finding product by code '{}': {}", code, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<ProductDTO> findAll() {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return dsl.select(PRODUCTS.asterisk(),
                              PRODUCTCATEGORIES.CATEGORY_NAME_EN.as("category_name_en_alias"),
                              PRODUCTCATEGORIES.CATEGORY_NAME_AR.as("category_name_ar_alias"))
                      .from(PRODUCTS)
                      .leftOuterJoin(PRODUCTCATEGORIES).on(PRODUCTS.CATEGORY_ID.eq(PRODUCTCATEGORIES.CATEGORY_ID))
                      .orderBy(PRODUCTS.PRODUCT_NAME_EN.asc())
                      .fetch(this::mapJoinedRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding all products: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<ProductDTO> findByCategoryId(int categoryId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return dsl.select(PRODUCTS.asterisk(),
                              PRODUCTCATEGORIES.CATEGORY_NAME_EN.as("category_name_en_alias"),
                              PRODUCTCATEGORIES.CATEGORY_NAME_AR.as("category_name_ar_alias"))
                      .from(PRODUCTS)
                      .leftOuterJoin(PRODUCTCATEGORIES).on(PRODUCTS.CATEGORY_ID.eq(PRODUCTCATEGORIES.CATEGORY_ID))
                      .where(PRODUCTS.CATEGORY_ID.eq(categoryId))
                      .orderBy(PRODUCTS.PRODUCT_NAME_EN.asc())
                      .fetch(this::mapJoinedRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding products by category ID {}: {}", categoryId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<ProductDTO> searchProducts(String query) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            String likeQuery = "%" + query.toLowerCase() + "%";
            return dsl.select(PRODUCTS.asterisk(),
                              PRODUCTCATEGORIES.CATEGORY_NAME_EN.as("category_name_en_alias"),
                              PRODUCTCATEGORIES.CATEGORY_NAME_AR.as("category_name_ar_alias"))
                      .from(PRODUCTS)
                      .leftOuterJoin(PRODUCTCATEGORIES).on(PRODUCTS.CATEGORY_ID.eq(PRODUCTCATEGORIES.CATEGORY_ID))
                      .where(DSL.lower(PRODUCTS.PRODUCT_NAME_EN).like(likeQuery)
                             .or(DSL.lower(PRODUCTS.PRODUCT_NAME_AR).like(likeQuery))
                             .or(DSL.lower(PRODUCTS.PRODUCT_CODE).like(likeQuery)))
                      .orderBy(PRODUCTS.PRODUCT_NAME_EN.asc())
                      .fetch(this::mapJoinedRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error searching products with query '{}': {}", query, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }


    @Override
    public ProductDTO save(ProductDTO productDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            ProductsRecord record;
            if (productDto.getProductId() > 0) { // Update
                record = dsl.fetchOne(PRODUCTS, PRODUCTS.PRODUCT_ID.eq(productDto.getProductId()));
                if (record == null) {
                    throw new DataAccessException("Product with ID " + productDto.getProductId() + " not found for update.");
                }
            } else { // Insert
                record = dsl.newRecord(PRODUCTS);
            }
            mapDtoToRecord(productDto, record); // categoryId is set here
            record.store();

            if (productDto.getProductId() <= 0) {
                productDto.setProductId(record.getProductId());
            }
            // After save, re-fetch with join to populate category names for the returned DTO
            return findById(productDto.getProductId())
                   .orElseThrow(() -> new DataAccessException("Failed to retrieve product after save, ID: " + productDto.getProductId()));
        } catch (DataAccessException e) {
            logger.error("Error saving product '{}': {}", productDto.getProductNameEn(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void deleteById(int id) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            int deletedRows = dsl.deleteFrom(PRODUCTS)
                                 .where(PRODUCTS.PRODUCT_ID.eq(id))
                                 .execute();
            if (deletedRows == 0) {
                 logger.warn("Product with ID {} not found for deletion.", id);
                // Consider throwing specific exception if preferred over silent failure
            } else {
                logger.info("Product with ID {} deleted successfully.", id);
            }
        } catch (DataAccessException e) {
            logger.error("Error deleting product with ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public boolean isProductInUse(int productId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return dsl.fetchExists(
                dsl.selectOne()
                   .from(INVENTORYITEMS)
                   .where(INVENTORYITEMS.PRODUCT_ID.eq(productId))
            );
        } catch (DataAccessException e) {
            logger.error("Error checking if product ID {} is in use: {}", productId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    // Helper Methods
    private ProductDTO mapJoinedRecordToDto(Record record) { // Record type from jOOQ
        if (record == null) return null;
        ProductsRecord productsRecord = record.into(PRODUCTS); // Extract part into generated record

        ProductDTO dto = new ProductDTO();
        dto.setProductId(productsRecord.getProductId());
        dto.setProductCode(productsRecord.getProductCode());
        dto.setProductNameEn(productsRecord.getProductNameEn());
        dto.setProductNameAr(productsRecord.getProductNameAr());
        dto.setCategoryId(productsRecord.getCategoryId());
        dto.setDescriptionEn(productsRecord.getDescriptionEn());
        dto.setDescriptionAr(productsRecord.getDescriptionAr());
        dto.setService(productsRecord.getIsService());
        dto.setStockItem(productsRecord.getIsStockItem());

        // Get joined category names (handle potential null if category_id was null or join failed)
        dto.setCategoryNameEn(record.get("category_name_en_alias", String.class));
        dto.setCategoryNameAr(record.get("category_name_ar_alias", String.class));

        return dto;
    }

    private void mapDtoToRecord(ProductDTO dto, ProductsRecord record) {
        record.setProductCode(dto.getProductCode());
        record.setProductNameEn(dto.getProductNameEn());
        record.setProductNameAr(dto.getProductNameAr());
        record.setCategoryId(dto.getCategoryId());
        record.setDescriptionEn(dto.getDescriptionEn());
        record.setDescriptionAr(dto.getDescriptionAr());
        record.setIsService(dto.isService());
        record.setIsStockItem(dto.isStockItem());
    }

    private void closeContext(DSLContext dslContext) {
        if (dslContext != null) {
            try {
                dslContext.close();
            } catch (Exception e) {
                logger.warn("Failed to close DSLContext.", e);
            }
        }
    }
}
