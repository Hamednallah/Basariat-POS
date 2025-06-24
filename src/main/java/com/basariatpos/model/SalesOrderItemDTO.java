package com.basariatpos.model;

import java.math.BigDecimal;

public class SalesOrderItemDTO {
    private int soItemId; // 0 for new
    private int salesOrderId;
    private Integer inventoryItemId; // Nullable if it's a service
    private Integer serviceProductId; // Nullable if it's an inventory item
    private String itemDisplayNameEn; // To store Product.productNameEn or InventoryItem.specificNameEn
    private String itemDisplaySpecificNameEn; // To store InventoryItem.specificNameEn if inventoryItemId is not null
    private String description; // Could be used for custom notes on an item
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal itemSubtotal; // For display, calculated by DB or service
    private String prescriptionDetails; // JSON or formatted string for optical prescriptions
    private boolean isCustomLenses; // Flag for custom lens orders
    private boolean isRestockedOnAbandonment = false; // Default to false

    // Constructors
    public SalesOrderItemDTO() {
    }

    // Full constructor - useful for creation
    public SalesOrderItemDTO(int soItemId, int salesOrderId, Integer inventoryItemId, Integer serviceProductId,
                             String itemDisplayNameEn, String itemDisplaySpecificNameEn, String description,
                             int quantity, BigDecimal unitPrice, BigDecimal itemSubtotal,
                             String prescriptionDetails, boolean isCustomLenses, boolean isRestockedOnAbandonment) {
        this.soItemId = soItemId;
        this.salesOrderId = salesOrderId;
        this.inventoryItemId = inventoryItemId;
        this.serviceProductId = serviceProductId;
        this.itemDisplayNameEn = itemDisplayNameEn;
        this.itemDisplaySpecificNameEn = itemDisplaySpecificNameEn;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.itemSubtotal = itemSubtotal;
        this.prescriptionDetails = prescriptionDetails;
        this.isCustomLenses = isCustomLenses;
        this.isRestockedOnAbandonment = isRestockedOnAbandonment;
    }

    // Getters
    public int getSoItemId() {
        return soItemId;
    }

    public int getSalesOrderId() {
        return salesOrderId;
    }

    public Integer getInventoryItemId() {
        return inventoryItemId;
    }

    public Integer getServiceProductId() {
        return serviceProductId;
    }

    public String getItemDisplayNameEn() {
        return itemDisplayNameEn;
    }

    public String getItemDisplaySpecificNameEn() {
        return itemDisplaySpecificNameEn;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getItemSubtotal() {
        return itemSubtotal;
    }

    public String getPrescriptionDetails() {
        return prescriptionDetails;
    }

    public boolean isCustomLenses() {
        return isCustomLenses;
    }

    public boolean isRestockedOnAbandonment() {
        return isRestockedOnAbandonment;
    }

    // Setters
    public void setSoItemId(int soItemId) {
        this.soItemId = soItemId;
    }

    public void setSalesOrderId(int salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public void setInventoryItemId(Integer inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public void setServiceProductId(Integer serviceProductId) {
        this.serviceProductId = serviceProductId;
    }

    public void setItemDisplayNameEn(String itemDisplayNameEn) {
        this.itemDisplayNameEn = itemDisplayNameEn;
    }

    public void setItemDisplaySpecificNameEn(String itemDisplaySpecificNameEn) {
        this.itemDisplaySpecificNameEn = itemDisplaySpecificNameEn;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setItemSubtotal(BigDecimal itemSubtotal) {
        this.itemSubtotal = itemSubtotal;
    }

    public void setPrescriptionDetails(String prescriptionDetails) {
        this.prescriptionDetails = prescriptionDetails;
    }

    public void setCustomLenses(boolean customLenses) {
        isCustomLenses = customLenses;
    }

    public void setRestockedOnAbandonment(boolean restockedOnAbandonment) {
        this.isRestockedOnAbandonment = restockedOnAbandonment;
    }

    @Override
    public String toString() {
        return "SalesOrderItemDTO{" +
               "soItemId=" + soItemId +
               ", salesOrderId=" + salesOrderId +
               ", inventoryItemId=" + inventoryItemId +
               ", serviceProductId=" + serviceProductId +
               ", itemDisplayNameEn='" + itemDisplayNameEn + '\'' +
               ", itemDisplaySpecificNameEn='" + itemDisplaySpecificNameEn + '\'' +
               ", quantity=" + quantity +
               ", unitPrice=" + unitPrice +
               ", itemSubtotal=" + itemSubtotal +
               '}';
    }
}
