/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.records;


import com.basariatpos.db.generated.tables.Salesorders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class SalesordersRecord extends UpdatableRecordImpl<SalesordersRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.salesorders.sales_order_id</code>.
     */
    public void setSalesOrderId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.salesorders.sales_order_id</code>.
     */
    public Integer getSalesOrderId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.salesorders.patient_id</code>.
     */
    public void setPatientId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.salesorders.patient_id</code>.
     */
    public Integer getPatientId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.salesorders.order_date</code>.
     */
    public void setOrderDate(OffsetDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.salesorders.order_date</code>.
     */
    public OffsetDateTime getOrderDate() {
        return (OffsetDateTime) get(2);
    }

    /**
     * Setter for <code>public.salesorders.status</code>.
     */
    public void setStatus(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.salesorders.status</code>.
     */
    public String getStatus() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.salesorders.subtotal_amount</code>.
     */
    public void setSubtotalAmount(BigDecimal value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.salesorders.subtotal_amount</code>.
     */
    public BigDecimal getSubtotalAmount() {
        return (BigDecimal) get(4);
    }

    /**
     * Setter for <code>public.salesorders.discount_amount</code>.
     */
    public void setDiscountAmount(BigDecimal value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.salesorders.discount_amount</code>.
     */
    public BigDecimal getDiscountAmount() {
        return (BigDecimal) get(5);
    }

    /**
     * Setter for <code>public.salesorders.total_amount</code>.
     */
    public void setTotalAmount(BigDecimal value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.salesorders.total_amount</code>.
     */
    public BigDecimal getTotalAmount() {
        return (BigDecimal) get(6);
    }

    /**
     * Setter for <code>public.salesorders.amount_paid</code>.
     */
    public void setAmountPaid(BigDecimal value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.salesorders.amount_paid</code>.
     */
    public BigDecimal getAmountPaid() {
        return (BigDecimal) get(7);
    }

    /**
     * Setter for <code>public.salesorders.balance_due</code>.
     */
    public void setBalanceDue(BigDecimal value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.salesorders.balance_due</code>.
     */
    public BigDecimal getBalanceDue() {
        return (BigDecimal) get(8);
    }

    /**
     * Setter for <code>public.salesorders.created_by_user_id</code>.
     */
    public void setCreatedByUserId(Integer value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.salesorders.created_by_user_id</code>.
     */
    public Integer getCreatedByUserId() {
        return (Integer) get(9);
    }

    /**
     * Setter for <code>public.salesorders.shift_id</code>.
     */
    public void setShiftId(Integer value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.salesorders.shift_id</code>.
     */
    public Integer getShiftId() {
        return (Integer) get(10);
    }

    /**
     * Setter for <code>public.salesorders.delivery_appointment_id</code>.
     */
    public void setDeliveryAppointmentId(Integer value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.salesorders.delivery_appointment_id</code>.
     */
    public Integer getDeliveryAppointmentId() {
        return (Integer) get(11);
    }

    /**
     * Setter for <code>public.salesorders.remarks</code>.
     */
    public void setRemarks(String value) {
        set(12, value);
    }

    /**
     * Getter for <code>public.salesorders.remarks</code>.
     */
    public String getRemarks() {
        return (String) get(12);
    }

    /**
     * Setter for <code>public.salesorders.updated_at</code>.
     */
    public void setUpdatedAt(OffsetDateTime value) {
        set(13, value);
    }

    /**
     * Getter for <code>public.salesorders.updated_at</code>.
     */
    public OffsetDateTime getUpdatedAt() {
        return (OffsetDateTime) get(13);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached SalesordersRecord
     */
    public SalesordersRecord() {
        super(Salesorders.SALESORDERS);
    }

    /**
     * Create a detached, initialised SalesordersRecord
     */
    public SalesordersRecord(Integer salesOrderId, Integer patientId, OffsetDateTime orderDate, String status, BigDecimal subtotalAmount, BigDecimal discountAmount, BigDecimal totalAmount, BigDecimal amountPaid, BigDecimal balanceDue, Integer createdByUserId, Integer shiftId, Integer deliveryAppointmentId, String remarks, OffsetDateTime updatedAt) {
        super(Salesorders.SALESORDERS);

        setSalesOrderId(salesOrderId);
        setPatientId(patientId);
        setOrderDate(orderDate);
        setStatus(status);
        setSubtotalAmount(subtotalAmount);
        setDiscountAmount(discountAmount);
        setTotalAmount(totalAmount);
        setAmountPaid(amountPaid);
        setBalanceDue(balanceDue);
        setCreatedByUserId(createdByUserId);
        setShiftId(shiftId);
        setDeliveryAppointmentId(deliveryAppointmentId);
        setRemarks(remarks);
        setUpdatedAt(updatedAt);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised SalesordersRecord
     */
    public SalesordersRecord(com.basariatpos.db.generated.tables.pojos.Salesorders value) {
        super(Salesorders.SALESORDERS);

        if (value != null) {
            setSalesOrderId(value.getSalesOrderId());
            setPatientId(value.getPatientId());
            setOrderDate(value.getOrderDate());
            setStatus(value.getStatus());
            setSubtotalAmount(value.getSubtotalAmount());
            setDiscountAmount(value.getDiscountAmount());
            setTotalAmount(value.getTotalAmount());
            setAmountPaid(value.getAmountPaid());
            setBalanceDue(value.getBalanceDue());
            setCreatedByUserId(value.getCreatedByUserId());
            setShiftId(value.getShiftId());
            setDeliveryAppointmentId(value.getDeliveryAppointmentId());
            setRemarks(value.getRemarks());
            setUpdatedAt(value.getUpdatedAt());
            resetChangedOnNotNull();
        }
    }
}
