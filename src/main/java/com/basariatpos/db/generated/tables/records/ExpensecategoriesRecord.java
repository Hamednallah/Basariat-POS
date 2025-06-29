/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.records;


import com.basariatpos.db.generated.tables.Expensecategories;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class ExpensecategoriesRecord extends UpdatableRecordImpl<ExpensecategoriesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.expensecategories.expense_category_id</code>.
     */
    public void setExpenseCategoryId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.expensecategories.expense_category_id</code>.
     */
    public Integer getExpenseCategoryId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.expensecategories.category_name_en</code>.
     */
    public void setCategoryNameEn(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.expensecategories.category_name_en</code>.
     */
    public String getCategoryNameEn() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.expensecategories.category_name_ar</code>.
     */
    public void setCategoryNameAr(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.expensecategories.category_name_ar</code>.
     */
    public String getCategoryNameAr() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.expensecategories.is_active</code>.
     */
    public void setIsActive(Boolean value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.expensecategories.is_active</code>.
     */
    public Boolean getIsActive() {
        return (Boolean) get(3);
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
     * Create a detached ExpensecategoriesRecord
     */
    public ExpensecategoriesRecord() {
        super(Expensecategories.EXPENSECATEGORIES);
    }

    /**
     * Create a detached, initialised ExpensecategoriesRecord
     */
    public ExpensecategoriesRecord(Integer expenseCategoryId, String categoryNameEn, String categoryNameAr, Boolean isActive) {
        super(Expensecategories.EXPENSECATEGORIES);

        setExpenseCategoryId(expenseCategoryId);
        setCategoryNameEn(categoryNameEn);
        setCategoryNameAr(categoryNameAr);
        setIsActive(isActive);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised ExpensecategoriesRecord
     */
    public ExpensecategoriesRecord(com.basariatpos.db.generated.tables.pojos.Expensecategories value) {
        super(Expensecategories.EXPENSECATEGORIES);

        if (value != null) {
            setExpenseCategoryId(value.getExpenseCategoryId());
            setCategoryNameEn(value.getCategoryNameEn());
            setCategoryNameAr(value.getCategoryNameAr());
            setIsActive(value.getIsActive());
            resetChangedOnNotNull();
        }
    }
}
