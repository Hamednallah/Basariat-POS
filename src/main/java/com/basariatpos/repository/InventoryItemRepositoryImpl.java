package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.InventoryitemsRecord;
import com.basariatpos.model.InventoryItemDTO; // Ensure this is your DTO
import com.basariatpos.util.AppLogger;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record; // For handling joined results
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.INVENTORYITEMS;
import static com.basariatpos.db.generated.Tables.PRODUCTS;
import static com.basariatpos.db.generated.Tables.LOWSTOCKITEMSVIEW; // View for low stock items

public class InventoryItemRepositoryImpl implements InventoryItemRepository {

    private static final Logger logger = AppLogger.getLogger(InventoryItemRepositoryImpl.class);

    private InventoryItemDTO mapFullRecordToDto(Record r) {
        if (r == null) return null;
        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setInventoryItemId(r.get(INVENTORYITEMS.INVENTORY_ITEM_ID));
        dto.setProductId(r.get(INVENTORYITEMS.PRODUCT_ID));
        dto.setProductNameEn(r.get(PRODUCTS.PRODUCT_NAME_EN)); // Joined field
        dto.setProductNameAr(r.get(PRODUCTS.PRODUCT_NAME_AR)); // Joined field
        dto.setBrandName(r.get(INVENTORYITEMS.BRAND_NAME));
        dto.setItemSpecificNameEn(r.get(INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN));
        dto.setItemSpecificNameAr(r.get(INVENTORYITEMS.ITEM_SPECIFIC_NAME_AR));
        dto.setAttributes(r.get(INVENTORYITEMS.ATTRIBUTES) != null ? r.get(INVENTORYITEMS.ATTRIBUTES).data() : null);
        dto.setQuantityOnHand(r.get(INVENTORYITEMS.QUANTITY_ON_HAND));
        dto.setSellingPrice(r.get(INVENTORYITEMS.SELLING_PRICE));
        dto.setCostPrice(r.get(INVENTORYITEMS.COST_PRICE));
        dto.setMinStockLevel(r.get(INVENTORYITEMS.MIN_STOCK_LEVEL));
        dto.setUnitOfMeasure(r.get(INVENTORYITEMS.UNIT_OF_MEASURE));
        dto.setActive(r.get(INVENTORYITEMS.IS_ACTIVE));
        return dto;
    }

    // Mapper for records from LOWSTOCKITEMSVIEW
    // The view should ideally have columns that can directly map or alias to InventoryItemDTO fields
    private InventoryItemDTO mapViewRecordToDto(Record r) {
        if (r == null) return null;
        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setInventoryItemId(r.get(LOWSTOCKITEMSVIEW.INVENTORY_ITEM_ID));
        dto.setProductId(r.get(LOWSTOCKITEMSVIEW.PRODUCT_ID));
        dto.setProductNameEn(r.get(LOWSTOCKITEMSVIEW.PRODUCT_NAME_EN));
        dto.setProductNameAr(r.get(LOWSTOCKITEMSVIEW.PRODUCT_NAME_AR));
        dto.setBrandName(r.get(LOWSTOCKITEMSVIEW.BRAND_NAME));
        dto.setItemSpecificNameEn(r.get(LOWSTOCKITEMSVIEW.ITEM_SPECIFIC_NAME_EN));
        dto.setItemSpecificNameAr(r.get(LOWSTOCKITEMSVIEW.ITEM_SPECIFIC_NAME_AR));
        dto.setAttributes(r.get(LOWSTOCKITEMSVIEW.ATTRIBUTES) != null ? r.get(LOWSTOCKITEMSVIEW.ATTRIBUTES).data() : null);
        dto.setQuantityOnHand(r.get(LOWSTOCKITEMSVIEW.QUANTITY_ON_HAND));
        dto.setSellingPrice(r.get(LOWSTOCKITEMSVIEW.SELLING_PRICE));
        dto.setCostPrice(r.get(LOWSTOCKITEMSVIEW.COST_PRICE));
        dto.setMinStockLevel(r.get(LOWSTOCKITEMSVIEW.MIN_STOCK_LEVEL));
        dto.setUnitOfMeasure(r.get(LOWSTOCKITEMSVIEW.UNIT_OF_MEASURE));
        dto.setActive(r.get(LOWSTOCKITEMSVIEW.IS_ACTIVE)); // View should include this
        return dto;
    }


