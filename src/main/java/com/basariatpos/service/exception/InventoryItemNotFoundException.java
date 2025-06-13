package com.basariatpos.service.exception;

public class InventoryItemNotFoundException extends InventoryItemServiceException {
    public InventoryItemNotFoundException(String message) {
        super(message);
    }

    public InventoryItemNotFoundException(int inventoryItemId) {
        super("Inventory item with ID " + inventoryItemId + " not found.");
    }
}
