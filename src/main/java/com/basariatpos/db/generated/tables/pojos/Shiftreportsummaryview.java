/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.pojos;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Shiftreportsummaryview implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer shiftId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String startedByUsername;
    private String startedByFullname;
    private String shiftStatus;
    private BigDecimal openingFloat;
    private BigDecimal closingCashCounted;
    private BigDecimal expectedCashInDrawer;
    private BigDecimal cashVariance;
    private String shiftNotes;
    private BigDecimal totalCashReceivedInShift;
    private BigDecimal totalBankReceivedInShift;
    private BigDecimal totalCashExpensesPaidFromShift;
    private Long totalSalesOrdersInShift;
    private BigDecimal totalDiscountsInShift;

    public Shiftreportsummaryview() {}

    public Shiftreportsummaryview(Shiftreportsummaryview value) {
        this.shiftId = value.shiftId;
        this.startTime = value.startTime;
        this.endTime = value.endTime;
        this.startedByUsername = value.startedByUsername;
        this.startedByFullname = value.startedByFullname;
        this.shiftStatus = value.shiftStatus;
        this.openingFloat = value.openingFloat;
        this.closingCashCounted = value.closingCashCounted;
        this.expectedCashInDrawer = value.expectedCashInDrawer;
        this.cashVariance = value.cashVariance;
        this.shiftNotes = value.shiftNotes;
        this.totalCashReceivedInShift = value.totalCashReceivedInShift;
        this.totalBankReceivedInShift = value.totalBankReceivedInShift;
        this.totalCashExpensesPaidFromShift = value.totalCashExpensesPaidFromShift;
        this.totalSalesOrdersInShift = value.totalSalesOrdersInShift;
        this.totalDiscountsInShift = value.totalDiscountsInShift;
    }

    public Shiftreportsummaryview(
        Integer shiftId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String startedByUsername,
        String startedByFullname,
        String shiftStatus,
        BigDecimal openingFloat,
        BigDecimal closingCashCounted,
        BigDecimal expectedCashInDrawer,
        BigDecimal cashVariance,
        String shiftNotes,
        BigDecimal totalCashReceivedInShift,
        BigDecimal totalBankReceivedInShift,
        BigDecimal totalCashExpensesPaidFromShift,
        Long totalSalesOrdersInShift,
        BigDecimal totalDiscountsInShift
    ) {
        this.shiftId = shiftId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startedByUsername = startedByUsername;
        this.startedByFullname = startedByFullname;
        this.shiftStatus = shiftStatus;
        this.openingFloat = openingFloat;
        this.closingCashCounted = closingCashCounted;
        this.expectedCashInDrawer = expectedCashInDrawer;
        this.cashVariance = cashVariance;
        this.shiftNotes = shiftNotes;
        this.totalCashReceivedInShift = totalCashReceivedInShift;
        this.totalBankReceivedInShift = totalBankReceivedInShift;
        this.totalCashExpensesPaidFromShift = totalCashExpensesPaidFromShift;
        this.totalSalesOrdersInShift = totalSalesOrdersInShift;
        this.totalDiscountsInShift = totalDiscountsInShift;
    }

    /**
     * Getter for <code>public.shiftreportsummaryview.shift_id</code>.
     */
    public Integer getShiftId() {
        return this.shiftId;
    }

    /**
     * Setter for <code>public.shiftreportsummaryview.shift_id</code>.
     */
    public void setShiftId(Integer shiftId) {
        this.shiftId = shiftId;
    }

    /**
     * Getter for <code>public.shiftreportsummaryview.start_time</code>.
     */
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Setter for <code>public.shiftreportsummaryview.start_time</code>.
     */
    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Getter for <code>public.shiftreportsummaryview.end_time</code>.
     */
    public OffsetDateTime getEndTime() {
        return this.endTime;
    }

    /**
     * Setter for <code>public.shiftreportsummaryview.end_time</code>.
     */
    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Getter for
     * <code>public.shiftreportsummaryview.started_by_username</code>.
     */
    public String getStartedByUsername() {
        return this.startedByUsername;
    }

    /**
     * Setter for
     * <code>public.shiftreportsummaryview.started_by_username</code>.
     */
    public void setStartedByUsername(String startedByUsername) {
        this.startedByUsername = startedByUsername;
    }

    /**
     * Getter for
     * <code>public.shiftreportsummaryview.started_by_fullname</code>.
     */
    public String getStartedByFullname() {
        return this.startedByFullname;
    }

    /**
     * Setter for
     * <code>public.shiftreportsummaryview.started_by_fullname</code>.
     */
    public void setStartedByFullname(String startedByFullname) {
        this.startedByFullname = startedByFullname;
    }

    /**
     * Getter for <code>public.shiftreportsummaryview.shift_status</code>.
     */
    public String getShiftStatus() {
        return this.shiftStatus;
    }

    /**
     * Setter for <code>public.shiftreportsummaryview.shift_status</code>.
     */
    public void setShiftStatus(String shiftStatus) {
        this.shiftStatus = shiftStatus;
    }

    /**
     * Getter for <code>public.shiftreportsummaryview.opening_float</code>.
     */
    public BigDecimal getOpeningFloat() {
        return this.openingFloat;
    }

    /**
     * Setter for <code>public.shiftreportsummaryview.opening_float</code>.
     */
    public void setOpeningFloat(BigDecimal openingFloat) {
        this.openingFloat = openingFloat;
    }

    /**
     * Getter for
     * <code>public.shiftreportsummaryview.closing_cash_counted</code>.
     */
    public BigDecimal getClosingCashCounted() {
        return this.closingCashCounted;
    }

    /**
     * Setter for
     * <code>public.shiftreportsummaryview.closing_cash_counted</code>.
     */
    public void setClosingCashCounted(BigDecimal closingCashCounted) {
        this.closingCashCounted = closingCashCounted;
    }

    /**
     * Getter for
     * <code>public.shiftreportsummaryview.expected_cash_in_drawer</code>.
     */
    public BigDecimal getExpectedCashInDrawer() {
        return this.expectedCashInDrawer;
    }

    /**
     * Setter for
     * <code>public.shiftreportsummaryview.expected_cash_in_drawer</code>.
     */
    public void setExpectedCashInDrawer(BigDecimal expectedCashInDrawer) {
        this.expectedCashInDrawer = expectedCashInDrawer;
    }

    /**
     * Getter for <code>public.shiftreportsummaryview.cash_variance</code>.
     */
    public BigDecimal getCashVariance() {
        return this.cashVariance;
    }

    /**
     * Setter for <code>public.shiftreportsummaryview.cash_variance</code>.
     */
    public void setCashVariance(BigDecimal cashVariance) {
        this.cashVariance = cashVariance;
    }

    /**
     * Getter for <code>public.shiftreportsummaryview.shift_notes</code>.
     */
    public String getShiftNotes() {
        return this.shiftNotes;
    }

    /**
     * Setter for <code>public.shiftreportsummaryview.shift_notes</code>.
     */
    public void setShiftNotes(String shiftNotes) {
        this.shiftNotes = shiftNotes;
    }

    /**
     * Getter for
     * <code>public.shiftreportsummaryview.total_cash_received_in_shift</code>.
     */
    public BigDecimal getTotalCashReceivedInShift() {
        return this.totalCashReceivedInShift;
    }

    /**
     * Setter for
     * <code>public.shiftreportsummaryview.total_cash_received_in_shift</code>.
     */
    public void setTotalCashReceivedInShift(BigDecimal totalCashReceivedInShift) {
        this.totalCashReceivedInShift = totalCashReceivedInShift;
    }

    /**
     * Getter for
     * <code>public.shiftreportsummaryview.total_bank_received_in_shift</code>.
     */
    public BigDecimal getTotalBankReceivedInShift() {
        return this.totalBankReceivedInShift;
    }

    /**
     * Setter for
     * <code>public.shiftreportsummaryview.total_bank_received_in_shift</code>.
     */
    public void setTotalBankReceivedInShift(BigDecimal totalBankReceivedInShift) {
        this.totalBankReceivedInShift = totalBankReceivedInShift;
    }

    /**
     * Getter for
     * <code>public.shiftreportsummaryview.total_cash_expenses_paid_from_shift</code>.
     */
    public BigDecimal getTotalCashExpensesPaidFromShift() {
        return this.totalCashExpensesPaidFromShift;
    }

    /**
     * Setter for
     * <code>public.shiftreportsummaryview.total_cash_expenses_paid_from_shift</code>.
     */
    public void setTotalCashExpensesPaidFromShift(BigDecimal totalCashExpensesPaidFromShift) {
        this.totalCashExpensesPaidFromShift = totalCashExpensesPaidFromShift;
    }

    /**
     * Getter for
     * <code>public.shiftreportsummaryview.total_sales_orders_in_shift</code>.
     */
    public Long getTotalSalesOrdersInShift() {
        return this.totalSalesOrdersInShift;
    }

    /**
     * Setter for
     * <code>public.shiftreportsummaryview.total_sales_orders_in_shift</code>.
     */
    public void setTotalSalesOrdersInShift(Long totalSalesOrdersInShift) {
        this.totalSalesOrdersInShift = totalSalesOrdersInShift;
    }

    /**
     * Getter for
     * <code>public.shiftreportsummaryview.total_discounts_in_shift</code>.
     */
    public BigDecimal getTotalDiscountsInShift() {
        return this.totalDiscountsInShift;
    }

    /**
     * Setter for
     * <code>public.shiftreportsummaryview.total_discounts_in_shift</code>.
     */
    public void setTotalDiscountsInShift(BigDecimal totalDiscountsInShift) {
        this.totalDiscountsInShift = totalDiscountsInShift;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Shiftreportsummaryview other = (Shiftreportsummaryview) obj;
        if (this.shiftId == null) {
            if (other.shiftId != null)
                return false;
        }
        else if (!this.shiftId.equals(other.shiftId))
            return false;
        if (this.startTime == null) {
            if (other.startTime != null)
                return false;
        }
        else if (!this.startTime.equals(other.startTime))
            return false;
        if (this.endTime == null) {
            if (other.endTime != null)
                return false;
        }
        else if (!this.endTime.equals(other.endTime))
            return false;
        if (this.startedByUsername == null) {
            if (other.startedByUsername != null)
                return false;
        }
        else if (!this.startedByUsername.equals(other.startedByUsername))
            return false;
        if (this.startedByFullname == null) {
            if (other.startedByFullname != null)
                return false;
        }
        else if (!this.startedByFullname.equals(other.startedByFullname))
            return false;
        if (this.shiftStatus == null) {
            if (other.shiftStatus != null)
                return false;
        }
        else if (!this.shiftStatus.equals(other.shiftStatus))
            return false;
        if (this.openingFloat == null) {
            if (other.openingFloat != null)
                return false;
        }
        else if (!this.openingFloat.equals(other.openingFloat))
            return false;
        if (this.closingCashCounted == null) {
            if (other.closingCashCounted != null)
                return false;
        }
        else if (!this.closingCashCounted.equals(other.closingCashCounted))
            return false;
        if (this.expectedCashInDrawer == null) {
            if (other.expectedCashInDrawer != null)
                return false;
        }
        else if (!this.expectedCashInDrawer.equals(other.expectedCashInDrawer))
            return false;
        if (this.cashVariance == null) {
            if (other.cashVariance != null)
                return false;
        }
        else if (!this.cashVariance.equals(other.cashVariance))
            return false;
        if (this.shiftNotes == null) {
            if (other.shiftNotes != null)
                return false;
        }
        else if (!this.shiftNotes.equals(other.shiftNotes))
            return false;
        if (this.totalCashReceivedInShift == null) {
            if (other.totalCashReceivedInShift != null)
                return false;
        }
        else if (!this.totalCashReceivedInShift.equals(other.totalCashReceivedInShift))
            return false;
        if (this.totalBankReceivedInShift == null) {
            if (other.totalBankReceivedInShift != null)
                return false;
        }
        else if (!this.totalBankReceivedInShift.equals(other.totalBankReceivedInShift))
            return false;
        if (this.totalCashExpensesPaidFromShift == null) {
            if (other.totalCashExpensesPaidFromShift != null)
                return false;
        }
        else if (!this.totalCashExpensesPaidFromShift.equals(other.totalCashExpensesPaidFromShift))
            return false;
        if (this.totalSalesOrdersInShift == null) {
            if (other.totalSalesOrdersInShift != null)
                return false;
        }
        else if (!this.totalSalesOrdersInShift.equals(other.totalSalesOrdersInShift))
            return false;
        if (this.totalDiscountsInShift == null) {
            if (other.totalDiscountsInShift != null)
                return false;
        }
        else if (!this.totalDiscountsInShift.equals(other.totalDiscountsInShift))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.shiftId == null) ? 0 : this.shiftId.hashCode());
        result = prime * result + ((this.startTime == null) ? 0 : this.startTime.hashCode());
        result = prime * result + ((this.endTime == null) ? 0 : this.endTime.hashCode());
        result = prime * result + ((this.startedByUsername == null) ? 0 : this.startedByUsername.hashCode());
        result = prime * result + ((this.startedByFullname == null) ? 0 : this.startedByFullname.hashCode());
        result = prime * result + ((this.shiftStatus == null) ? 0 : this.shiftStatus.hashCode());
        result = prime * result + ((this.openingFloat == null) ? 0 : this.openingFloat.hashCode());
        result = prime * result + ((this.closingCashCounted == null) ? 0 : this.closingCashCounted.hashCode());
        result = prime * result + ((this.expectedCashInDrawer == null) ? 0 : this.expectedCashInDrawer.hashCode());
        result = prime * result + ((this.cashVariance == null) ? 0 : this.cashVariance.hashCode());
        result = prime * result + ((this.shiftNotes == null) ? 0 : this.shiftNotes.hashCode());
        result = prime * result + ((this.totalCashReceivedInShift == null) ? 0 : this.totalCashReceivedInShift.hashCode());
        result = prime * result + ((this.totalBankReceivedInShift == null) ? 0 : this.totalBankReceivedInShift.hashCode());
        result = prime * result + ((this.totalCashExpensesPaidFromShift == null) ? 0 : this.totalCashExpensesPaidFromShift.hashCode());
        result = prime * result + ((this.totalSalesOrdersInShift == null) ? 0 : this.totalSalesOrdersInShift.hashCode());
        result = prime * result + ((this.totalDiscountsInShift == null) ? 0 : this.totalDiscountsInShift.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Shiftreportsummaryview (");

        sb.append(shiftId);
        sb.append(", ").append(startTime);
        sb.append(", ").append(endTime);
        sb.append(", ").append(startedByUsername);
        sb.append(", ").append(startedByFullname);
        sb.append(", ").append(shiftStatus);
        sb.append(", ").append(openingFloat);
        sb.append(", ").append(closingCashCounted);
        sb.append(", ").append(expectedCashInDrawer);
        sb.append(", ").append(cashVariance);
        sb.append(", ").append(shiftNotes);
        sb.append(", ").append(totalCashReceivedInShift);
        sb.append(", ").append(totalBankReceivedInShift);
        sb.append(", ").append(totalCashExpensesPaidFromShift);
        sb.append(", ").append(totalSalesOrdersInShift);
        sb.append(", ").append(totalDiscountsInShift);

        sb.append(")");
        return sb.toString();
    }
}
