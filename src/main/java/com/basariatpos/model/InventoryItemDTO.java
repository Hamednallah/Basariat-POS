package com.basariatpos.model;

import java.math.BigDecimal;
import java.util.Objects;

public class InventoryItemDTO {
    private int inventoryItemId;
    private int productId;
    private String productNameEn; // For display, joined from Products
    private String productNameAr; // For display, joined from Products
    private String brandName;       // Optional
    private String itemSpecificNameEn; // e.g., "Red, Large", "500ml Bottle"
    private String itemSpecificNameAr; // Arabic version of specific name
    private String attributes;      // JSON string for other attributes (e.g., {"color": "Red", "size": "Large"})
    private int quantityOnHand;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;       // Optional, based on permissions later
    private int minStockLevel;
    private String unitOfMeasure;   // e.g., "pcs", "box", "bottle"
    private boolean isActive;

    // Default constructor
    public InventoryItemDTO() {
        this.isActive = true; // Default to active
        this.quantityOnHand = 0;
        this.minStockLevel = 0;
    }

    // Getters
    public int getInventoryItemId() { return inventoryItemId; }
    public int getProductId() { return productId; }
    public String getProductNameEn() { return productNameEn; }
    public String getProductNameAr() { return productNameAr; }
    public String getBrandName() { return brandName; }
    public String getItemSpecificNameEn() { return itemSpecificNameEn; }
    public String getItemSpecificNameAr() { return itemSpecificNameAr; }
    public String getAttributes() { return attributes; }
    public int getQuantityOnHand() { return quantityOnHand; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public int getMinStockLevel() { return minStockLevel; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setInventoryItemId(int inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setProductNameEn(String productNameEn) { this.productNameEn = productNameEn; }
    public void setProductNameAr(String productNameAr) { this.productNameAr = productNameAr; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public void setItemSpecificNameEn(String itemSpecificNameEn) { this.itemSpecificNameEn = itemSpecificNameEn; }
    public void setItemSpecificNameAr(String itemSpecificNameAr) { this.itemSpecificNameAr = itemSpecificNameAr; }
    public void setAttributes(String attributes) { this.attributes = attributes; }
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public void setActive(boolean active) { isActive = active; }

    /**
     * Provides a display name combining product name and specific item name.
     * Useful for ComboBoxes or list views where a concise representation is needed.
     */
    public String getDisplayFullNameEn() {
        StringBuilder sb = new StringBuilder();
        if (productNameEn != null && !productNameEn.isEmpty()) {
            sb.append(productNameEn);
        } else {
            sb.append("Product ID: ").append(productId); // Fallback
        }
        if (itemSpecificNameEn != null && !itemSpecificNameEn.isEmpty()) {
            sb.append(" - ").append(itemSpecificNameEn);
        }
        if (brandName != null && !brandName.isEmpty()) {
            sb.append(" (").append(brandName).append(")");
        }
        return sb.toString();
    }


    @Override
    public String toString() {
        return "InventoryItemDTO{" +
               "inventoryItemId=" + inventoryItemId +
               ", productId=" + productId +
               ", productNameEn='" + productNameEn + '\'' +
               ", itemSpecificNameEn='" + itemSpecificNameEn + '\'' +
               ", quantityOnHand=" + quantityOnHand +
               ", sellingPrice=" + sellingPrice +
               ", isActive=" + isActive +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItemDTO that = (InventoryItemDTO) o;
        return inventoryItemId == that.inventoryItemId &&
               productId == that.productId &&
               quantityOnHand == that.quantityOnHand &&
               minStockLevel == that.minStockLevel &&
               isActive == that.isActive &&
               Objects.equals(productNameEn, that.productNameEn) &&
               Objects.equals(productNameAr, that.productNameAr) &&
               Objects.equals(brandName, that.brandName) &&
               Objects.equals(itemSpecificNameEn, that.itemSpecificNameEn) &&
               Objects.equals(itemSpecificNameAr, that.itemSpecificNameAr) &&
               Objects.equals(attributes, that.attributes) &&
               Objects.equals(sellingPrice, that.sellingPrice) &&
               Objects.equals(costPrice, that.costPrice) &&
               Objects.equals(unitOfMeasure, that.unitOfMeasure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inventoryItemId, productId, productNameEn, productNameAr, brandName,
                            itemSpecificNameEn, itemSpecificNameAr, attributes, quantityOnHand,
                            sellingPrice, costPrice, minStockLevel, unitOfMeasure, isActive);
    }
}
