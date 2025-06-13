package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.PurchaseorderitemsRecord;
import com.basariatpos.db.generated.tables.records.PurchaseordersRecord;
import com.basariatpos.model.InventoryItemDTO; // Placeholder for potential future use in mapping
import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.Record; // For handling joined results
import org.jooq.Result;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.basariatpos.db.generated.Tables.PURCHASEORDERS;
import static com.basariatpos.db.generated.Tables.PURCHASEORDERITEMS;
import static com.basariatpos.db.generated.Tables.INVENTORYITEMS; // For item details
import static com.basariatpos.db.generated.Tables.PRODUCTS;     // For product names via inventory items
import static com.basariatpos.db.generated.Tables.USERS;         // For created_by_user_name

public class PurchaseOrderRepositoryImpl implements PurchaseOrderRepository {

    private static final Logger logger = AppLogger.getLogger(PurchaseOrderRepositoryImpl.class);

    // --- Mapper for full PurchaseOrderDTO with items ---
    private PurchaseOrderDTO mapFullPoRecordToDto(Record r, List<PurchaseOrderItemDTO> items) {
        if (r == null) return null;
        PurchaseordersRecord poRecord = r.into(PURCHASEORDERS);
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setPurchaseOrderId(poRecord.getPurchaseOrderId());
        dto.setOrderDate(poRecord.getOrderDate());
        dto.setSupplierName(poRecord.getSupplierName());
        dto.setTotalAmount(poRecord.getTotalAmount()); // This is a view column in DB, or calculated
        dto.setStatus(poRecord.getStatus());
        dto.setCreatedByUserId(poRecord.getCreatedByUserId());
        dto.setCreatedAt(poRecord.getCreatedAt());
        dto.setCreatedByName(r.get(USERS.FULL_NAME)); // From join
        dto.setItems(items);
        return dto;
    }

    // --- Mapper for PurchaseOrderDTO summary (no items or specific item details) ---
    private PurchaseOrderDTO mapPoSummaryRecordToDto(Record r) {
        if (r == null) return null;
        PurchaseordersRecord poRecord = r.into(PURCHASEORDERS);
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setPurchaseOrderId(poRecord.getPurchaseOrderId());
        dto.setOrderDate(poRecord.getOrderDate());
        dto.setSupplierName(poRecord.getSupplierName());
        dto.setTotalAmount(poRecord.getTotalAmount());
        dto.setStatus(poRecord.getStatus());
        dto.setCreatedByUserId(poRecord.getCreatedByUserId());
        dto.setCreatedAt(poRecord.getCreatedAt());
        dto.setCreatedByName(r.get(USERS.FULL_NAME)); // From join
        // Items list is intentionally not populated for summaries
        return dto;
    }


    // --- Mapper for PurchaseOrderItemDTO with joined names ---
    private PurchaseOrderItemDTO mapPoItemRecordToDto(Record r) {
        if (r == null) return null;
        PurchaseorderitemsRecord poiRecord = r.into(PURCHASEORDERITEMS);
        PurchaseOrderItemDTO itemDto = new PurchaseOrderItemDTO();
        itemDto.setPoItemId(poiRecord.getPoItemId());
        itemDto.setPurchaseOrderId(poiRecord.getPurchaseOrderId());
        itemDto.setInventoryItemId(poiRecord.getInventoryItemId());
        itemDto.setQuantityOrdered(poiRecord.getQuantityOrdered());
        itemDto.setQuantityReceived(poiRecord.getQuantityReceived());
        itemDto.setPurchasePricePerUnit(poiRecord.getPurchasePricePerUnit());
        itemDto.setSubtotal(poiRecord.getSubtotal()); // This is a view column in DB or calculated

        // Joined fields from InventoryItems and Products
        itemDto.setInventoryItemProductCode(r.get(PRODUCTS.PRODUCT_CODE));
        itemDto.setInventoryItemProductNameEn(r.get(PRODUCTS.PRODUCT_NAME_EN));
        itemDto.setInventoryItemSpecificNameEn(r.get(INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN));
        itemDto.setInventoryItemUnitOfMeasure(r.get(INVENTORYITEMS.UNIT_OF_MEASURE));

        return itemDto;
    }


