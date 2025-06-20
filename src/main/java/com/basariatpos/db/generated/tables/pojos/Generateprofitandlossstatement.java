/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.pojos;


import java.io.Serializable;
import java.math.BigDecimal;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Generateprofitandlossstatement implements Serializable {

    private static final long serialVersionUID = 1L;

    private String categoryType;
    private String itemDescription;
    private BigDecimal amount;

    public Generateprofitandlossstatement() {}

    public Generateprofitandlossstatement(Generateprofitandlossstatement value) {
        this.categoryType = value.categoryType;
        this.itemDescription = value.itemDescription;
        this.amount = value.amount;
    }

    public Generateprofitandlossstatement(
        String categoryType,
        String itemDescription,
        BigDecimal amount
    ) {
        this.categoryType = categoryType;
        this.itemDescription = itemDescription;
        this.amount = amount;
    }

    /**
     * Getter for
     * <code>public.generateprofitandlossstatement.category_type</code>.
     */
    public String getCategoryType() {
        return this.categoryType;
    }

    /**
     * Setter for
     * <code>public.generateprofitandlossstatement.category_type</code>.
     */
    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    /**
     * Getter for
     * <code>public.generateprofitandlossstatement.item_description</code>.
     */
    public String getItemDescription() {
        return this.itemDescription;
    }

    /**
     * Setter for
     * <code>public.generateprofitandlossstatement.item_description</code>.
     */
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    /**
     * Getter for <code>public.generateprofitandlossstatement.amount</code>.
     */
    public BigDecimal getAmount() {
        return this.amount;
    }

    /**
     * Setter for <code>public.generateprofitandlossstatement.amount</code>.
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Generateprofitandlossstatement other = (Generateprofitandlossstatement) obj;
        if (this.categoryType == null) {
            if (other.categoryType != null)
                return false;
        }
        else if (!this.categoryType.equals(other.categoryType))
            return false;
        if (this.itemDescription == null) {
            if (other.itemDescription != null)
                return false;
        }
        else if (!this.itemDescription.equals(other.itemDescription))
            return false;
        if (this.amount == null) {
            if (other.amount != null)
                return false;
        }
        else if (!this.amount.equals(other.amount))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.categoryType == null) ? 0 : this.categoryType.hashCode());
        result = prime * result + ((this.itemDescription == null) ? 0 : this.itemDescription.hashCode());
        result = prime * result + ((this.amount == null) ? 0 : this.amount.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Generateprofitandlossstatement (");

        sb.append(categoryType);
        sb.append(", ").append(itemDescription);
        sb.append(", ").append(amount);

        sb.append(")");
        return sb.toString();
    }
}
