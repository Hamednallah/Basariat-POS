/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.pojos;


import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Banknames implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer bankNameId;
    private String bankNameEn;
    private String bankNameAr;
    private Boolean isActive;

    public Banknames() {}

    public Banknames(Banknames value) {
        this.bankNameId = value.bankNameId;
        this.bankNameEn = value.bankNameEn;
        this.bankNameAr = value.bankNameAr;
        this.isActive = value.isActive;
    }

    public Banknames(
        Integer bankNameId,
        String bankNameEn,
        String bankNameAr,
        Boolean isActive
    ) {
        this.bankNameId = bankNameId;
        this.bankNameEn = bankNameEn;
        this.bankNameAr = bankNameAr;
        this.isActive = isActive;
    }

    /**
     * Getter for <code>public.banknames.bank_name_id</code>.
     */
    public Integer getBankNameId() {
        return this.bankNameId;
    }

    /**
     * Setter for <code>public.banknames.bank_name_id</code>.
     */
    public void setBankNameId(Integer bankNameId) {
        this.bankNameId = bankNameId;
    }

    /**
     * Getter for <code>public.banknames.bank_name_en</code>.
     */
    public String getBankNameEn() {
        return this.bankNameEn;
    }

    /**
     * Setter for <code>public.banknames.bank_name_en</code>.
     */
    public void setBankNameEn(String bankNameEn) {
        this.bankNameEn = bankNameEn;
    }

    /**
     * Getter for <code>public.banknames.bank_name_ar</code>.
     */
    public String getBankNameAr() {
        return this.bankNameAr;
    }

    /**
     * Setter for <code>public.banknames.bank_name_ar</code>.
     */
    public void setBankNameAr(String bankNameAr) {
        this.bankNameAr = bankNameAr;
    }

    /**
     * Getter for <code>public.banknames.is_active</code>.
     */
    public Boolean getIsActive() {
        return this.isActive;
    }

    /**
     * Setter for <code>public.banknames.is_active</code>.
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Banknames other = (Banknames) obj;
        if (this.bankNameId == null) {
            if (other.bankNameId != null)
                return false;
        }
        else if (!this.bankNameId.equals(other.bankNameId))
            return false;
        if (this.bankNameEn == null) {
            if (other.bankNameEn != null)
                return false;
        }
        else if (!this.bankNameEn.equals(other.bankNameEn))
            return false;
        if (this.bankNameAr == null) {
            if (other.bankNameAr != null)
                return false;
        }
        else if (!this.bankNameAr.equals(other.bankNameAr))
            return false;
        if (this.isActive == null) {
            if (other.isActive != null)
                return false;
        }
        else if (!this.isActive.equals(other.isActive))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.bankNameId == null) ? 0 : this.bankNameId.hashCode());
        result = prime * result + ((this.bankNameEn == null) ? 0 : this.bankNameEn.hashCode());
        result = prime * result + ((this.bankNameAr == null) ? 0 : this.bankNameAr.hashCode());
        result = prime * result + ((this.isActive == null) ? 0 : this.isActive.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Banknames (");

        sb.append(bankNameId);
        sb.append(", ").append(bankNameEn);
        sb.append(", ").append(bankNameAr);
        sb.append(", ").append(isActive);

        sb.append(")");
        return sb.toString();
    }
}
