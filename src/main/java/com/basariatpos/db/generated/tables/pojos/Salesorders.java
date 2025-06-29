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
public class Salesorders implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer salesOrderId;
    private Integer patientId;
    private OffsetDateTime orderDate;
    private String status;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private Integer createdByUserId;
    private Integer shiftId;
    private Integer deliveryAppointmentId;
    private String remarks;
    private OffsetDateTime updatedAt;

    public Salesorders() {}

    public Salesorders(Salesorders value) {
        this.salesOrderId = value.salesOrderId;
        this.patientId = value.patientId;
        this.orderDate = value.orderDate;
        this.status = value.status;
        this.subtotalAmount = value.subtotalAmount;
        this.discountAmount = value.discountAmount;
        this.totalAmount = value.totalAmount;
        this.amountPaid = value.amountPaid;
        this.balanceDue = value.balanceDue;
        this.createdByUserId = value.createdByUserId;
        this.shiftId = value.shiftId;
        this.deliveryAppointmentId = value.deliveryAppointmentId;
        this.remarks = value.remarks;
        this.updatedAt = value.updatedAt;
    }

    public Salesorders(
        Integer salesOrderId,
        Integer patientId,
        OffsetDateTime orderDate,
        String status,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal balanceDue,
        Integer createdByUserId,
        Integer shiftId,
        Integer deliveryAppointmentId,
        String remarks,
        OffsetDateTime updatedAt
    ) {
        this.salesOrderId = salesOrderId;
        this.patientId = patientId;
        this.orderDate = orderDate;
        this.status = status;
        this.subtotalAmount = subtotalAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.balanceDue = balanceDue;
        this.createdByUserId = createdByUserId;
        this.shiftId = shiftId;
        this.deliveryAppointmentId = deliveryAppointmentId;
        this.remarks = remarks;
        this.updatedAt = updatedAt;
    }

    /**
     * Getter for <code>public.salesorders.sales_order_id</code>.
     */
    public Integer getSalesOrderId() {
        return this.salesOrderId;
    }

    /**
     * Setter for <code>public.salesorders.sales_order_id</code>.
     */
    public void setSalesOrderId(Integer salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    /**
     * Getter for <code>public.salesorders.patient_id</code>.
     */
    public Integer getPatientId() {
        return this.patientId;
    }

    /**
     * Setter for <code>public.salesorders.patient_id</code>.
     */
    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    /**
     * Getter for <code>public.salesorders.order_date</code>.
     */
    public OffsetDateTime getOrderDate() {
        return this.orderDate;
    }

    /**
     * Setter for <code>public.salesorders.order_date</code>.
     */
    public void setOrderDate(OffsetDateTime orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * Getter for <code>public.salesorders.status</code>.
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Setter for <code>public.salesorders.status</code>.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter for <code>public.salesorders.subtotal_amount</code>.
     */
    public BigDecimal getSubtotalAmount() {
        return this.subtotalAmount;
    }

    /**
     * Setter for <code>public.salesorders.subtotal_amount</code>.
     */
    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    /**
     * Getter for <code>public.salesorders.discount_amount</code>.
     */
    public BigDecimal getDiscountAmount() {
        return this.discountAmount;
    }

    /**
     * Setter for <code>public.salesorders.discount_amount</code>.
     */
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Getter for <code>public.salesorders.total_amount</code>.
     */
    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    /**
     * Setter for <code>public.salesorders.total_amount</code>.
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Getter for <code>public.salesorders.amount_paid</code>.
     */
    public BigDecimal getAmountPaid() {
        return this.amountPaid;
    }

    /**
     * Setter for <code>public.salesorders.amount_paid</code>.
     */
    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    /**
     * Getter for <code>public.salesorders.balance_due</code>.
     */
    public BigDecimal getBalanceDue() {
        return this.balanceDue;
    }

    /**
     * Setter for <code>public.salesorders.balance_due</code>.
     */
    public void setBalanceDue(BigDecimal balanceDue) {
        this.balanceDue = balanceDue;
    }

    /**
     * Getter for <code>public.salesorders.created_by_user_id</code>.
     */
    public Integer getCreatedByUserId() {
        return this.createdByUserId;
    }

    /**
     * Setter for <code>public.salesorders.created_by_user_id</code>.
     */
    public void setCreatedByUserId(Integer createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    /**
     * Getter for <code>public.salesorders.shift_id</code>.
     */
    public Integer getShiftId() {
        return this.shiftId;
    }

    /**
     * Setter for <code>public.salesorders.shift_id</code>.
     */
    public void setShiftId(Integer shiftId) {
        this.shiftId = shiftId;
    }

    /**
     * Getter for <code>public.salesorders.delivery_appointment_id</code>.
     */
    public Integer getDeliveryAppointmentId() {
        return this.deliveryAppointmentId;
    }

    /**
     * Setter for <code>public.salesorders.delivery_appointment_id</code>.
     */
    public void setDeliveryAppointmentId(Integer deliveryAppointmentId) {
        this.deliveryAppointmentId = deliveryAppointmentId;
    }

    /**
     * Getter for <code>public.salesorders.remarks</code>.
     */
    public String getRemarks() {
        return this.remarks;
    }

    /**
     * Setter for <code>public.salesorders.remarks</code>.
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Getter for <code>public.salesorders.updated_at</code>.
     */
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    /**
     * Setter for <code>public.salesorders.updated_at</code>.
     */
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Salesorders other = (Salesorders) obj;
        if (this.salesOrderId == null) {
            if (other.salesOrderId != null)
                return false;
        }
        else if (!this.salesOrderId.equals(other.salesOrderId))
            return false;
        if (this.patientId == null) {
            if (other.patientId != null)
                return false;
        }
        else if (!this.patientId.equals(other.patientId))
            return false;
        if (this.orderDate == null) {
            if (other.orderDate != null)
                return false;
        }
        else if (!this.orderDate.equals(other.orderDate))
            return false;
        if (this.status == null) {
            if (other.status != null)
                return false;
        }
        else if (!this.status.equals(other.status))
            return false;
        if (this.subtotalAmount == null) {
            if (other.subtotalAmount != null)
                return false;
        }
        else if (!this.subtotalAmount.equals(other.subtotalAmount))
            return false;
        if (this.discountAmount == null) {
            if (other.discountAmount != null)
                return false;
        }
        else if (!this.discountAmount.equals(other.discountAmount))
            return false;
        if (this.totalAmount == null) {
            if (other.totalAmount != null)
                return false;
        }
        else if (!this.totalAmount.equals(other.totalAmount))
            return false;
        if (this.amountPaid == null) {
            if (other.amountPaid != null)
                return false;
        }
        else if (!this.amountPaid.equals(other.amountPaid))
            return false;
        if (this.balanceDue == null) {
            if (other.balanceDue != null)
                return false;
        }
        else if (!this.balanceDue.equals(other.balanceDue))
            return false;
        if (this.createdByUserId == null) {
            if (other.createdByUserId != null)
                return false;
        }
        else if (!this.createdByUserId.equals(other.createdByUserId))
            return false;
        if (this.shiftId == null) {
            if (other.shiftId != null)
                return false;
        }
        else if (!this.shiftId.equals(other.shiftId))
            return false;
        if (this.deliveryAppointmentId == null) {
            if (other.deliveryAppointmentId != null)
                return false;
        }
        else if (!this.deliveryAppointmentId.equals(other.deliveryAppointmentId))
            return false;
        if (this.remarks == null) {
            if (other.remarks != null)
                return false;
        }
        else if (!this.remarks.equals(other.remarks))
            return false;
        if (this.updatedAt == null) {
            if (other.updatedAt != null)
                return false;
        }
        else if (!this.updatedAt.equals(other.updatedAt))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.salesOrderId == null) ? 0 : this.salesOrderId.hashCode());
        result = prime * result + ((this.patientId == null) ? 0 : this.patientId.hashCode());
        result = prime * result + ((this.orderDate == null) ? 0 : this.orderDate.hashCode());
        result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
        result = prime * result + ((this.subtotalAmount == null) ? 0 : this.subtotalAmount.hashCode());
        result = prime * result + ((this.discountAmount == null) ? 0 : this.discountAmount.hashCode());
        result = prime * result + ((this.totalAmount == null) ? 0 : this.totalAmount.hashCode());
        result = prime * result + ((this.amountPaid == null) ? 0 : this.amountPaid.hashCode());
        result = prime * result + ((this.balanceDue == null) ? 0 : this.balanceDue.hashCode());
        result = prime * result + ((this.createdByUserId == null) ? 0 : this.createdByUserId.hashCode());
        result = prime * result + ((this.shiftId == null) ? 0 : this.shiftId.hashCode());
        result = prime * result + ((this.deliveryAppointmentId == null) ? 0 : this.deliveryAppointmentId.hashCode());
        result = prime * result + ((this.remarks == null) ? 0 : this.remarks.hashCode());
        result = prime * result + ((this.updatedAt == null) ? 0 : this.updatedAt.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Salesorders (");

        sb.append(salesOrderId);
        sb.append(", ").append(patientId);
        sb.append(", ").append(orderDate);
        sb.append(", ").append(status);
        sb.append(", ").append(subtotalAmount);
        sb.append(", ").append(discountAmount);
        sb.append(", ").append(totalAmount);
        sb.append(", ").append(amountPaid);
        sb.append(", ").append(balanceDue);
        sb.append(", ").append(createdByUserId);
        sb.append(", ").append(shiftId);
        sb.append(", ").append(deliveryAppointmentId);
        sb.append(", ").append(remarks);
        sb.append(", ").append(updatedAt);

        sb.append(")");
        return sb.toString();
    }
}
