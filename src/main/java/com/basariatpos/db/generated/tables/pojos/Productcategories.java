/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.pojos;


import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Productcategories implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer categoryId;
    private String categoryNameEn;
    private String categoryNameAr;

    public Productcategories() {}

    public Productcategories(Productcategories value) {
        this.categoryId = value.categoryId;
        this.categoryNameEn = value.categoryNameEn;
        this.categoryNameAr = value.categoryNameAr;
    }

    public Productcategories(
        Integer categoryId,
        String categoryNameEn,
        String categoryNameAr
    ) {
        this.categoryId = categoryId;
        this.categoryNameEn = categoryNameEn;
        this.categoryNameAr = categoryNameAr;
    }

    /**
     * Getter for <code>public.productcategories.category_id</code>.
     */
    public Integer getCategoryId() {
        return this.categoryId;
    }

    /**
     * Setter for <code>public.productcategories.category_id</code>.
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Getter for <code>public.productcategories.category_name_en</code>.
     */
    public String getCategoryNameEn() {
        return this.categoryNameEn;
    }

    /**
     * Setter for <code>public.productcategories.category_name_en</code>.
     */
    public void setCategoryNameEn(String categoryNameEn) {
        this.categoryNameEn = categoryNameEn;
    }

    /**
     * Getter for <code>public.productcategories.category_name_ar</code>.
     */
    public String getCategoryNameAr() {
        return this.categoryNameAr;
    }

    /**
     * Setter for <code>public.productcategories.category_name_ar</code>.
     */
    public void setCategoryNameAr(String categoryNameAr) {
        this.categoryNameAr = categoryNameAr;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Productcategories other = (Productcategories) obj;
        if (this.categoryId == null) {
            if (other.categoryId != null)
                return false;
        }
        else if (!this.categoryId.equals(other.categoryId))
            return false;
        if (this.categoryNameEn == null) {
            if (other.categoryNameEn != null)
                return false;
        }
        else if (!this.categoryNameEn.equals(other.categoryNameEn))
            return false;
        if (this.categoryNameAr == null) {
            if (other.categoryNameAr != null)
                return false;
        }
        else if (!this.categoryNameAr.equals(other.categoryNameAr))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.categoryId == null) ? 0 : this.categoryId.hashCode());
        result = prime * result + ((this.categoryNameEn == null) ? 0 : this.categoryNameEn.hashCode());
        result = prime * result + ((this.categoryNameAr == null) ? 0 : this.categoryNameAr.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Productcategories (");

        sb.append(categoryId);
        sb.append(", ").append(categoryNameEn);
        sb.append(", ").append(categoryNameAr);

        sb.append(")");
        return sb.toString();
    }
}
