package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.SalesorderitemsRecord;
import com.basariatpos.db.generated.tables.records.SalesordersRecord;
import com.basariatpos.model.SalesOrderDTO;
import com.basariatpos.model.SalesOrderItemDTO;
import com.basariatpos.util.AppLogger;

import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.basariatpos.db.generated.Tables.*;
import static com.basariatpos.db.generated.Routines.recalculatesalesordersubtotal;


public class SalesOrderRepositoryImpl implements SalesOrderRepository {

    private static final Logger logger = AppLogger.getLogger(SalesOrderRepositoryImpl.class);

    // --- Mapper Methods ---

    private SalesOrderDTO mapRecordToOrderDTO(SalesordersRecord r, Record patientAndUserRecord, List<SalesOrderItemDTO> items) {
        if (r == null) return null;
        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setSalesOrderId(r.getSalesOrderId());
        dto.setPatientId(r.getPatientId());
        if (patientAndUserRecord != null && r.getPatientId() != null) {
            dto.setPatientSystemId(patientAndUserRecord.get(PATIENTS.PATIENT_SYSTEM_ID));
            dto.setPatientFullName(patientAndUserRecord.get(PATIENTS.FULL_NAME_EN)); // Assuming English for now
            dto.setPatientPhoneNumber(patientAndUserRecord.get(PATIENTS.PHONE_NUMBER)); // Added
            dto.setPatientWhatsappOptIn(patientAndUserRecord.get(PATIENTS.WHATSAPP_OPT_IN, boolean.class)); // Added
        }
        dto.setOrderDate(r.getOrderDate());
        dto.setStatus(r.getStatus());
        dto.setSubtotalAmount(r.getSubtotalAmount());
        dto.setDiscountAmount(r.getDiscountAmount());
        dto.setTotalAmount(r.getTotalAmount());
        dto.setAmountPaid(r.getAmountPaid());
        dto.setBalanceDue(r.getBalanceDue());
        dto.setCreatedByUserId(r.getCreatedByUserId());
        if (patientAndUserRecord != null) {
            dto.setCreatedByName(patientAndUserRecord.get(USERS.FULL_NAME)); // Joined from USERS table
        }
        dto.setShiftId(r.getShiftId());
        dto.setRemarks(r.getRemarks());
        dto.setItems(items != null ? items : new ArrayList<>());
        return dto;
    }

    private SalesOrderDTO mapRecordToOrderSummaryDTO(Record r) { // Used for findAllOrderSummaries
        if (r == null) return null;
        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setSalesOrderId(r.get(SALESORDERS.SALES_ORDER_ID));
        dto.setPatientId(r.get(SALESORDERS.PATIENT_ID));
        if (r.get(SALESORDERS.PATIENT_ID) != null) {
            dto.setPatientSystemId(r.get(PATIENTS.PATIENT_SYSTEM_ID));
            dto.setPatientFullName(r.get(PATIENTS.FULL_NAME_EN));
            dto.setPatientPhoneNumber(r.get(PATIENTS.PHONE_NUMBER)); // Added
            dto.setPatientWhatsappOptIn(r.get(PATIENTS.WHATSAPP_OPT_IN, boolean.class)); // Added
        }
        dto.setOrderDate(r.get(SALESORDERS.ORDER_DATE));
        dto.setStatus(r.get(SALESORDERS.STATUS));
        dto.setSubtotalAmount(r.get(SALESORDERS.SUBTOTAL_AMOUNT));
        dto.setDiscountAmount(r.get(SALESORDERS.DISCOUNT_AMOUNT));
        dto.setTotalAmount(r.get(SALESORDERS.TOTAL_AMOUNT));
        dto.setAmountPaid(r.get(SALESORDERS.AMOUNT_PAID));
        dto.setBalanceDue(r.get(SALESORDERS.BALANCE_DUE));
        dto.setCreatedByUserId(r.get(SALESORDERS.CREATED_BY_USER_ID));
        dto.setCreatedByName(r.get(USERS.FULL_NAME));
        dto.setShiftId(r.get(SALESORDERS.SHIFT_ID));
        dto.setRemarks(r.get(SALESORDERS.REMARKS));
        // items list is not populated for summaries
        return dto;
    }


