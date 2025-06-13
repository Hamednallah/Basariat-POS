package com.basariatpos.repository;

import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository {

    /**
     * Finds a purchase order by its ID, including all its items and their details.
     * @param purchaseOrderId The ID of the purchase order.
     * @return An Optional containing the fully populated PurchaseOrderDTO if found.
     */
    Optional<PurchaseOrderDTO> findById(int purchaseOrderId);

    /**
     * Retrieves summaries of all purchase orders (e.g., without full item details for list views).
     * Details like createdByName should be populated by joining with Users table.
     * @return A list of PurchaseOrderDTOs, potentially with item list empty or null.
     */
    List<PurchaseOrderDTO> findAllSummaries();

    /**
     * Saves a new purchase order header and all its associated items in a single transaction.
     * @param orderDto The PurchaseOrderDTO containing header info and a list of PurchaseOrderItemDTOs.
     *                 The purchaseOrderId in orderDto should be 0 or null for new orders.
     *                 The poItemId in item DTOs should be 0 or null.
     * @return The saved PurchaseOrderDTO, updated with generated IDs for header and items.
     */
    PurchaseOrderDTO saveNewOrderWithItems(PurchaseOrderDTO orderDto);

    /**
     * Updates only the header fields of an existing purchase order
     * (e.g., supplier, order date, status).
     * Does not modify items.
     * @param orderDto The PurchaseOrderDTO containing the updated header information.
     *                 The purchaseOrderId must be valid.
     * @return The updated PurchaseOrderDTO (header part).
     */
    PurchaseOrderDTO updateOrderHeader(PurchaseOrderDTO orderDto);

    /**
     * Saves (inserts or updates) a single item on an existing purchase order.
     * If itemDto.getPoItemId() is 0, it's a new item for the PO. Otherwise, it's an update.
     * @param itemDto The PurchaseOrderItemDTO to save. purchaseOrderId must be set.
     * @return The saved PurchaseOrderItemDTO, updated with ID if new.
     */
    PurchaseOrderItemDTO saveOrderItem(PurchaseOrderItemDTO itemDto);

    /**
     * Deletes a specific item from a purchase order.
     * @param poItemId The ID of the purchase order item to delete.
     */
    void deleteOrderItem(int poItemId);

    /**
     * Updates the received quantity and purchase price per unit for a specific purchase order item.
     * This is typically called during stock receiving.
     * @param poItemId The ID of the purchase order item.
     * @param quantityReceived The quantity newly received in this transaction.
     * @param purchasePricePerUnit The actual purchase price per unit for this batch.
     */
    void updateOrderItemReceivedQuantityAndPrice(int poItemId, int quantityReceived, BigDecimal purchasePricePerUnit);

    /**
     * Updates the status of a purchase order.
     * @param purchaseOrderId The ID of the purchase order.
     * @param newStatus The new status (e.g., "Partial", "Received", "Cancelled").
     */
    void updateOrderStatus(int purchaseOrderId, String newStatus);
}
