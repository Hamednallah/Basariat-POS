package com.basariatpos.repository;

import com.basariatpos.model.InventoryItemDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository {

    /**
     * Finds an inventory item by its auto-incremented primary ID.
     * Includes product names by joining with Products table.
     * @param id The inventory_item_id.
     * @return Optional of InventoryItemDTO.
     */
    Optional<InventoryItemDTO> findById(int id);

    /**
     * Finds all inventory items for a specific product ID.
     * Includes product names.
     * @param productId The ID of the product.
     * @param includeInactive true to include inactive items, false for active only.
     * @return List of InventoryItemDTOs.
     */
    List<InventoryItemDTO> findByProductId(int productId, boolean includeInactive);

    /**
     * Retrieves all inventory items. Includes product names.
     * Consider pagination for large datasets.
     * @param includeInactive true to include inactive items, false for active only.
     * @return List of all InventoryItemDTOs.
     */
    List<InventoryItemDTO> findAll(boolean includeInactive);

    /**
     * Searches for inventory items by various criteria (product name, item specific name, brand).
     * Includes product names.
     * @param query The search query string.
     * @param includeInactive true to include inactive items.
     * @return List of matching InventoryItemDTOs.
     */
    List<InventoryItemDTO> searchItems(String query, boolean includeInactive);

    /**
     * Saves (inserts or updates) an inventory item.
     * If itemDto.getInventoryItemId() is 0, it's an insert. Otherwise, it's an update.
     * @param itemDto The InventoryItemDTO to save. productId must be valid.
     * @return The saved InventoryItemDTO, updated with a generated ID if it was an insert.
     */
    InventoryItemDTO save(InventoryItemDTO itemDto);

    /**
     * Sets the active status of an inventory item.
     * @param inventoryItemId The ID of the inventory item.
     * @param isActive true to activate, false to deactivate.
     */
    void setActiveStatus(int inventoryItemId, boolean isActive);

    /**
     * Directly updates the quantity on hand for a specific inventory item.
     * Used by stock adjustments, purchase order receiving, or sales order fulfillment (decrement).
     * @param inventoryItemId The ID of the inventory item.
     * @param newQuantityOnHand The new quantity.
     */
    void updateStockQuantity(int inventoryItemId, int newQuantityOnHand);

    /**
     * Retrieves the current quantity on hand for a specific inventory item.
     * @param inventoryItemId The ID of the inventory item.
     * @return Optional containing the quantity on hand, or empty if item not found.
     */
    Optional<Integer> getQuantityOnHand(int inventoryItemId);

    /**
     * Adjusts the stock quantity by a given amount (positive for increase, negative for decrease).
     * @param inventoryItemId The ID of the inventory item.
     * @param quantityChange The amount to adjust by.
     * @return true if the update was successful (e.g., 1 row affected), false otherwise.
     */
    boolean adjustStockQuantity(int inventoryItemId, int quantityChange);


    /**
     * Updates the cost price of a specific inventory item.
     * Typically used during purchase order receiving or manual cost price adjustments.
     * @param inventoryItemId The ID of the inventory item.
     * @param newCostPrice The new cost price.
     */
    void updateCostPrice(int inventoryItemId, BigDecimal newCostPrice);

    /**
     * Retrieves items that are at or below their minimum stock level.
     * Uses the `LowStockItemsView`.
     * @return List of InventoryItemDTOs representing low stock items.
     */
    List<InventoryItemDTO> getLowStockItems();

    /**
     * Deletes an inventory item by its ID.
     * Note: This is a hard delete. Consider if this is allowed or if items should only be deactivated.
     * Deletion might be restricted if item has transaction history.
     * @param inventoryItemId The ID of the item to delete.
     */
    // void deleteById(int inventoryItemId); // Optional, if hard delete is needed.
}
