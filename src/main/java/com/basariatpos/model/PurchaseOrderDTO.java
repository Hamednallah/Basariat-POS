package com.basariatpos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurchaseOrderDTO {
    private int purchaseOrderId;
    private LocalDate orderDate;
    private String supplierName; // For simplicity; could be a SupplierDTO later
    private BigDecimal totalAmount; // Calculated or from DB view; sum of item subtotals
    private String status; // E.g., "Pending", "Partial", "Received", "Cancelled"
    private int createdByUserId;
    private String createdByName; // For display, joined from Users
    private OffsetDateTime createdAt;
    private List<PurchaseOrderItemDTO> items;

    // Default constructor
    public PurchaseOrderDTO() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDate.now(); // Default to today
        this.status = "Pending"; // Default status
    }

    // Constructor for creating a new PO (ID might be 0)
    public PurchaseOrderDTO(LocalDate orderDate, String supplierName, int createdByUserId, String createdByName) {
        this(); // Call default constructor
        this.orderDate = orderDate;
        this.supplierName = supplierName;
        this.createdByUserId = createdByUserId;
        this.createdByName = createdByName; // Can be set after fetching user if needed
    }

    // Full constructor
    public PurchaseOrderDTO(int purchaseOrderId, LocalDate orderDate, String supplierName,
                            BigDecimal totalAmount, String status, int createdByUserId,
                            String createdByName, OffsetDateTime createdAt, List<PurchaseOrderItemDTO> items) {
        this.purchaseOrderId = purchaseOrderId;
        this.orderDate = orderDate;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdByUserId = createdByUserId;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.items = items == null ? new ArrayList<>() : items;
    }


    // Getters
    public int getPurchaseOrderId() { return purchaseOrderId; }
    public LocalDate getOrderDate() { return orderDate; }
    public String getSupplierName() { return supplierName; }
    public BigDecimal getTotalAmount() {
        // Calculate if not set from DB, or if items have changed
        if (items != null && !items.isEmpty()) {
            BigDecimal calculatedTotal = BigDecimal.ZERO;
            for (PurchaseOrderItemDTO item : items) {
                BigDecimal itemSubtotal = item.getSubtotal(); // DTO's subtotal getter calculates if necessary
                if (itemSubtotal != null) {
                    calculatedTotal = calculatedTotal.add(itemSubtotal);
                }
            }
            return calculatedTotal;
        }
        return totalAmount; // Return stored value if items list is empty or null
    }
    public String getStatus() { return status; }
    public int getCreatedByUserId() { return createdByUserId; }
    public String getCreatedByName() { return createdByName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<PurchaseOrderItemDTO> getItems() { return items; }

    // Setters
    public void setPurchaseOrderId(int purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; } // Typically set from DB or calculated
    public void setStatus(String status) { this.status = status; }
    public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setItems(List<PurchaseOrderItemDTO> items) { this.items = items == null ? new ArrayList<>() : items; }

    // Helper to add an item
    public void addItem(PurchaseOrderItemDTO item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        item.setPurchaseOrderId(this.purchaseOrderId); // Link item to this PO
    }


    @Override
    public String toString() {
        return "PurchaseOrderDTO{" +
               "purchaseOrderId=" + purchaseOrderId +
               ", orderDate=" + orderDate +
               ", supplierName='" + supplierName + '\'' +
               ", totalAmount=" + getTotalAmount() + // Use getter to ensure calculation if needed
               ", status='" + status + '\'' +
               ", createdByName='" + createdByName + '\'' +
               ", itemsCount=" + (items != null ? items.size() : 0) +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseOrderDTO that = (PurchaseOrderDTO) o;
        return purchaseOrderId == that.purchaseOrderId &&
               createdByUserId == that.createdByUserId &&
               Objects.equals(orderDate, that.orderDate) &&
               Objects.equals(supplierName, that.supplierName) &&
               Objects.equals(getTotalAmount(), that.getTotalAmount()) && // Compare calculated/stored total
               Objects.equals(status, that.status) &&
               Objects.equals(createdByName, that.createdByName) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purchaseOrderId, orderDate, supplierName, getTotalAmount(), status,
                            createdByUserId, createdByName, createdAt, items);
    }
}