    private void mapOrderDTOToRecord(SalesOrderDTO dto, SalesordersRecord r) {
        if (dto.getPatientId() != null && dto.getPatientId() > 0) {
            r.setPatientId(dto.getPatientId());
        } else {
            r.setPatientId(null);
        }
        r.setOrderDate(dto.getOrderDate() != null ? dto.getOrderDate() : OffsetDateTime.now(ZoneOffset.UTC));
        r.setStatus(dto.getStatus());
        r.setSubtotalAmount(dto.getSubtotalAmount());
        r.setDiscountAmount(dto.getDiscountAmount());
        r.setTotalAmount(dto.getTotalAmount());
        r.setAmountPaid(dto.getAmountPaid());
        r.setBalanceDue(dto.getBalanceDue());
        r.setCreatedByUserId(dto.getCreatedByUserId());
        r.setShiftId(dto.getShiftId());
        r.setRemarks(dto.getRemarks());
    }

    private SalesOrderItemDTO mapRecordToOrderItemDTO(SalesorderitemsRecord r, Record itemProductDetails) {
        if (r == null) return null;
        SalesOrderItemDTO itemDto = new SalesOrderItemDTO();
        itemDto.setSoItemId(r.getSoItemId());
        itemDto.setSalesOrderId(r.getSalesOrderId());
        itemDto.setInventoryItemId(r.getInventoryItemId());
        itemDto.setServiceProductId(r.getServiceProductId());
        itemDto.setQuantity(r.getQuantity());
        itemDto.setUnitPrice(r.getUnitPrice());
        itemDto.setItemSubtotal(r.getItemSubtotal());
        itemDto.setPrescriptionDetails(r.getPrescriptionDetails() != null ? r.getPrescriptionDetails().data() : null);
        itemDto.setIsCustomLenses(r.getIsCustomLenses());
        itemDto.setRestockedOnAbandonment(r.getIsRestockedOnAbandonment());
        itemDto.setDescription(r.getDescription());

        if (itemProductDetails != null) {
            if (r.getInventoryItemId() != null) {
                // Assuming itemProductDetails contains fields from INVENTORYITEMS joined with PRODUCTS
                itemDto.setItemDisplayNameEn(itemProductDetails.get(PRODUCTS.PRODUCT_NAME_EN));
                itemDto.setItemDisplaySpecificNameEn(itemProductDetails.get(INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN));
            } else if (r.getServiceProductId() != null) {
                // Assuming itemProductDetails contains fields from PRODUCTS (for services)
                itemDto.setItemDisplayNameEn(itemProductDetails.get(PRODUCTS.PRODUCT_NAME_EN));
            }
        }
        return itemDto;
    }

    private void mapOrderItemDTOToRecord(SalesOrderItemDTO dto, SalesorderitemsRecord r) {
        r.setSalesOrderId(dto.getSalesOrderId());
        r.setInventoryItemId(dto.getInventoryItemId());
        r.setServiceProductId(dto.getServiceProductId());
        r.setQuantity(dto.getQuantity());
        r.setUnitPrice(dto.getUnitPrice());
        // itemSubtotal is usually calculated by DB
        if (dto.getPrescriptionDetails() != null) {
            r.setPrescriptionDetails(JSONB.valueOf(dto.getPrescriptionDetails()));
        } else {
            r.setPrescriptionDetails(null);
        }
        r.setIsCustomLenses(dto.isCustomLenses());
        r.setIsRestockedOnAbandonment(dto.isRestockedOnAbandonment());
        r.setDescription(dto.getDescription());
    }


    // --- Repository Methods ---

