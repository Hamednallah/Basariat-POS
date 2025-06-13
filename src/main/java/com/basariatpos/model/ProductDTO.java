package com.basariatpos.model;

import java.util.Objects;

public class ProductDTO {
    private int productId;
    private String productCode; // User-defined or system-generated unique code
    private String productNameEn;
    private String productNameAr;
    private int categoryId;
    private String categoryNameEn; // For display, joined from ProductCategories
    private String categoryNameAr; // For display, joined from ProductCategories
    private String descriptionEn;  // Optional
    private String descriptionAr;  // Optional
    private boolean isService;     // True if this is a service, false if a physical product
    private boolean isStockItem;   // True if inventory is tracked for this item

    // Default constructor
    public ProductDTO() {
        this.isService = false; // Default to physical product
        this.isStockItem = true;  // Default to stockable if physical product
    }

    // Constructor for creating a new product (before saving, ID might be 0)
    public ProductDTO(String productCode, String productNameEn, String productNameAr, int categoryId,
                      String descriptionEn, String descriptionAr, boolean isService, boolean isStockItem) {
        this(); // Call default constructor
        this.productCode = productCode;
        this.productNameEn = productNameEn;
        this.productNameAr = productNameAr;
        this.categoryId = categoryId;
        this.descriptionEn = descriptionEn;
        this.descriptionAr = descriptionAr;
        this.isService = isService;
        this.isStockItem = isStockItem;
        if (isService) { // Services are typically not stock items
            this.isStockItem = false;
        }
    }

    // Full constructor (often used when mapping from DB record with joined category names)
    public ProductDTO(int productId, String productCode, String productNameEn, String productNameAr,
                      int categoryId, String categoryNameEn, String categoryNameAr,
                      String descriptionEn, String descriptionAr, boolean isService, boolean isStockItem) {
        this(productCode, productNameEn, productNameAr, categoryId, descriptionEn, descriptionAr, isService, isStockItem);
        this.productId = productId;
        this.categoryNameEn = categoryNameEn;
        this.categoryNameAr = categoryNameAr;
    }


    // Getters
    public int getProductId() { return productId; }
    public String getProductCode() { return productCode; }
    public String getProductNameEn() { return productNameEn; }
    public String getProductNameAr() { return productNameAr; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryNameEn() { return categoryNameEn; }
    public String getCategoryNameAr() { return categoryNameAr; }
    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionAr() { return descriptionAr; }
    public boolean isService() { return isService; }
    public boolean isStockItem() { return isStockItem; }

    // Setters
    public void setProductId(int productId) { this.productId = productId; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public void setProductNameEn(String productNameEn) { this.productNameEn = productNameEn; }
    public void setProductNameAr(String productNameAr) { this.productNameAr = productNameAr; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setCategoryNameEn(String categoryNameEn) { this.categoryNameEn = categoryNameEn; }
    public void setCategoryNameAr(String categoryNameAr) { this.categoryNameAr = categoryNameAr; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public void setDescriptionAr(String descriptionAr) { this.descriptionAr = descriptionAr; }
    public void setService(boolean service) {
        isService = service;
        if (isService) { // Services are typically not stock items
            this.isStockItem = false;
        }
    }
    public void setStockItem(boolean stockItem) {
        // If it's a service, it cannot be a stock item
        if (this.isService && stockItem) {
            this.isStockItem = false;
        } else {
            this.isStockItem = stockItem;
        }
    }

    @Override
    public String toString() {
        // Primarily for ComboBox display or logging, can be adjusted
        return productNameEn + (productNameAr != null && !productNameAr.isEmpty() ? " / " + productNameAr : "") +
               (productCode != null && !productCode.isEmpty() ? " (" + productCode + ")" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDTO that = (ProductDTO) o;
        return productId == that.productId &&
               categoryId == that.categoryId &&
               isService == that.isService &&
               isStockItem == that.isStockItem &&
               Objects.equals(productCode, that.productCode) &&
               Objects.equals(productNameEn, that.productNameEn) &&
               Objects.equals(productNameAr, that.productNameAr) &&
               Objects.equals(descriptionEn, that.descriptionEn) &&
               Objects.equals(descriptionAr, that.descriptionAr);
        // categoryNameEn and categoryNameAr are for display, not core equality of product entity
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productCode, productNameEn, productNameAr, categoryId,
                            descriptionEn, descriptionAr, isService, isStockItem);
    }
}
