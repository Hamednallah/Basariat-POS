package com.basariatpos.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public class PatientDTO {
    private int patientId;
    private String systemPatientId; // System-generated unique ID (e.g., PAT-00001)
    private String fullName;
    private String phoneNumber; // Primary contact number
    private String address;     // Optional
    private boolean whatsappOptIn;
    private Integer createdByUserId; // Nullable if system creates or for imported data
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt; // Nullable if not yet updated

    // Default constructor
    public PatientDTO() {
        this.whatsappOptIn = false; // Default to opt-out
    }

    // Constructor for creating a new patient (before saving, ID and systemId might be unset)
    public PatientDTO(String fullName, String phoneNumber, String address, boolean whatsappOptIn) {
        this(); // Call default constructor
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.whatsappOptIn = whatsappOptIn;
    }

    // Full constructor (typically used when mapping from DB record)
    public PatientDTO(int patientId, String systemPatientId, String fullName, String phoneNumber,
                      String address, boolean whatsappOptIn, Integer createdByUserId,
                      OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.patientId = patientId;
        this.systemPatientId = systemPatientId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.whatsappOptIn = whatsappOptIn;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getPatientId() { return patientId; }
    public String getSystemPatientId() { return systemPatientId; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public boolean isWhatsappOptIn() { return whatsappOptIn; }
    public Integer getCreatedByUserId() { return createdByUserId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public void setSystemPatientId(String systemPatientId) { this.systemPatientId = systemPatientId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setWhatsappOptIn(boolean whatsappOptIn) { this.whatsappOptIn = whatsappOptIn; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "PatientDTO{" +
               "patientId=" + patientId +
               ", systemPatientId='" + systemPatientId + '\'' +
               ", fullName='" + fullName + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", whatsappOptIn=" + whatsappOptIn +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientDTO that = (PatientDTO) o;
        return patientId == that.patientId &&
               whatsappOptIn == that.whatsappOptIn &&
               Objects.equals(systemPatientId, that.systemPatientId) &&
               Objects.equals(fullName, that.fullName) &&
               Objects.equals(phoneNumber, that.phoneNumber) &&
               Objects.equals(address, that.address) &&
               Objects.equals(createdByUserId, that.createdByUserId) &&
               Objects.equals(createdAt, that.createdAt) && // Consider precision for datetime comparison
               Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId, systemPatientId, fullName, phoneNumber, address, whatsappOptIn,
                            createdByUserId, createdAt, updatedAt);
    }
}
