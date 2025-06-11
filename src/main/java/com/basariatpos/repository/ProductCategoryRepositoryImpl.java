package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.ProductcategoriesRecord;
import com.basariatpos.model.ProductCategoryDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.basariatpos.db.generated.Tables.PRODUCTCATEGORIES;
import static com.basariatpos.db.generated.Tables.PRODUCTS; // For isCategoryInUse check

public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private static final Logger logger = AppLogger.getLogger(ProductCategoryRepositoryImpl.class);

    @Override
    public Optional<ProductCategoryDTO> findById(int id) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            ProductcategoriesRecord record = dsl.selectFrom(PRODUCTCATEGORIES)
                                                .where(PRODUCTCATEGORIES.CATEGORY_ID.eq(id))
                                                .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding product category by ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<ProductCategoryDTO> findByNameEn(String nameEn) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            ProductcategoriesRecord record = dsl.selectFrom(PRODUCTCATEGORIES)
                                                .where(DSL.lower(PRODUCTCATEGORIES.CATEGORY_NAME_EN).eq(nameEn.toLowerCase()))
                                                .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding product category by English name '{}': {}", nameEn, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<ProductCategoryDTO> findByNameAr(String nameAr) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            ProductcategoriesRecord record = dsl.selectFrom(PRODUCTCATEGORIES)
                                                .where(PRODUCTCATEGORIES.CATEGORY_NAME_AR.eq(nameAr))
                                                .fetchOne();
            return Optional.ofNullable(record).map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding product category by Arabic name '{}': {}", nameAr, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<ProductCategoryDTO> findAll() {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            return dsl.selectFrom(PRODUCTCATEGORIES)
                      .orderBy(PRODUCTCATEGORIES.CATEGORY_NAME_EN.asc())
                      .fetch()
                      .map(this::mapRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding all product categories: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public ProductCategoryDTO save(ProductCategoryDTO categoryDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            ProductcategoriesRecord record;
            if (categoryDto.getCategoryId() > 0) { // Update
                record = dsl.fetchOne(PRODUCTCATEGORIES, PRODUCTCATEGORIES.CATEGORY_ID.eq(categoryDto.getCategoryId()));
                if (record == null) {
                    throw new DataAccessException("Product category with ID " + categoryDto.getCategoryId() + " not found for update.");
                }
            } else { // Insert
                record = dsl.newRecord(PRODUCTCATEGORIES);
            }
            mapDtoToRecord(categoryDto, record);
            record.store();

            if (categoryDto.getCategoryId() <= 0) {
                categoryDto.setCategoryId(record.getCategoryId());
            }
            logger.info("Product category '{}' (ID: {}) saved successfully.", categoryDto.getCategoryNameEn(), categoryDto.getCategoryId());
            return categoryDto;
        } catch (DataAccessException e) {
            logger.error("Error saving product category '{}': {}", categoryDto.getCategoryNameEn(), e.getMessage(), e);
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

            int deletedRows = dsl.deleteFrom(PRODUCTCATEGORIES)
                                 .where(PRODUCTCATEGORIES.CATEGORY_ID.eq(id))
                                 .execute();
            if (deletedRows == 0) {
                throw new DataAccessException("Product category with ID " + id + " not found for deletion.");
            }
            logger.info("Product category with ID {} deleted successfully.", id);
        } catch (DataAccessException e) {
            logger.error("Error deleting product category with ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public boolean isCategoryInUse(int id) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            return dsl.fetchExists(
                dsl.selectOne()
                   .from(PRODUCTS)
                   .where(PRODUCTS.CATEGORY_ID.eq(id))
            );
        } catch (DataAccessException e) {
            logger.error("Error checking if product category ID {} is in use: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    private ProductCategoryDTO mapRecordToDto(ProductcategoriesRecord record) {
        if (record == null) return null;
        return new ProductCategoryDTO(
                record.getCategoryId(),
                record.getCategoryNameEn(),
                record.getCategoryNameAr()
        );
    }

    private void mapDtoToRecord(ProductCategoryDTO dto, ProductcategoriesRecord record) {
        record.setCategoryNameEn(dto.getCategoryNameEn());
        record.setCategoryNameAr(dto.getCategoryNameAr());
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
