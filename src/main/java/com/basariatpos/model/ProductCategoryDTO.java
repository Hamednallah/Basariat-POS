package com.basariatpos.model;

import java.util.Objects;

public class ProductCategoryDTO {
    private int categoryId;
    private String categoryNameEn;
    private String categoryNameAr;
    // No isActive flag as per database schema for ProductCategories

    // Default constructor
    public ProductCategoryDTO() {}

    // Constructor for creating new DTOs (ID might be 0 or not set)
    public ProductCategoryDTO(String categoryNameEn, String categoryNameAr) {
        this.categoryNameEn = categoryNameEn;
        this.categoryNameAr = categoryNameAr;
    }

    // Full constructor
    public ProductCategoryDTO(int categoryId, String categoryNameEn, String categoryNameAr) {
        this.categoryId = categoryId;
        this.categoryNameEn = categoryNameEn;
        this.categoryNameAr = categoryNameAr;
    }

    // Getters
    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryNameEn() {
        return categoryNameEn;
    }

    public String getCategoryNameAr() {
        return categoryNameAr;
    }

    // Setters
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryNameEn(String categoryNameEn) {
        this.categoryNameEn = categoryNameEn;
    }

    public void setCategoryNameAr(String categoryNameAr) {
        this.categoryNameAr = categoryNameAr;
    }

    @Override
    public String toString() {
        return "ProductCategoryDTO{" +
               "categoryId=" + categoryId +
               ", categoryNameEn='" + categoryNameEn + '\'' +
               ", categoryNameAr='" + categoryNameAr + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCategoryDTO that = (ProductCategoryDTO) o;
        return categoryId == that.categoryId &&
               Objects.equals(categoryNameEn, that.categoryNameEn) &&
               Objects.equals(categoryNameAr, that.categoryNameAr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, categoryNameEn, categoryNameAr);
    }
}