    @Override
    public SalesOrderDTO saveOrderHeader(SalesOrderDTO orderDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            SalesordersRecord record;
            if (orderDto.getSalesOrderId() > 0) { // Update
                record = dsl.fetchOne(SALESORDERS, SALESORDERS.SALES_ORDER_ID.eq(orderDto.getSalesOrderId()));
                if (record == null) {
                    throw new DataAccessException("SalesOrder with ID " + orderDto.getSalesOrderId() + " not found for update.");
                }
            } else { // Insert
                record = dsl.newRecord(SALESORDERS);
            }
            mapOrderDTOToRecord(orderDto, record);
            record.store(); // Inserts or updates
            orderDto.setSalesOrderId(record.getSalesOrderId());
            return orderDto; // DTO now has the ID, other fields might need re-fetch for full consistency
        } catch (DataAccessException e) {
            logger.error("Error saving sales order header: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public SalesOrderItemDTO saveOrderItem(SalesOrderItemDTO itemDto) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            SalesorderitemsRecord record;
            if (itemDto.getSoItemId() > 0) { // Update
                record = dsl.fetchOne(SALESORDERITEMS, SALESORDERITEMS.SO_ITEM_ID.eq(itemDto.getSoItemId()));
                if (record == null) {
                    throw new DataAccessException("SalesOrderItem with ID " + itemDto.getSoItemId() + " not found.");
                }
            } else { // Insert
                record = dsl.newRecord(SALESORDERITEMS);
            }
            mapOrderItemDTOToRecord(itemDto, record);
            record.store();
            itemDto.setSoItemId(record.getSoItemId());
            return itemDto;
        } catch (DataAccessException e) {
            logger.error("Error saving sales order item for order ID {}: {}", itemDto.getSalesOrderId(), e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void deleteOrderItem(int soItemId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            dsl.deleteFrom(SALESORDERITEMS)
               .where(SALESORDERITEMS.SO_ITEM_ID.eq(soItemId))
               .execute();
            logger.info("Deleted sales order item with ID: {}", soItemId);
        } catch (DataAccessException e) {
            logger.error("Error deleting sales order item with ID {}: {}", soItemId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void deleteOrderItemsBySalesOrderId(int salesOrderId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            dsl.deleteFrom(SALESORDERITEMS)
               .where(SALESORDERITEMS.SALES_ORDER_ID.eq(salesOrderId))
               .execute();
            logger.info("Deleted all items for sales order ID: {}", salesOrderId);
        } catch (DataAccessException e) {
            logger.error("Error deleting items for sales order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public Optional<SalesOrderDTO> findById(int salesOrderId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            SalesordersRecord orderRecord = dsl.selectFrom(SALESORDERS)
                                               .where(SALESORDERS.SALES_ORDER_ID.eq(salesOrderId))
                                               .fetchOne();
            if (orderRecord == null) {
                return Optional.empty();
            }

            Record patientAndUserRecord = null;
            if (orderRecord.getPatientId() != null) {
                 patientAndUserRecord = dsl.select(
                                       PATIENTS.PATIENT_SYSTEM_ID,
                                       PATIENTS.FULL_NAME_EN,
                                       PATIENTS.PHONE_NUMBER,      // Added
                                       PATIENTS.WHATSAPP_OPT_IN,   // Added
                                       USERS.FULL_NAME
                                   )
                                   .from(SALESORDERS)
                                   .leftOuterJoin(PATIENTS).on(SALESORDERS.PATIENT_ID.eq(PATIENTS.PATIENT_ID))
                                   .join(USERS).on(SALESORDERS.CREATED_BY_USER_ID.eq(USERS.USER_ID))
                                   .where(SALESORDERS.SALES_ORDER_ID.eq(salesOrderId))
                                   .fetchOne();
            } else {
                 patientAndUserRecord = dsl.select(USERS.FULL_NAME)
                                   .from(SALESORDERS)
                                   .join(USERS).on(SALESORDERS.CREATED_BY_USER_ID.eq(USERS.USER_ID))
                                   .where(SALESORDERS.SALES_ORDER_ID.eq(salesOrderId))
                                   .fetchOne();
            }


            List<SalesOrderItemDTO> items = dsl.select(SALESORDERITEMS.fields())
                .select(
                    PRODUCTS.PRODUCT_NAME_EN, // For both services and inventory items' product parent
                    INVENTORYITEMS.ITEM_SPECIFIC_NAME_EN // Only for inventory items
                )
                .from(SALESORDERITEMS)
                .leftOuterJoin(INVENTORYITEMS).on(SALESORDERITEMS.INVENTORY_ITEM_ID.eq(INVENTORYITEMS.INVENTORY_ITEM_ID))
                .leftOuterJoin(PRODUCTS).on(
                    PRODUCTS.PRODUCT_ID.eq(INVENTORYITEMS.PRODUCT_ID) // Join for inventory items
                    .or(PRODUCTS.PRODUCT_ID.eq(SALESORDERITEMS.SERVICE_PRODUCT_ID)) // Join for service items
                )
                .where(SALESORDERITEMS.SALES_ORDER_ID.eq(salesOrderId))
                .fetch(r -> {
                    SalesorderitemsRecord soiRecord = r.into(SALESORDERITEMS);
                    // The additional selected fields (PRODUCT_NAME_EN, ITEM_SPECIFIC_NAME_EN) are in 'r' directly
                    return mapRecordToOrderItemDTO(soiRecord, r);
                });

            return Optional.of(mapRecordToOrderDTO(orderRecord, patientAndUserRecord, items));
        } catch (DataAccessException e) {
            logger.error("Error finding sales order by ID {}: {}", salesOrderId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public List<SalesOrderDTO> findAllOrderSummaries(LocalDate fromDate, LocalDate toDate, String statusFilter, String patientQuery) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            Condition condition = DSL.trueCondition();
            if (fromDate != null) {
                condition = condition.and(SALESORDERS.ORDER_DATE.ge(OffsetDateTime.of(fromDate, LocalTime.MIN, ZoneOffset.UTC)));
            }
            if (toDate != null) {
                condition = condition.and(SALESORDERS.ORDER_DATE.le(OffsetDateTime.of(toDate, LocalTime.MAX, ZoneOffset.UTC)));
            }
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                condition = condition.and(SALESORDERS.STATUS.eq(statusFilter));
            }
            if (patientQuery != null && !patientQuery.trim().isEmpty()) {
                String likeQuery = "%" + patientQuery.toLowerCase() + "%";
                condition = condition.and(
                    DSL.lower(PATIENTS.FULL_NAME_EN).like(likeQuery)
                    .or(DSL.lower(PATIENTS.PATIENT_SYSTEM_ID).like(likeQuery))
                );
            }

            return dsl.select(SALESORDERS.fields())
                      .select(
                          PATIENTS.PATIENT_SYSTEM_ID,
                          PATIENTS.FULL_NAME_EN,
                          PATIENTS.PHONE_NUMBER,      // Added
                          PATIENTS.WHATSAPP_OPT_IN,   // Added
                          USERS.FULL_NAME
                      )
                      .from(SALESORDERS)
                      .leftOuterJoin(PATIENTS).on(SALESORDERS.PATIENT_ID.eq(PATIENTS.PATIENT_ID))
                      .join(USERS).on(SALESORDERS.CREATED_BY_USER_ID.eq(USERS.USER_ID))
                      .where(condition)
                      .orderBy(SALESORDERS.ORDER_DATE.desc())
                      .fetch(this::mapRecordToOrderSummaryDTO);
        } catch (DataAccessException e) {
            logger.error("Error finding all sales order summaries: {}", e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void updateOrderStatus(int salesOrderId, String newStatus) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            int updatedRows = dsl.update(SALESORDERS)
                                 .set(SALESORDERS.STATUS, newStatus)
                                 .where(SALESORDERS.SALES_ORDER_ID.eq(salesOrderId))
                                 .execute();
            if (updatedRows == 0) throw new DataAccessException("SalesOrder ID " + salesOrderId + " not found for status update.");
            logger.info("Status for sales order ID {} updated to {}.", salesOrderId, newStatus);
        } catch (DataAccessException e) {
            logger.error("Error updating status for sales order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void updateOrderDiscount(int salesOrderId, BigDecimal discountAmount) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");
            int updatedRows = dsl.update(SALESORDERS)
                                 .set(SALESORDERS.DISCOUNT_AMOUNT, discountAmount)
                                 .where(SALESORDERS.SALES_ORDER_ID.eq(salesOrderId))
                                 .execute();
            if (updatedRows == 0) throw new DataAccessException("SalesOrder ID " + salesOrderId + " not found for discount update.");
            logger.info("Discount for sales order ID {} updated to {}.", salesOrderId, discountAmount);
            // After discount update, totals should be recalculated
            callRecalculateSalesOrderSubtotalProcedure(salesOrderId);
        } catch (DataAccessException e) {
            logger.error("Error updating discount for sales order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw e;
        } finally {
            closeContext(dsl);
        }
    }

    @Override
    public void callRecalculateSalesOrderSubtotalProcedure(int salesOrderId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) throw new DataAccessException("DSLContext not available");

            recalculatesalesordersubtotal(dsl.configuration(), salesOrderId);

            logger.info("Called RecalculateSalesOrderSubtotal procedure for sales order ID {}.", salesOrderId);
        } catch (DataAccessException e) {
            logger.error("Error calling RecalculateSalesOrderSubtotal procedure for sales order ID {}: {}", salesOrderId, e.getMessage(), e);
            throw e; // Or a custom exception
        } finally {
            closeContext(dsl);
        }
    }

    private void closeContext(DSLContext dslContext) {
        if (dslContext != null) {
            try {
                // dslContext.close(); // Managed by DBManager or connection pool typically
            } catch (Exception e) {
                logger.warn("Error closing DSLContext (or connection not managed here).", e);
            }
        }
    }
}
