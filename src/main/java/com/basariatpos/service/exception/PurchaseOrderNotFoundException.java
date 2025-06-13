package com.basariatpos.service.exception;

public class PurchaseOrderNotFoundException extends PurchaseOrderException {
    public PurchaseOrderNotFoundException(String message) {
        super(message);
    }

    public PurchaseOrderNotFoundException(int purchaseOrderId) {
        super("Purchase Order with ID " + purchaseOrderId + " not found.");
    }

    public PurchaseOrderNotFoundException(int poItemId, String itemContext) {
        super("Purchase Order Item with ID " + poItemId + " not found " + itemContext);
    }
}
