/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.routines;


import com.basariatpos.db.generated.Public;

import java.math.BigDecimal;

import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Recordpaymentandupdatesalesorder extends AbstractRoutine<java.lang.Void> {

    private static final long serialVersionUID = 1L;

    /**
     * The parameter
     * <code>public.recordpaymentandupdatesalesorder.p_sales_order_id</code>.
     */
    public static final Parameter<Integer> P_SALES_ORDER_ID = Internal.createParameter("p_sales_order_id", SQLDataType.INTEGER, false, false);

    /**
     * The parameter
     * <code>public.recordpaymentandupdatesalesorder.p_amount</code>.
     */
    public static final Parameter<BigDecimal> P_AMOUNT = Internal.createParameter("p_amount", SQLDataType.NUMERIC, false, false);

    /**
     * The parameter
     * <code>public.recordpaymentandupdatesalesorder.p_payment_method</code>.
     */
    public static final Parameter<String> P_PAYMENT_METHOD = Internal.createParameter("p_payment_method", SQLDataType.VARCHAR, false, false);

    /**
     * The parameter
     * <code>public.recordpaymentandupdatesalesorder.p_bank_name_id</code>.
     */
    public static final Parameter<Integer> P_BANK_NAME_ID = Internal.createParameter("p_bank_name_id", SQLDataType.INTEGER, false, false);

    /**
     * The parameter
     * <code>public.recordpaymentandupdatesalesorder.p_transaction_id</code>.
     */
    public static final Parameter<String> P_TRANSACTION_ID = Internal.createParameter("p_transaction_id", SQLDataType.VARCHAR, false, false);

    /**
     * The parameter
     * <code>public.recordpaymentandupdatesalesorder.p_received_by_user_id</code>.
     */
    public static final Parameter<Integer> P_RECEIVED_BY_USER_ID = Internal.createParameter("p_received_by_user_id", SQLDataType.INTEGER, false, false);

    /**
     * The parameter
     * <code>public.recordpaymentandupdatesalesorder.p_notes</code>.
     */
    public static final Parameter<String> P_NOTES = Internal.createParameter("p_notes", SQLDataType.CLOB, false, false);

    /**
     * Create a new routine call instance
     */
    public Recordpaymentandupdatesalesorder() {
        super("recordpaymentandupdatesalesorder", Public.PUBLIC);

        addInParameter(P_SALES_ORDER_ID);
        addInParameter(P_AMOUNT);
        addInParameter(P_PAYMENT_METHOD);
        addInParameter(P_BANK_NAME_ID);
        addInParameter(P_TRANSACTION_ID);
        addInParameter(P_RECEIVED_BY_USER_ID);
        addInParameter(P_NOTES);
        setSQLUsable(false);
    }

    /**
     * Set the <code>p_sales_order_id</code> parameter IN value to the routine
     */
    public void setPSalesOrderId(Integer value) {
        setValue(P_SALES_ORDER_ID, value);
    }

    /**
     * Set the <code>p_amount</code> parameter IN value to the routine
     */
    public void setPAmount(BigDecimal value) {
        setValue(P_AMOUNT, value);
    }

    /**
     * Set the <code>p_payment_method</code> parameter IN value to the routine
     */
    public void setPPaymentMethod(String value) {
        setValue(P_PAYMENT_METHOD, value);
    }

    /**
     * Set the <code>p_bank_name_id</code> parameter IN value to the routine
     */
    public void setPBankNameId(Integer value) {
        setValue(P_BANK_NAME_ID, value);
    }

    /**
     * Set the <code>p_transaction_id</code> parameter IN value to the routine
     */
    public void setPTransactionId(String value) {
        setValue(P_TRANSACTION_ID, value);
    }

    /**
     * Set the <code>p_received_by_user_id</code> parameter IN value to the
     * routine
     */
    public void setPReceivedByUserId(Integer value) {
        setValue(P_RECEIVED_BY_USER_ID, value);
    }

    /**
     * Set the <code>p_notes</code> parameter IN value to the routine
     */
    public void setPNotes(String value) {
        setValue(P_NOTES, value);
    }
}
