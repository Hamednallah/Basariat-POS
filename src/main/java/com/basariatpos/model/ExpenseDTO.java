package com.basariatpos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

public class ExpenseDTO {
    private int expenseId;
    private LocalDate expenseDate;
    private int expenseCategoryId;
    private String categoryNameEnDisplay;
    private String categoryNameArDisplay;
    private String description;
    private BigDecimal amount;
    private String paymentMethod; // e.g., "Cash", "Bank Transaction", "Cheque"
    private Integer bankNameId; // Nullable
    private String bankNameDisplayEn; // For display
    private String bankNameDisplayAr; // For display
    private String transactionIdRef; // Nullable, for bank/card/cheque transactions
    private int createdByUserId;
    private String createdByNameDisplay; // For display
    private Integer shiftId; // Nullable, for cash expenses tied to a shift
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Default constructor
    public ExpenseDTO() {
    }

    // Getters
    public int getExpenseId() { return expenseId; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public int getExpenseCategoryId() { return expenseCategoryId; }
    public String getCategoryNameEnDisplay() { return categoryNameEnDisplay; }
    public String getCategoryNameArDisplay() { return categoryNameArDisplay; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public Integer getBankNameId() { return bankNameId; }
    public String getBankNameDisplayEn() { return bankNameDisplayEn; }
    public String getBankNameDisplayAr() { return bankNameDisplayAr; }
    public String getTransactionIdRef() { return transactionIdRef; }
    public int getCreatedByUserId() { return createdByUserId; }
    public String getCreatedByNameDisplay() { return createdByNameDisplay; }
    public Integer getShiftId() { return shiftId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public void setExpenseCategoryId(int expenseCategoryId) { this.expenseCategoryId = expenseCategoryId; }
    public void setCategoryNameEnDisplay(String categoryNameEnDisplay) { this.categoryNameEnDisplay = categoryNameEnDisplay; }
    public void setCategoryNameArDisplay(String categoryNameArDisplay) { this.categoryNameArDisplay = categoryNameArDisplay; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setBankNameId(Integer bankNameId) { this.bankNameId = bankNameId; }
    public void setBankNameDisplayEn(String bankNameDisplayEn) { this.bankNameDisplayEn = bankNameDisplayEn; }
    public void setBankNameDisplayAr(String bankNameDisplayAr) { this.bankNameDisplayAr = bankNameDisplayAr; }
    public void setTransactionIdRef(String transactionIdRef) { this.transactionIdRef = transactionIdRef; }
    public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }
    public void setCreatedByNameDisplay(String createdByNameDisplay) { this.createdByNameDisplay = createdByNameDisplay; }
    public void setShiftId(Integer shiftId) { this.shiftId = shiftId; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpenseDTO that = (ExpenseDTO) o;
        return expenseId == that.expenseId &&
               expenseCategoryId == that.expenseCategoryId &&
               createdByUserId == that.createdByUserId &&
               Objects.equals(expenseDate, that.expenseDate) &&
               Objects.equals(description, that.description) &&
               (amount != null && that.amount != null ? amount.compareTo(that.amount) == 0 : Objects.equals(amount, that.amount)) &&
               Objects.equals(paymentMethod, that.paymentMethod) &&
               Objects.equals(bankNameId, that.bankNameId) &&
               Objects.equals(transactionIdRef, that.transactionIdRef) &&
               Objects.equals(shiftId, that.shiftId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, expenseDate, expenseCategoryId, description, amount, paymentMethod,
                            bankNameId, transactionIdRef, createdByUserId, shiftId);
    }

    @Override
    public String toString() {
        return "ExpenseDTO{" +
               "expenseId=" + expenseId +
               ", expenseDate=" + expenseDate +
               ", expenseCategoryId=" + expenseCategoryId +
               ", description='" + description + '\'' +
               ", amount=" + amount +
               ", paymentMethod='" + paymentMethod + '\'' +
               ", createdByUserId=" + createdByUserId +
               ", shiftId=" + shiftId +
               '}';
    }
}
