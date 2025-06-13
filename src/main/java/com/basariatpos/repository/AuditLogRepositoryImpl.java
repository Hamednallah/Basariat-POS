package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.AuditlogRecord;
import com.basariatpos.util.AppLogger;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.JSONB; // For JSONB data type
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.basariatpos.db.generated.Tables.AUDITLOG;

public class AuditLogRepositoryImpl implements AuditLogRepository {

    private static final Logger logger = AppLogger.getLogger(AuditLogRepositoryImpl.class);

    @Override
    public void logStockAdjustment(int inventoryItemId, String itemName, int quantityChange,
                                   int oldQty, int newQty, String reason, Integer adjustedByUserId) {
        DSLContext dsl = null;
        try {
            dsl = DBManager.getDSLContext();
            if (dsl == null) {
                logger.error("DSLContext not available. Cannot log stock adjustment.");
                return; // Or throw exception
            }

            AuditlogRecord auditRecord = dsl.newRecord(AUDITLOG);
            auditRecord.setTableName("InventoryItems"); // Or a more specific event type like "STOCK_ADJUSTMENT"
            auditRecord.setRecordPk(String.valueOf(inventoryItemId)); // Assuming record_pk is VARCHAR

            // Construct JSONB for old and new values
            String oldValJson = String.format("{ \"quantityOnHand\": %d }", oldQty);
            String newValJson = String.format("{ \"quantityOnHand\": %d }", newQty);
            auditRecord.setOldValue(JSONB.valueOf(oldValJson));
            auditRecord.setNewValue(JSONB.valueOf(newValJson));

            auditRecord.setActionType("ADJUST"); // Custom action type for stock adjustments
            auditRecord.setUserId(adjustedByUserId); // User who performed the action

            String details = String.format("Item: '%s'. Change: %d. Old Qty: %d, New Qty: %d. Reason: %s",
                                           itemName, quantityChange, oldQty, newQty, reason);
            auditRecord.setDetails(details.substring(0, Math.min(details.length(), 255))); // Ensure details fit if column has limit

            auditRecord.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC)); // Timestamp of the audit log entry

            auditRecord.store(); // Inserts the audit log record

            logger.info("Stock adjustment logged for item ID {}. User ID: {}, Reason: {}",
                        inventoryItemId, adjustedByUserId, reason);

        } catch (DataAccessException e) {
            logger.error("Error logging stock adjustment for item ID {}: {}", inventoryItemId, e.getMessage(), e);
            // Depending on policy, this might throw a specific AuditLogException
        } finally {
            closeContext(dsl);
        }
    }

    private void closeContext(DSLContext dslContext) {
        if (dslContext != null) {
            try {
                dslContext.close();
            } catch (Exception e) {
                logger.warn("Failed to close DSLContext in AuditLogRepositoryImpl.", e);
            }
        }
    }
}
