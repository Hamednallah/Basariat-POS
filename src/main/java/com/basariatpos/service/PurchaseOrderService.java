package com.basariatpos.service;

import com.basariatpos.model.PurchaseOrderDTO;
import com.basariatpos.model.PurchaseOrderItemDTO;
import com.basariatpos.service.exception.*; // Import all custom exceptions for PO

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderService {

    /**
     * Creates a new purchase order with its items.
     * @param orderDto DTO containing header and items. `createdByUserId` will be set from session.
     * @return The created PurchaseOrderDTO with generated IDs.
     * @throws PurchaseOrderValidationException if data is invalid.
     * @throws PurchaseOrderException for other errors.
     */
    PurchaseOrderDTO createNewPurchaseOrder(PurchaseOrderDTO orderDto)
        throws PurchaseOrderValidationException, PurchaseOrderException;

    /**
     * Updates the header information of an existing purchase order.
     * @param orderDto DTO with updated header data. `purchaseOrderId` must be valid.
     * @return The updated PurchaseOrderDTO.
     * @throws PurchaseOrderValidationException if data is invalid.
     * @throws PurchaseOrderNotFoundException if PO not found.
     * @throws PurchaseOrderException for other errors.
     */
    PurchaseOrderDTO updatePurchaseOrderHeader(PurchaseOrderDTO orderDto)
        throws PurchaseOrderValidationException, PurchaseOrderNotFoundException, PurchaseOrderException;

    /**
     * Adds a new item to an existing purchase order or updates an existing item on it.
     * If itemDto.getPoItemId() is 0, it's a new item.
     * @param itemDto The item to add or update. `purchaseOrderId` and `inventoryItemId` must be valid.
     * @return The saved PurchaseOrderItemDTO with its ID.
     * @throws PurchaseOrderValidationException for invalid item data.
     * @throws InventoryItemNotFoundException if the referenced inventory item doesn't exist.
     * @throws PurchaseOrderNotFoundException if the parent PO doesn't exist or is in a state that prevents modification.
     * @throws PurchaseOrderException for other errors.
     */
    PurchaseOrderItemDTO addOrUpdateItemOnOrder(PurchaseOrderItemDTO itemDto)
        throws PurchaseOrderValidationException, InventoryItemNotFoundException, PurchaseOrderNotFoundException, PurchaseOrderException;

    /**
     * Removes an item from a purchase order.
     * @param poItemId The ID of the purchase order item to remove.
     * @param purchaseOrderId The ID of the parent PO (for verification).
     * @throws PurchaseOrderNotFoundException if the PO or item is not found, or item doesn't belong to PO.
     * @throws PurchaseOrderException if PO status prevents modification or other errors.
     */
    void removeItemFromOrder(int poItemId, int purchaseOrderId)
        throws PurchaseOrderNotFoundException, PurchaseOrderException;

    /**
     * Records received stock for a specific purchase order item.
     * Updates the item's received quantity and purchase price. Triggers inventory stock update via DB.
     * @param poItemId The ID of the purchase order item.
     * @param purchaseOrderId The ID of the parent PO.
     * @param quantityJustReceived The quantity received in this specific transaction (not total).
     * @param newPurchasePricePerUnit The actual purchase price for this batch.
     * @throws StockReceivingException for errors specific to stock receiving logic (e.g., over-receiving).
     * @throws PurchaseOrderNotFoundException if PO or item not found.
     * @throws InventoryItemNotFoundException if linked inventory item is somehow missing.
     * @throws ValidationException if received quantity or price is invalid.
     * @throws PurchaseOrderException for other errors.
     */
    void receiveStockForItem(int poItemId, int purchaseOrderId, int quantityJustReceived, BigDecimal newPurchasePricePerUnit)
        throws StockReceivingException, PurchaseOrderNotFoundException, InventoryItemNotFoundException, ValidationException, PurchaseOrderException;

    /**
     * Retrieves all purchase order summaries (header info only).
     * @return List of PurchaseOrderDTOs.
     */
    List<PurchaseOrderDTO> getAllPurchaseOrderSummaries() throws PurchaseOrderException;

    /**
     * Retrieves full details of a specific purchase order, including its items.
     * @param purchaseOrderId The ID of the purchase order.
     * @return Optional of PurchaseOrderDTO.
     */
    Optional<PurchaseOrderDTO> getPurchaseOrderDetails(int purchaseOrderId) throws PurchaseOrderException;

    /**
     * Updates the status of a purchase order (e.g., to 'Cancelled', 'Completed').
     * @param purchaseOrderId The ID of the PO.
     * @param newStatus The new status string.
     * @throws PurchaseOrderNotFoundException If PO not found.
     * @throws ValidationException If status is invalid.
     * @throws PurchaseOrderException For other errors.
     */
    void updatePurchaseOrderStatus(int purchaseOrderId, String newStatus)
        throws PurchaseOrderNotFoundException, ValidationException, PurchaseOrderException;

}