    private void mapDtoToRecord(InventoryItemDTO dto, InventoryitemsRecord record) {
        record.setProductId(dto.getProductId());
        record.setBrandName(dto.getBrandName());
        record.setItemSpecificNameEn(dto.getItemSpecificNameEn());
        record.setItemSpecificNameAr(dto.getItemSpecificNameAr());
        if (dto.getAttributes() != null) {
            record.setAttributes(org.jooq.JSONB.valueOf(dto.getAttributes()));
        } else {
            record.setAttributes(null);
        }
        record.setQuantityOnHand(dto.getQuantityOnHand());
        record.setSellingPrice(dto.getSellingPrice());
        record.setCostPrice(dto.getCostPrice());
        record.setMinStockLevel(dto.getMinStockLevel());
        record.setUnitOfMeasure(dto.getUnitOfMeasure());
        record.setIsActive(dto.isActive());
    }

    @Override
    public Optional<InventoryItemDTO> findById(int id) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return Optional.ofNullable(
                dsl.select(INVENTORYITEMS.asterisk(), PRODUCTS.PRODUCT_NAME_EN, PRODUCTS.PRODUCT_NAME_AR)
                   .from(INVENTORYITEMS)
                   .join(PRODUCTS).on(INVENTORYITEMS.PRODUCT_ID.eq(PRODUCTS.PRODUCT_ID))
                   .where(INVENTORYITEMS.INVENTORY_ITEM_ID.eq(id))
                   .fetchOne(this::mapFullRecordToDto)
            );
        } catch (DataAccessException e) {
            logger.error("Error finding inventory item by ID {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<InventoryItemDTO> findByProductId(int productId, boolean includeInactive) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            Condition condition = INVENTORYITEMS.PRODUCT_ID.eq(productId);
            if (!includeInactive) {
                condition = condition.and(INVENTORYITEMS.IS_ACTIVE.isTrue());
            }
            return dsl.select(INVENTORYITEMS.asterisk(), PRODUCTS.PRODUCT_NAME_EN, PRODUCTS.PRODUCT_NAME_AR)
                      .from(INVENTORYITEMS)
                      .join(PRODUCTS).on(INVENTORYITEMS.PRODUCT_ID.eq(PRODUCTS.PRODUCT_ID))
                      .where(condition)
                      .orderBy(INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN.asc())
                      .fetch(this::mapFullRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding inventory items by product ID {}: {}", productId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<InventoryItemDTO> findAll(boolean includeInactive) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            Condition condition = DSL.trueCondition();
            if (!includeInactive) {
                condition = condition.and(INVENTORYITEMS.IS_ACTIVE.isTrue());
            }
            return dsl.select(INVENTORYITEMS.asterisk(), PRODUCTS.PRODUCT_NAME_EN, PRODUCTS.PRODUCT_NAME_AR)
                      .from(INVENTORYITEMS)
                      .join(PRODUCTS).on(INVENTORYITEMS.PRODUCT_ID.eq(PRODUCTS.PRODUCT_ID))
                      .where(condition)
                      .orderBy(PRODUCTS.PRODUCT_NAME_EN.asc(), INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN.asc())
                      .fetch(this::mapFullRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding all inventory items: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<InventoryItemDTO> searchItems(String query, boolean includeInactive) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            String likeQuery = "%" + query.toLowerCase() + "%";
            Condition condition = DSL.lower(PRODUCTS.PRODUCT_NAME_EN).like(likeQuery)
                                   .or(DSL.lower(PRODUCTS.PRODUCT_NAME_AR).like(likeQuery))
                                   .or(DSL.lower(INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN).like(likeQuery))
                                   .or(DSL.lower(INVENTORYITEMS.ITEM_SPECIFIC_NAME_AR).like(likeQuery))
                                   .or(DSL.lower(INVENTORYITEMS.BRAND_NAME).like(likeQuery));
            if (!includeInactive) {
                condition = condition.and(INVENTORYITEMS.IS_ACTIVE.isTrue());
            }
            return dsl.select(INVENTORYITEMS.asterisk(), PRODUCTS.PRODUCT_NAME_EN, PRODUCTS.PRODUCT_NAME_AR)
                      .from(INVENTORYITEMS)
                      .join(PRODUCTS).on(INVENTORYITEMS.PRODUCT_ID.eq(PRODUCTS.PRODUCT_ID))
                      .where(condition)
                      .orderBy(PRODUCTS.PRODUCT_NAME_EN.asc(), INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN.asc())
                      .fetch(this::mapFullRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error searching inventory items with query '{}': {}", query, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public InventoryItemDTO save(InventoryItemDTO itemDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            InventoryitemsRecord record;
            if (itemDto.getInventoryItemId() > 0) { // Update
                record = dsl.fetchOne(INVENTORYITEMS, INVENTORYITEMS.INVENTORY_ITEM_ID.eq(itemDto.getInventoryItemId()));
                if (record == null) {
                    throw new DataAccessException("InventoryItem with ID " + itemDto.getInventoryItemId() + " not found for update.");
                }
            } else { // Insert
                record = dsl.newRecord(INVENTORYITEMS);
            }
            mapDtoToRecord(itemDto, record);
            record.store();
            itemDto.setInventoryItemId(record.getInventoryItemId()); // Update DTO with ID

            // Return DTO with potentially joined names (re-fetch or map from known product DTO if available)
            return findById(itemDto.getInventoryItemId())
                   .orElseThrow(() -> new DataAccessException("Failed to retrieve item after save, ID: " + itemDto.getInventoryItemId()));

        } catch (DataAccessException e) {
            logger.error("Error saving inventory item for product ID {}: {}", itemDto.getProductId(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void setActiveStatus(int inventoryItemId, boolean isActive) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            int updatedRows = dsl.update(INVENTORYITEMS)
                                 .set(INVENTORYITEMS.IS_ACTIVE, isActive)
                                 .where(INVENTORYITEMS.INVENTORY_ITEM_ID.eq(inventoryItemId))
                                 .execute();
            if (updatedRows == 0) throw new DataAccessException("InventoryItem ID " + inventoryItemId + " not found for status update.");
            logger.info("Active status for inventory item ID {} set to {}.", inventoryItemId, isActive);
        } catch (DataAccessException e) {
            logger.error("Error setting active status for inventory item ID {}: {}", inventoryItemId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void updateStockQuantity(int inventoryItemId, int newQuantityOnHand) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            int updatedRows = dsl.update(INVENTORYITEMS)
                                 .set(INVENTORYITEMS.QUANTITY_ON_HAND, newQuantityOnHand)
                                 .where(INVENTORYITEMS.INVENTORY_ITEM_ID.eq(inventoryItemId))
                                 .execute();
            if (updatedRows == 0) throw new DataAccessException("InventoryItem ID " + inventoryItemId + " not found for stock update.");
            logger.info("Stock quantity for inventory item ID {} updated to {}.", inventoryItemId, newQuantityOnHand);
        } catch (DataAccessException e) {
            logger.error("Error updating stock quantity for inventory item ID {}: {}", inventoryItemId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public boolean adjustStockQuantity(int inventoryItemId, int quantityChange) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            // Perform the atomic update
            int updatedRows = dsl.update(INVENTORYITEMS)
                                 .set(INVENTORYITEMS.QUANTITY_ON_HAND, INVENTORYITEMS.QUANTITY_ON_HAND.plus(quantityChange))
                                 .where(INVENTORYITEMS.INVENTORY_ITEM_ID.eq(inventoryItemId))
                                 .execute();

            if (updatedRows > 0) {
                logger.info("Stock quantity for inventory item ID {} adjusted by {}. Rows affected: {}", inventoryItemId, quantityChange, updatedRows);
                return true;
            } else {
                logger.warn("Stock adjustment for inventory item ID {} failed or item not found. Rows affected: {}", inventoryItemId, updatedRows);
                // Optionally throw DataAccessException if item not found is considered an error here
                // throw new DataAccessException("InventoryItem ID " + inventoryItemId + " not found for stock adjustment or no change made.");
                return false;
            }
        } catch (DataAccessException e) {
            logger.error("Error adjusting stock quantity for inventory item ID {}: {}", inventoryItemId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }


    @Override
    public void updateCostPrice(int inventoryItemId, BigDecimal newCostPrice) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            int updatedRows = dsl.update(INVENTORYITEMS)
                                 .set(INVENTORYITEMS.COST_PRICE, newCostPrice)
                                 .where(INVENTORYITEMS.INVENTORY_ITEM_ID.eq(inventoryItemId))
                                 .execute();
            if (updatedRows == 0) throw new DataAccessException("InventoryItem ID " + inventoryItemId + " not found for cost price update.");
            logger.info("Cost price for inventory item ID {} updated to {}.", inventoryItemId, newCostPrice);
        } catch (DataAccessException e) {
            logger.error("Error updating cost price for inventory item ID {}: {}", inventoryItemId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<InventoryItemDTO> getLowStockItems() {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            // Fields in LOWSTOCKITEMSVIEW are assumed to match or alias to what mapLowStockViewRecordToDto expects
            return dsl.selectFrom(LOWSTOCKITEMSVIEW)
                      .fetch(this::mapViewRecordToDto); // Use a specific mapper for the view
        } catch (DataAccessException e) {
            logger.error("Error fetching low stock items: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
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
