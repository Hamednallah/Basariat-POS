/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.pojos;


import java.io.Serializable;
import java.time.OffsetDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Centerprofile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer profileId;
    private String centerName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String country;
    private String postalCode;
    private String phonePrimary;
    private String phoneSecondary;
    private String emailAddress;
    private String website;
    private String logoImagePath;
    private String taxIdentifier;
    private String currencySymbol;
    private String currencyCode;
    private String receiptFooterMessage;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Centerprofile() {}

    public Centerprofile(Centerprofile value) {
        this.profileId = value.profileId;
        this.centerName = value.centerName;
        this.addressLine1 = value.addressLine1;
        this.addressLine2 = value.addressLine2;
        this.city = value.city;
        this.country = value.country;
        this.postalCode = value.postalCode;
        this.phonePrimary = value.phonePrimary;
        this.phoneSecondary = value.phoneSecondary;
        this.emailAddress = value.emailAddress;
        this.website = value.website;
        this.logoImagePath = value.logoImagePath;
        this.taxIdentifier = value.taxIdentifier;
        this.currencySymbol = value.currencySymbol;
        this.currencyCode = value.currencyCode;
        this.receiptFooterMessage = value.receiptFooterMessage;
        this.createdAt = value.createdAt;
        this.updatedAt = value.updatedAt;
    }

    public Centerprofile(
        Integer profileId,
        String centerName,
        String addressLine1,
        String addressLine2,
        String city,
        String country,
        String postalCode,
        String phonePrimary,
        String phoneSecondary,
        String emailAddress,
        String website,
        String logoImagePath,
        String taxIdentifier,
        String currencySymbol,
        String currencyCode,
        String receiptFooterMessage,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
    ) {
        this.profileId = profileId;
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Getter for <code>public.centerprofile.profile_id</code>.
     */
    public Integer getProfileId() {
        return this.profileId;
    }

    /**
     * Setter for <code>public.centerprofile.profile_id</code>.
     */
    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    /**
     * Getter for <code>public.centerprofile.center_name</code>.
     */
    public String getCenterName() {
        return this.centerName;
    }

    /**
     * Setter for <code>public.centerprofile.center_name</code>.
     */
    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    /**
     * Getter for <code>public.centerprofile.address_line1</code>.
     */
    public String getAddressLine1() {
        return this.addressLine1;
    }

    /**
     * Setter for <code>public.centerprofile.address_line1</code>.
     */
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * Getter for <code>public.centerprofile.address_line2</code>.
     */
    public String getAddressLine2() {
        return this.addressLine2;
    }

    /**
     * Setter for <code>public.centerprofile.address_line2</code>.
     */
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    /**
     * Getter for <code>public.centerprofile.city</code>.
     */
    public String getCity() {
        return this.city;
    }

    /**
     * Setter for <code>public.centerprofile.city</code>.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Getter for <code>public.centerprofile.country</code>.
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * Setter for <code>public.centerprofile.country</code>.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Getter for <code>public.centerprofile.postal_code</code>.
     */
    public String getPostalCode() {
        return this.postalCode;
    }

    /**
     * Setter for <code>public.centerprofile.postal_code</code>.
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Getter for <code>public.centerprofile.phone_primary</code>.
     */
    public String getPhonePrimary() {
        return this.phonePrimary;
    }

    /**
     * Setter for <code>public.centerprofile.phone_primary</code>.
     */
    public void setPhonePrimary(String phonePrimary) {
        this.phonePrimary = phonePrimary;
    }

    /**
     * Getter for <code>public.centerprofile.phone_secondary</code>.
     */
    public String getPhoneSecondary() {
        return this.phoneSecondary;
    }

    /**
     * Setter for <code>public.centerprofile.phone_secondary</code>.
     */
    public void setPhoneSecondary(String phoneSecondary) {
        this.phoneSecondary = phoneSecondary;
    }

    /**
     * Getter for <code>public.centerprofile.email_address</code>.
     */
    public String getEmailAddress() {
        return this.emailAddress;
    }

    /**
     * Setter for <code>public.centerprofile.email_address</code>.
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Getter for <code>public.centerprofile.website</code>.
     */
    public String getWebsite() {
        return this.website;
    }

    /**
     * Setter for <code>public.centerprofile.website</code>.
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Getter for <code>public.centerprofile.logo_image_path</code>.
     */
    public String getLogoImagePath() {
        return this.logoImagePath;
    }

    /**
     * Setter for <code>public.centerprofile.logo_image_path</code>.
     */
    public void setLogoImagePath(String logoImagePath) {
        this.logoImagePath = logoImagePath;
    }

    /**
     * Getter for <code>public.centerprofile.tax_identifier</code>.
     */
    public String getTaxIdentifier() {
        return this.taxIdentifier;
    }

    /**
     * Setter for <code>public.centerprofile.tax_identifier</code>.
     */
    public void setTaxIdentifier(String taxIdentifier) {
        this.taxIdentifier = taxIdentifier;
    }

    /**
     * Getter for <code>public.centerprofile.currency_symbol</code>.
     */
    public String getCurrencySymbol() {
        return this.currencySymbol;
    }

    /**
     * Setter for <code>public.centerprofile.currency_symbol</code>.
     */
    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    /**
     * Getter for <code>public.centerprofile.currency_code</code>.
     */
    public String getCurrencyCode() {
        return this.currencyCode;
    }

    /**
     * Setter for <code>public.centerprofile.currency_code</code>.
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     * Getter for <code>public.centerprofile.receipt_footer_message</code>.
     */
    public String getReceiptFooterMessage() {
        return this.receiptFooterMessage;
    }

    /**
     * Setter for <code>public.centerprofile.receipt_footer_message</code>.
     */
    public void setReceiptFooterMessage(String receiptFooterMessage) {
        this.receiptFooterMessage = receiptFooterMessage;
    }

    /**
     * Getter for <code>public.centerprofile.created_at</code>.
     */
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Setter for <code>public.centerprofile.created_at</code>.
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Getter for <code>public.centerprofile.updated_at</code>.
     */
    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    /**
     * Setter for <code>public.centerprofile.updated_at</code>.
     */
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Centerprofile other = (Centerprofile) obj;
        if (this.profileId == null) {
            if (other.profileId != null)
                return false;
        }
        else if (!this.profileId.equals(other.profileId))
            return false;
        if (this.centerName == null) {
            if (other.centerName != null)
                return false;
        }
        else if (!this.centerName.equals(other.centerName))
            return false;
        if (this.addressLine1 == null) {
            if (other.addressLine1 != null)
                return false;
        }
        else if (!this.addressLine1.equals(other.addressLine1))
            return false;
        if (this.addressLine2 == null) {
            if (other.addressLine2 != null)
                return false;
        }
        else if (!this.addressLine2.equals(other.addressLine2))
            return false;
        if (this.city == null) {
            if (other.city != null)
                return false;
        }
        else if (!this.city.equals(other.city))
            return false;
        if (this.country == null) {
            if (other.country != null)
                return false;
        }
        else if (!this.country.equals(other.country))
            return false;
        if (this.postalCode == null) {
            if (other.postalCode != null)
                return false;
        }
        else if (!this.postalCode.equals(other.postalCode))
            return false;
        if (this.phonePrimary == null) {
            if (other.phonePrimary != null)
                return false;
        }
        else if (!this.phonePrimary.equals(other.phonePrimary))
            return false;
        if (this.phoneSecondary == null) {
            if (other.phoneSecondary != null)
                return false;
        }
        else if (!this.phoneSecondary.equals(other.phoneSecondary))
            return false;
        if (this.emailAddress == null) {
            if (other.emailAddress != null)
                return false;
        }
        else if (!this.emailAddress.equals(other.emailAddress))
            return false;
        if (this.website == null) {
            if (other.website != null)
                return false;
        }
        else if (!this.website.equals(other.website))
            return false;
        if (this.logoImagePath == null) {
            if (other.logoImagePath != null)
                return false;
        }
        else if (!this.logoImagePath.equals(other.logoImagePath))
            return false;
        if (this.taxIdentifier == null) {
            if (other.taxIdentifier != null)
                return false;
        }
        else if (!this.taxIdentifier.equals(other.taxIdentifier))
            return false;
        if (this.currencySymbol == null) {
            if (other.currencySymbol != null)
                return false;
        }
        else if (!this.currencySymbol.equals(other.currencySymbol))
            return false;
        if (this.currencyCode == null) {
            if (other.currencyCode != null)
                return false;
        }
        else if (!this.currencyCode.equals(other.currencyCode))
            return false;
        if (this.receiptFooterMessage == null) {
            if (other.receiptFooterMessage != null)
                return false;
        }
        else if (!this.receiptFooterMessage.equals(other.receiptFooterMessage))
            return false;
        if (this.createdAt == null) {
            if (other.createdAt != null)
                return false;
        }
        else if (!this.createdAt.equals(other.createdAt))
            return false;
        if (this.updatedAt == null) {
            if (other.updatedAt != null)
                return false;
        }
        else if (!this.updatedAt.equals(other.updatedAt))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.profileId == null) ? 0 : this.profileId.hashCode());
        result = prime * result + ((this.centerName == null) ? 0 : this.centerName.hashCode());
        result = prime * result + ((this.addressLine1 == null) ? 0 : this.addressLine1.hashCode());
        result = prime * result + ((this.addressLine2 == null) ? 0 : this.addressLine2.hashCode());
        result = prime * result + ((this.city == null) ? 0 : this.city.hashCode());
        result = prime * result + ((this.country == null) ? 0 : this.country.hashCode());
        result = prime * result + ((this.postalCode == null) ? 0 : this.postalCode.hashCode());
        result = prime * result + ((this.phonePrimary == null) ? 0 : this.phonePrimary.hashCode());
        result = prime * result + ((this.phoneSecondary == null) ? 0 : this.phoneSecondary.hashCode());
        result = prime * result + ((this.emailAddress == null) ? 0 : this.emailAddress.hashCode());
        result = prime * result + ((this.website == null) ? 0 : this.website.hashCode());
        result = prime * result + ((this.logoImagePath == null) ? 0 : this.logoImagePath.hashCode());
        result = prime * result + ((this.taxIdentifier == null) ? 0 : this.taxIdentifier.hashCode());
        result = prime * result + ((this.currencySymbol == null) ? 0 : this.currencySymbol.hashCode());
        result = prime * result + ((this.currencyCode == null) ? 0 : this.currencyCode.hashCode());
        result = prime * result + ((this.receiptFooterMessage == null) ? 0 : this.receiptFooterMessage.hashCode());
        result = prime * result + ((this.createdAt == null) ? 0 : this.createdAt.hashCode());
        result = prime * result + ((this.updatedAt == null) ? 0 : this.updatedAt.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Centerprofile (");

        sb.append(profileId);
        sb.append(", ").append(centerName);
        sb.append(", ").append(addressLine1);
        sb.append(", ").append(addressLine2);
        sb.append(", ").append(city);
        sb.append(", ").append(country);
        sb.append(", ").append(postalCode);
        sb.append(", ").append(phonePrimary);
        sb.append(", ").append(phoneSecondary);
        sb.append(", ").append(emailAddress);
        sb.append(", ").append(website);
        sb.append(", ").append(logoImagePath);
        sb.append(", ").append(taxIdentifier);
        sb.append(", ").append(currencySymbol);
        sb.append(", ").append(currencyCode);
        sb.append(", ").append(receiptFooterMessage);
        sb.append(", ").append(createdAt);
        sb.append(", ").append(updatedAt);

        sb.append(")");
        return sb.toString();
    }
}
