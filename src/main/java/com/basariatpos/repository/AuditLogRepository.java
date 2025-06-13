package com.basariatpos.repository;

public interface AuditLogRepository {

    /**
     * Logs a stock adjustment event.
     *
     * @param inventoryItemId The ID of the inventory item that was adjusted.
     * @param itemName The display name of the item (e.g., product name + specific name) for better log readability.
     * @param quantityChange The amount by which the quantity was changed (can be positive or negative).
     * @param oldQty The quantity on hand before the adjustment.
     * @param newQty The quantity on hand after the adjustment.
     * @param reason The reason provided for the stock adjustment.
     * @param adjustedByUserId The ID of the user who performed the adjustment.
     */
    void logStockAdjustment(int inventoryItemId,
                            String itemName,
                            int quantityChange,
                            int oldQty,
                            int newQty,
                            String reason,
                            Integer adjustedByUserId);

    // Other audit logging methods can be added here as needed, e.g.:
    // void logGenericEvent(String eventType, String tableName, Integer recordPk, String details, Integer userId);
}
