package com.basariatpos.model;

import java.util.Objects;

/**
 * Data Transfer Object for Center Profile information.
 */
public class CenterProfileDTO {

    // Assuming profile_id is always 1 and managed by the repository, not part of user-editable DTO fields.
    private String centerName;
    private String addressLine1;
    private String addressLine2; // Optional
    private String city;
    private String country;
    private String postalCode; // Optional
    private String phonePrimary;
    private String phoneSecondary; // Optional
    private String emailAddress; // Optional
    private String website; // Optional
    private String logoImagePath; // Optional
    private String taxIdentifier; // Optional
    private String currencySymbol;
    private String currencyCode; // e.g., USD, EUR, SDG
    private String receiptFooterMessage; // Optional

    // Default constructor
    public CenterProfileDTO() {}

    // All-args constructor (or use a builder pattern for many fields)
    public CenterProfileDTO(String centerName, String addressLine1, String addressLine2, String city, String country,
                            String postalCode, String phonePrimary, String phoneSecondary, String emailAddress,
                            String website, String logoImagePath, String taxIdentifier, String currencySymbol,
                            String currencyCode, String receiptFooterMessage) {
        this.centerName = centerName;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.phonePrimary = phonePrimary;
        this.phoneSecondary = phoneSecondary;
        this.emailAddress = emailAddress;
        this.website = website;
        this.logoImagePath = logoImagePath;
        this.taxIdentifier = taxIdentifier;
        this.currencySymbol = currencySymbol;
        this.currencyCode = currencyCode;
        this.receiptFooterMessage = receiptFooterMessage;
    }

    // Getters
    public String getCenterName() { return centerName; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getPostalCode() { return postalCode; }
    public String getPhonePrimary() { return phonePrimary; }
    public String getPhoneSecondary() { return phoneSecondary; }
    public String getEmailAddress() { return emailAddress; }
    public String getWebsite() { return website; }
    public String getLogoImagePath() { return logoImagePath; }
    public String getTaxIdentifier() { return taxIdentifier; }
    public String getCurrencySymbol() { return currencySymbol; }
    public String getCurrencyCode() { return currencyCode; }
    public String getReceiptFooterMessage() { return receiptFooterMessage; }

    // Setters
    public void setCenterName(String centerName) { this.centerName = centerName; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    public void setCity(String city) { this.city = city; }
    public void setCountry(String country) { this.country = country; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setPhonePrimary(String phonePrimary) { this.phonePrimary = phonePrimary; }
    public void setPhoneSecondary(String phoneSecondary) { this.phoneSecondary = phoneSecondary; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public void setWebsite(String website) { this.website = website; }
    public void setLogoImagePath(String logoImagePath) { this.logoImagePath = logoImagePath; }
    public void setTaxIdentifier(String taxIdentifier) { this.taxIdentifier = taxIdentifier; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public void setReceiptFooterMessage(String receiptFooterMessage) { this.receiptFooterMessage = receiptFooterMessage; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CenterProfileDTO that = (CenterProfileDTO) o;
        return Objects.equals(centerName, that.centerName) &&
               Objects.equals(addressLine1, that.addressLine1) &&
               Objects.equals(addressLine2, that.addressLine2) &&
               Objects.equals(city, that.city) &&
               Objects.equals(country, that.country) &&
               Objects.equals(postalCode, that.postalCode) &&
               Objects.equals(phonePrimary, that.phonePrimary) &&
               Objects.equals(phoneSecondary, that.phoneSecondary) &&
               Objects.equals(emailAddress, that.emailAddress) &&
               Objects.equals(website, that.website) &&
               Objects.equals(logoImagePath, that.logoImagePath) &&
               Objects.equals(taxIdentifier, that.taxIdentifier) &&
               Objects.equals(currencySymbol, that.currencySymbol) &&
               Objects.equals(currencyCode, that.currencyCode) &&
               Objects.equals(receiptFooterMessage, that.receiptFooterMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(centerName, addressLine1, addressLine2, city, country, postalCode, phonePrimary,
                            phoneSecondary, emailAddress, website, logoImagePath, taxIdentifier, currencySymbol,
                            currencyCode, receiptFooterMessage);
    }

    @Override
    public String toString() {
        return "CenterProfileDTO{" +
               "centerName='" + centerName + '\'' +
               ", addressLine1='" + addressLine1 + '\'' +
               // ... (include other fields for completeness if desired)
               ", currencyCode='" + currencyCode + '\'' +
               ", currencySymbol='" + currencySymbol + '\'' +
               '}';
    }
}
