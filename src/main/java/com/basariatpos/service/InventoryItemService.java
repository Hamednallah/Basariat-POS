package com.basariatpos.service;

import com.basariatpos.model.InventoryItemDTO;
import com.basariatpos.service.exception.InventoryItemNotFoundException;
import com.basariatpos.service.exception.InventoryItemServiceException;
import com.basariatpos.service.exception.InventoryItemValidationException;
import com.basariatpos.service.exception.ProductNotFoundException; // From ProductService

import java.util.List;
import java.util.Optional;

public interface InventoryItemService {

    /**
     * Saves (creates or updates) an inventory item.
     * Validates DTO, ensures productId refers to an existing product.
     * @param itemDto The DTO containing inventory item details.
     * @return The saved InventoryItemDTO, updated with ID if new.
     * @throws InventoryItemValidationException if item data is invalid.
     * @throws ProductNotFoundException if the productId in itemDto does not refer to an existing product.
     * @throws InventoryItemServiceException for other service or repository level errors.
     */
    InventoryItemDTO saveInventoryItem(InventoryItemDTO itemDto)
        throws InventoryItemValidationException, ProductNotFoundException, InventoryItemServiceException;

    /**
     * Toggles the active status of an inventory item.
     * @param inventoryItemId The ID of the inventory item to toggle.
     * @throws InventoryItemNotFoundException if the item is not found.
     * @throws InventoryItemServiceException for other errors.
     */
    void toggleActiveStatus(int inventoryItemId)
        throws InventoryItemNotFoundException, InventoryItemServiceException;

    /**
     * Retrieves an inventory item by its ID.
     * @param id The ID of the inventory item.
     * @return Optional of InventoryItemDTO.
     */
    Optional<InventoryItemDTO> getInventoryItemById(int id) throws InventoryItemServiceException;

    /**
     * Retrieves all inventory items for a specific product.
     * @param productId The ID of the product.
     * @param includeInactive true to include inactive items.
     * @return List of InventoryItemDTOs.
     */
    List<InventoryItemDTO> getInventoryItemsByProduct(int productId, boolean includeInactive) throws InventoryItemServiceException;

    /**
     * Retrieves all inventory items.
     * @param includeInactive true to include inactive items.
     * @return List of all InventoryItemDTOs.
     */
    List<InventoryItemDTO> getAllInventoryItems(boolean includeInactive) throws InventoryItemServiceException;

    /**
     * Searches for inventory items based on a query string (product name, item specific name, brand).
     * @param query The search query.
     * @param includeInactive true to include inactive items.
     * @return List of matching InventoryItemDTOs.
     */
    List<InventoryItemDTO> searchInventoryItems(String query, boolean includeInactive) throws InventoryItemServiceException;

    /**
     * Retrieves a list of inventory items that are at or below their minimum stock level.
     * @return List of low stock InventoryItemDTOs.
     * @throws InventoryItemServiceException for service level errors.
     */
    List<InventoryItemDTO> getLowStockItemsReport() throws InventoryItemServiceException;

    // Stock adjustment methods will be added in Sprint 2, Step 3 (Inventory Adjustments)

    /**
     * Performs a manual stock adjustment for an inventory item.
     * Logs the adjustment to the audit log.
     * @param inventoryItemId The ID of the item to adjust.
     * @param quantityChange The change in quantity (positive for increase, negative for decrease).
     * @param reason The reason for the adjustment.
     * @throws InventoryItemNotFoundException if the item is not found.
     * @throws InventoryItemValidationException if the reason is blank or the adjustment results in a negative stock quantity.
     * @throws InventoryItemServiceException for other errors.
     */
    void performStockAdjustment(int inventoryItemId, int quantityChange, String reason)
        throws InventoryItemNotFoundException, InventoryItemValidationException, InventoryItemServiceException;
}
