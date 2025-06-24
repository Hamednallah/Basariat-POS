package com.basariatpos.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderDTO {
    private int salesOrderId; // 0 for new
    private Integer patientId; // Nullable
    private String patientSystemId; // For display
    private String patientFullName; // For display
    private OffsetDateTime orderDate;
    private String status; // E.g., "Pending", "Completed", "Cancelled", "Abandoned"
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private int createdByUserId;
    private String createdByName; // For display
    private int shiftId;
    private String remarks;
    private List<SalesOrderItemDTO> items;
    private String patientPhoneNumber; // Added
    private boolean patientWhatsappOptIn; // Added

    // Constructors
    public SalesOrderDTO() {
        this.items = new ArrayList<>();
        this.orderDate = OffsetDateTime.now(); // Default to current time
        this.status = "Pending"; // Default status
        this.subtotalAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.amountPaid = BigDecimal.ZERO;
        this.balanceDue = BigDecimal.ZERO;
    }

    // Getters
    public int getSalesOrderId() {
        return salesOrderId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public String getPatientSystemId() {
        return patientSystemId;
    }

    public String getPatientFullName() {
        return patientFullName;
    }

    public OffsetDateTime getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getBalanceDue() {
        return balanceDue;
    }

    public int getCreatedByUserId() {
        return createdByUserId;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public int getShiftId() {
        return shiftId;
    }

    public String getRemarks() {
        return remarks;
    }

    public List<SalesOrderItemDTO> getItems() {
        return items;
    }

    public String getPatientPhoneNumber() {
        return patientPhoneNumber;
    }

    public boolean isPatientWhatsappOptIn() {
        return patientWhatsappOptIn;
    }

    // Setters
    public void setSalesOrderId(int salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public void setPatientSystemId(String patientSystemId) {
        this.patientSystemId = patientSystemId;
    }

    public void setPatientFullName(String patientFullName) {
        this.patientFullName = patientFullName;
    }

    public void setOrderDate(OffsetDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public void setBalanceDue(BigDecimal balanceDue) {
        this.balanceDue = balanceDue;
    }

    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setItems(List<SalesOrderItemDTO> items) {
        this.items = items;
    }

    public void setPatientPhoneNumber(String patientPhoneNumber) {
        this.patientPhoneNumber = patientPhoneNumber;
    }

    public void setPatientWhatsappOptIn(boolean patientWhatsappOptIn) {
        this.patientWhatsappOptIn = patientWhatsappOptIn;
    }

    @Override
    public String toString() {
        return "SalesOrderDTO{" +
               "salesOrderId=" + salesOrderId +
               ", patientId=" + patientId +
               ", status='" + status + '\'' +
               ", totalAmount=" + totalAmount +
               ", createdByUserId=" + createdByUserId +
               ", shiftId=" + shiftId +
               ", itemsCount=" + (items != null ? items.size() : 0) +
               '}';
    }
}