    @Override
    public Optional<PurchaseOrderDTO> findById(int purchaseOrderId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            Record headerRecord = dsl.select(PURCHASEORDERS.asterisk(), USERS.FULL_NAME)
                                     .from(PURCHASEORDERS)
                                     .join(USERS).on(PURCHASEORDERS.CREATED_BY_USER_ID.eq(USERS.USER_ID))
                                     .where(PURCHASEORDERS.PURCHASE_ORDER_ID.eq(purchaseOrderId))
                                     .fetchOne();

            if (headerRecord == null) return Optional.empty();

            List<PurchaseOrderItemDTO> items = dsl.select(
                    PURCHASEORDERITEMS.asterisk(),
                    PRODUCTS.PRODUCT_CODE,
                    PRODUCTS.PRODUCT_NAME_EN,
                    INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN,
                    INVENTORYITEMS.UNIT_OF_MEASURE
                )
                .from(PURCHASEORDERITEMS)
                .join(INVENTORYITEMS).on(PURCHASEORDERITEMS.INVENTORY_ITEM_ID.eq(INVENTORYITEMS.INVENTORY_ITEM_ID))
                .join(PRODUCTS).on(INVENTORYITEMS.PRODUCT_ID.eq(PRODUCTS.PRODUCT_ID))
                .where(PURCHASEORDERITEMS.PURCHASE_ORDER_ID.eq(purchaseOrderId))
                .fetch(this::mapPoItemRecordToDto);

            return Optional.of(mapFullPoRecordToDto(headerRecord, items));
        } catch (DataAccessException e) {
            logger.error("Error finding PO by ID {}: {}", purchaseOrderId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<PurchaseOrderDTO> findAllSummaries() {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            return dsl.select(PURCHASEORDERS.asterisk(), USERS.FULL_NAME)
                      .from(PURCHASEORDERS)
                      .join(USERS).on(PURCHASEORDERS.CREATED_BY_USER_ID.eq(USERS.USER_ID))
                      .orderBy(PURCHASEORDERS.ORDER_DATE.desc(), PURCHASEORDERS.PURCHASE_ORDER_ID.desc())
                      .fetch(this::mapPoSummaryRecordToDto);
        } catch (DataAccessException e) {
            logger.error("Error finding all PO summaries: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public PurchaseOrderDTO saveNewOrderWithItems(PurchaseOrderDTO orderDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            return dsl.transactionResult(configuration -> {
                DSLContext transactionalDsl = DSL.using(configuration);
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

                PurchaseordersRecord poRecord = transactionalDsl.newRecord(PURCHASEORDERS);
                poRecord.setOrderDate(orderDto.getOrderDate());
                poRecord.setSupplierName(orderDto.getSupplierName());
                poRecord.setStatus(orderDto.getStatus() != null ? orderDto.getStatus() : "Pending");
                poRecord.setCreatedByUserId(orderDto.getCreatedByUserId());
                poRecord.setCreatedAt(now);
                // TotalAmount is calculated by DB trigger/view or should be sum of items here
                poRecord.store(); // Inserts and retrieves generated ID

                int generatedPoId = poRecord.getPurchaseOrderId();
                orderDto.setPurchaseOrderId(generatedPoId);
                orderDto.setCreatedAt(poRecord.getCreatedAt());
                // orderDto.setTotalAmount(poRecord.getTotalAmount()); // If DB sets it

                BigDecimal calculatedTotal = BigDecimal.ZERO;
                if (orderDto.getItems() != null) {
                    for (PurchaseOrderItemDTO itemDto : orderDto.getItems()) {
                        itemDto.setPurchaseOrderId(generatedPoId); // Link item to header
                        PurchaseorderitemsRecord itemRecord = transactionalDsl.newRecord(PURCHASEORDERITEMS);
                        itemRecord.setPurchaseOrderId(generatedPoId);
                        itemRecord.setInventoryItemId(itemDto.getInventoryItemId());
                        itemRecord.setQuantityOrdered(itemDto.getQuantityOrdered());
                        itemRecord.setQuantityReceived(0); // Initially 0
                        itemRecord.setPurchasePricePerUnit(itemDto.getPurchasePricePerUnit());
                        // Subtotal can be calculated by DB or here
                        BigDecimal itemSubtotal = itemDto.getPurchasePricePerUnit().multiply(new BigDecimal(itemDto.getQuantityOrdered()));
                        calculatedTotal = calculatedTotal.add(itemSubtotal);
                        itemRecord.setSubtotal(itemSubtotal); // Set if your table has it, otherwise DB calculates
                        itemRecord.store();
                        itemDto.setPoItemId(itemRecord.getPoItemId());
                    }
                }

                // If total_amount is not auto-calculated by DB based on items, update it
                // This might be redundant if a trigger handles it.
                if (poRecord.getTotalAmount() == null || poRecord.getTotalAmount().compareTo(calculatedTotal) != 0) {
                    transactionalDsl.update(PURCHASEORDERS)
                                    .set(PURCHASEORDERS.TOTAL_AMOUNT, calculatedTotal)
                                    .where(PURCHASEORDERS.PURCHASE_ORDER_ID.eq(generatedPoId))
                                    .execute();
                    orderDto.setTotalAmount(calculatedTotal);
                }

                logger.info("New PO ID {} with {} items saved.", generatedPoId, orderDto.getItems().size());
                return orderDto; // DTO now has all IDs
            });
        } catch (DataAccessException e) {
            logger.error("Error saving new PO with items: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl); // dsl is non-transactional, actual transaction was on configuration
        }
    }


    @Override
    public PurchaseOrderDTO updateOrderHeader(PurchaseOrderDTO orderDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            int updatedRows = dsl.update(PURCHASEORDERS)
               .set(PURCHASEORDERS.ORDER_DATE, orderDto.getOrderDate())
               .set(PURCHASEORDERS.SUPPLIER_NAME, orderDto.getSupplierName())
               .set(PURCHASEORDERS.STATUS, orderDto.getStatus())
               // Other updatable header fields like notes if any
               .where(PURCHASEORDERS.PURCHASE_ORDER_ID.eq(orderDto.getPurchaseOrderId()))
               .execute();

            if(updatedRows == 0) {
                throw new DataAccessException("PO ID " + orderDto.getPurchaseOrderId() + " not found for header update.");
            }
            logger.info("PO Header for ID {} updated.", orderDto.getPurchaseOrderId());
            // Return potentially re-fetched DTO to get any DB-side changes (like total_amount if items changed status)
            return findById(orderDto.getPurchaseOrderId())
                   .orElseThrow(() -> new DataAccessException("Failed to retrieve PO after header update, ID: " + orderDto.getPurchaseOrderId()));
        } catch (DataAccessException e) {
            logger.error("Error updating PO header for ID {}: {}", orderDto.getPurchaseOrderId(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public PurchaseOrderItemDTO saveOrderItem(PurchaseOrderItemDTO itemDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            PurchaseorderitemsRecord record;
            if(itemDto.getPoItemId() > 0) { // Update
                record = dsl.fetchOne(PURCHASEORDERITEMS, PURCHASEORDERITEMS.PO_ITEM_ID.eq(itemDto.getPoItemId()));
                if (record == null) throw new DataAccessException("PO Item ID " + itemDto.getPoItemId() + " not found.");
            } else { // Insert
                record = dsl.newRecord(PURCHASEORDERITEMS);
                record.setPurchaseOrderId(itemDto.getPurchaseOrderId());
                record.setQuantityReceived(0); // Default for new item
            }
            record.setInventoryItemId(itemDto.getInventoryItemId());
            record.setQuantityOrdered(itemDto.getQuantityOrdered());
            record.setPurchasePricePerUnit(itemDto.getPurchasePricePerUnit());
            // Subtotal could be calculated by DB or here
            BigDecimal itemSubtotal = itemDto.getPurchasePricePerUnit().multiply(new BigDecimal(itemDto.getQuantityOrdered()));
            record.setSubtotal(itemSubtotal);

            record.store();
            itemDto.setPoItemId(record.getPoItemId());
            itemDto.setSubtotal(itemSubtotal); // Ensure DTO has calculated subtotal

            logger.info("PO Item ID {} for PO ID {} saved.", itemDto.getPoItemId(), itemDto.getPurchaseOrderId());
            // Note: This does not update PO header total_amount. That should be handled by a trigger or service logic.
            return itemDto;
        } catch (DataAccessException e) {
            logger.error("Error saving PO Item for PO ID {}: {}", itemDto.getPurchaseOrderId(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void deleteOrderItem(int poItemId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            int deleted = dsl.deleteFrom(PURCHASEORDERITEMS)
                             .where(PURCHASEORDERITEMS.PO_ITEM_ID.eq(poItemId))
                             .execute();
            if(deleted == 0) logger.warn("PO Item ID {} not found for deletion.", poItemId);
            else logger.info("PO Item ID {} deleted.", poItemId);
            // Note: PO header total_amount needs update (trigger or service logic).
        } catch (DataAccessException e) {
            logger.error("Error deleting PO Item ID {}: {}", poItemId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void updateOrderItemReceivedQuantityAndPrice(int poItemId, int quantityReceived, BigDecimal purchasePricePerUnit) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            // Fetch current record to update quantity_received incrementally and check ordered quantity
            PurchaseorderitemsRecord itemRecord = dsl.fetchOne(PURCHASEORDERITEMS, PURCHASEORDERITEMS.PO_ITEM_ID.eq(poItemId));
            if (itemRecord == null) {
                throw new DataAccessException("PO Item ID " + poItemId + " not found for stock receiving.");
            }

            int newTotalReceived = itemRecord.getQuantityReceived() + quantityReceived;
            if (newTotalReceived > itemRecord.getQuantityOrdered()) {
                // This validation should ideally be in the service layer before calling repo.
                logger.warn("Attempt to receive more items ({}) for PO Item ID {} than ordered ({}). Clamping to ordered quantity.",
                            newTotalReceived, poItemId, itemRecord.getQuantityOrdered());
                newTotalReceived = itemRecord.getQuantityOrdered(); // Or throw exception
            }

            int updatedRows = dsl.update(PURCHASEORDERITEMS)
               .set(PURCHASEORDERITEMS.QUANTITY_RECEIVED, newTotalReceived)
               .set(PURCHASEORDERITEMS.PURCHASE_PRICE_PER_UNIT, purchasePricePerUnit) // Update price to actual received price
               // Subtotal might need recalculation based on quantity_received * purchase_price_per_unit for received value
               // Or keep subtotal as ordered_qty * ordered_price and handle variances elsewhere.
               // For now, assume subtotal is based on ordered quantity.
               .where(PURCHASEORDERITEMS.PO_ITEM_ID.eq(poItemId))
               .execute();

            if(updatedRows == 0) throw new DataAccessException("PO Item ID " + poItemId + " not found during update for stock receiving.");
            logger.info("Stock received for PO Item ID {}: {} units at price {}. Total received: {}", poItemId, quantityReceived, purchasePricePerUnit, newTotalReceived);
            // DB trigger `trg_UpdateInventoryOnStockReceive` should handle `InventoryItems` update.
            // DB trigger `trg_UpdatePOStatusOnItemReceive` should handle `PurchaseOrders` status update.
        } catch (DataAccessException e) {
            logger.error("Error updating received quantity for PO Item ID {}: {}", poItemId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void updateOrderStatus(int purchaseOrderId, String newStatus) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            int updatedRows = dsl.update(PURCHASEORDERS)
                                 .set(PURCHASEORDERS.STATUS, newStatus)
                                 .where(PURCHASEORDERS.PURCHASE_ORDER_ID.eq(purchaseOrderId))
                                 .execute();
            if(updatedRows == 0) throw new DataAccessException("PO ID " + purchaseOrderId + " not found for status update.");
            logger.info("Status for PO ID {} updated to '{}'.", purchaseOrderId, newStatus);
        } catch (DataAccessException e) {
            logger.error("Error updating status for PO ID {}: {}", purchaseOrderId, e.getMessage(), e);
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
