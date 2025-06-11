package com.basariatpos.model;

import java.util.Objects;

public class ExpenseCategoryDTO {
    private int expenseCategoryId;
    private String categoryNameEn;
    private String categoryNameAr;
    private boolean isActive;

    // Default constructor
    public ExpenseCategoryDTO() {
        this.isActive = true; // Default to active
    }

    // Constructor for creating new DTOs (ID might be 0)
    public ExpenseCategoryDTO(String categoryNameEn, String categoryNameAr, boolean isActive) {
        this.categoryNameEn = categoryNameEn;
        this.categoryNameAr = categoryNameAr;
        this.isActive = isActive;
    }

    // Full constructor
    public ExpenseCategoryDTO(int expenseCategoryId, String categoryNameEn, String categoryNameAr, boolean isActive) {
        this.expenseCategoryId = expenseCategoryId;
        this.categoryNameEn = categoryNameEn;
        this.categoryNameAr = categoryNameAr;
        this.isActive = isActive;
    }

    // Getters
    public int getExpenseCategoryId() {
        return expenseCategoryId;
    }

    public String getCategoryNameEn() {
        return categoryNameEn;
    }

    public String getCategoryNameAr() {
        return categoryNameAr;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setExpenseCategoryId(int expenseCategoryId) {
        this.expenseCategoryId = expenseCategoryId;
    }

    public void setCategoryNameEn(String categoryNameEn) {
        this.categoryNameEn = categoryNameEn;
    }

    public void setCategoryNameAr(String categoryNameAr) {
        this.categoryNameAr = categoryNameAr;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "ExpenseCategoryDTO{" +
               "expenseCategoryId=" + expenseCategoryId +
               ", categoryNameEn='" + categoryNameEn + '\'' +
               ", categoryNameAr='" + categoryNameAr + '\'' +
               ", isActive=" + isActive +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpenseCategoryDTO that = (ExpenseCategoryDTO) o;
        return expenseCategoryId == that.expenseCategoryId &&
               isActive == that.isActive &&
               Objects.equals(categoryNameEn, that.categoryNameEn) &&
               Objects.equals(categoryNameAr, that.categoryNameAr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseCategoryId, categoryNameEn, categoryNameAr, isActive);
    }
}
