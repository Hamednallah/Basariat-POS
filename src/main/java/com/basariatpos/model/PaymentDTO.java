package com.basariatpos.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentDTO {
    private int paymentId;
    private int salesOrderId;
    private OffsetDateTime paymentDate;
    private BigDecimal amount;
    private String paymentMethod; // E.g., "Cash", "Card", "Bank Transfer", "Cheque"
    private Integer bankNameId; // Nullable
    private String bankNameDisplayEn; // For display
    private String bankNameDisplayAr; // For display
    private String transactionId; // E.g., card transaction ID, cheque number
    private int receivedByUserId;
    private String receivedByUsername; // For display
    private Integer shiftId; // Nullable, might be derived or directly stored
    private String notes;

    public PaymentDTO() {
        this.paymentDate = OffsetDateTime.now(); // Default to now
    }

    // Getters
    public int getPaymentId() { return paymentId; }
    public int getSalesOrderId() { return salesOrderId; }
    public OffsetDateTime getPaymentDate() { return paymentDate; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public Integer getBankNameId() { return bankNameId; }
    public String getBankNameDisplayEn() { return bankNameDisplayEn; }
    public String getBankNameDisplayAr() { return bankNameDisplayAr; }
    public String getTransactionId() { return transactionId; }
    public int getReceivedByUserId() { return receivedByUserId; }
    public String getReceivedByUsername() { return receivedByUsername; }
    public Integer getShiftId() { return shiftId; }
    public String getNotes() { return notes; }

    // Setters
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public void setSalesOrderId(int salesOrderId) { this.salesOrderId = salesOrderId; }
    public void setPaymentDate(OffsetDateTime paymentDate) { this.paymentDate = paymentDate; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setBankNameId(Integer bankNameId) { this.bankNameId = bankNameId; }
    public void setBankNameDisplayEn(String bankNameDisplayEn) { this.bankNameDisplayEn = bankNameDisplayEn; }
    public void setBankNameDisplayAr(String bankNameDisplayAr) { this.bankNameDisplayAr = bankNameDisplayAr; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setReceivedByUserId(int receivedByUserId) { this.receivedByUserId = receivedByUserId; }
    public void setReceivedByUsername(String receivedByUsername) { this.receivedByUsername = receivedByUsername; }
    public void setShiftId(Integer shiftId) { this.shiftId = shiftId; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "PaymentDTO{" +
               "paymentId=" + paymentId +
               ", salesOrderId=" + salesOrderId +
               ", paymentDate=" + paymentDate +
               ", amount=" + amount +
               ", paymentMethod='" + paymentMethod + '\'' +
               ", receivedByUserId=" + receivedByUserId +
               '}';
    }
}
