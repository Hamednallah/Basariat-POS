package com.basariatpos.model;

import java.util.Objects;

public class BankNameDTO {
    private int bankNameId;
    private String bankNameEn;
    private String bankNameAr;
    private boolean isActive;

    // Default constructor
    public BankNameDTO() {
        this.isActive = true; // Default to active for new entries
    }

    // Constructor for creating new DTOs before saving (ID might be 0 or not set)
    public BankNameDTO(String bankNameEn, String bankNameAr, boolean isActive) {
        this.bankNameEn = bankNameEn;
        this.bankNameAr = bankNameAr;
        this.isActive = isActive;
    }

    // Full constructor
    public BankNameDTO(int bankNameId, String bankNameEn, String bankNameAr, boolean isActive) {
        this.bankNameId = bankNameId;
        this.bankNameEn = bankNameEn;
        this.bankNameAr = bankNameAr;
        this.isActive = isActive;
    }

    // Getters
    public int getBankNameId() {
        return bankNameId;
    }

    public String getBankNameEn() {
        return bankNameEn;
    }

    public String getBankNameAr() {
        return bankNameAr;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setBankNameId(int bankNameId) {
        this.bankNameId = bankNameId;
    }

    public void setBankNameEn(String bankNameEn) {
        this.bankNameEn = bankNameEn;
    }

    public void setBankNameAr(String bankNameAr) {
        this.bankNameAr = bankNameAr;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "BankNameDTO{" +
               "bankNameId=" + bankNameId +
               ", bankNameEn='" + bankNameEn + '\'' +
               ", bankNameAr='" + bankNameAr + '\'' +
               ", isActive=" + isActive +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankNameDTO that = (BankNameDTO) o;
        return bankNameId == that.bankNameId &&
               isActive == that.isActive &&
               Objects.equals(bankNameEn, that.bankNameEn) &&
               Objects.equals(bankNameAr, that.bankNameAr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bankNameId, bankNameEn, bankNameAr, isActive);
    }
}
