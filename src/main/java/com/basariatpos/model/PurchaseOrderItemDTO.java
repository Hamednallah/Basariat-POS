package com.basariatpos.model;

import java.math.BigDecimal;
import java.util.Objects;

public class PurchaseOrderItemDTO {
    private int poItemId;
    private int purchaseOrderId; // Foreign key to PurchaseOrderDTO
    private int inventoryItemId;

    // Display fields, not directly in PurchaseOrderItems table but joined for convenience
    private String inventoryItemProductCode; // e.g., from Products table via InventoryItem
    private String inventoryItemProductNameEn;
    private String inventoryItemSpecificNameEn;
    private String inventoryItemUnitOfMeasure;

    private int quantityOrdered;
    private int quantityReceived;
    private BigDecimal purchasePricePerUnit; // Cost price at the time of this PO item
    private BigDecimal subtotal; // Calculated: quantityOrdered * purchasePricePerUnit (can be from DB view or calculated)

    // Default constructor
    public PurchaseOrderItemDTO() {}

    // Getters
    public int getPoItemId() { return poItemId; }
    public int getPurchaseOrderId() { return purchaseOrderId; }
    public int getInventoryItemId() { return inventoryItemId; }
    public String getInventoryItemProductCode() { return inventoryItemProductCode; }
    public String getInventoryItemProductNameEn() { return inventoryItemProductNameEn; }
    public String getInventoryItemSpecificNameEn() { return inventoryItemSpecificNameEn; }
    public String getInventoryItemUnitOfMeasure() { return inventoryItemUnitOfMeasure; }
    public int getQuantityOrdered() { return quantityOrdered; }
    public int getQuantityReceived() { return quantityReceived; }
    public BigDecimal getPurchasePricePerUnit() { return purchasePricePerUnit; }
    public BigDecimal getSubtotal() {
        if (purchasePricePerUnit != null && quantityOrdered > 0) {
            return purchasePricePerUnit.multiply(new BigDecimal(quantityOrdered));
        }
        return subtotal; // Could be set from DB if it's a calculated field there
    }

    // Setters
    public void setPoItemId(int poItemId) { this.poItemId = poItemId; }
    public void setPurchaseOrderId(int purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public void setInventoryItemId(int inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public void setInventoryItemProductCode(String code) { this.inventoryItemProductCode = code; }
    public void setInventoryItemProductNameEn(String name) { this.inventoryItemProductNameEn = name; }
    public void setInventoryItemSpecificNameEn(String name) { this.inventoryItemSpecificNameEn = name; }
    public void setInventoryItemUnitOfMeasure(String unit) { this.inventoryItemUnitOfMeasure = unit; }
    public void setQuantityOrdered(int quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    public void setQuantityReceived(int quantityReceived) { this.quantityReceived = quantityReceived; }
    public void setPurchasePricePerUnit(BigDecimal purchasePricePerUnit) { this.purchasePricePerUnit = purchasePricePerUnit; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; } // If set from DB

    @Override
    public String toString() {
        return "PurchaseOrderItemDTO{" +
               "poItemId=" + poItemId +
               ", purchaseOrderId=" + purchaseOrderId +
               ", inventoryItemId=" + inventoryItemId +
               ", inventoryItemNameEn='" + getInventoryItemDisplayFullName() + '\'' +
               ", quantityOrdered=" + quantityOrdered +
               ", quantityReceived=" + quantityReceived +
               ", purchasePricePerUnit=" + purchasePricePerUnit +
               '}';
    }

    public String getInventoryItemDisplayFullName() {
        StringBuilder sb = new StringBuilder();
        if (inventoryItemProductNameEn != null) sb.append(inventoryItemProductNameEn);
        if (inventoryItemSpecificNameEn != null && !inventoryItemSpecificNameEn.isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(inventoryItemSpecificNameEn);
        }
        if (inventoryItemProductCode != null && !inventoryItemProductCode.isEmpty()){
            if(sb.length() > 0) sb.append(" ");
            sb.append("(").append(inventoryItemProductCode).append(")");
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseOrderItemDTO that = (PurchaseOrderItemDTO) o;
        return poItemId == that.poItemId &&
               purchaseOrderId == that.purchaseOrderId &&
               inventoryItemId == that.inventoryItemId &&
               quantityOrdered == that.quantityOrdered &&
               quantityReceived == that.quantityReceived &&
               Objects.equals(purchasePricePerUnit, that.purchasePricePerUnit);
        // Display fields are not part of core equality
    }

    @Override
    public int hashCode() {
        return Objects.hash(poItemId, purchaseOrderId, inventoryItemId, quantityOrdered, quantityReceived, purchasePricePerUnit);
    }
}
