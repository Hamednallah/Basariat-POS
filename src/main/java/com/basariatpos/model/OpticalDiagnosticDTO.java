package com.basariatpos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

public class OpticalDiagnosticDTO {
    private int diagnosticId;
    private int patientId;
    private LocalDate diagnosticDate;
    private boolean isContactLensRx;
    private String contactLensDetails; // E.g., Brand, Type, BC, Dia, Power

    // Spectacle Prescription - Distance
    private BigDecimal odSphDist; // Oculus Dexter (Right Eye) - Spherical for Distance
    private BigDecimal odCylDist; // Oculus Dexter - Cylindrical for Distance
    private Integer    odAxisDist;  // Oculus Dexter - Axis for Distance
    private BigDecimal osSphDist; // Oculus Sinister (Left Eye) - Spherical for Distance
    private BigDecimal osCylDist; // Oculus Sinister - Cylindrical for Distance
    private Integer    osAxisDist;  // Oculus Sinister - Axis for Distance

    // Spectacle Prescription - Reading Addition (if any)
    private BigDecimal odAdd;     // Oculus Dexter - Reading Addition
    private BigDecimal osAdd;     // Oculus Sinister - Reading Addition

    private BigDecimal ipd;       // Interpupillary Distance
    private String remarks;       // General remarks or notes

    private Integer createdByUserId;
    private OffsetDateTime createdAt;

    // Default constructor
    public OpticalDiagnosticDTO() {}

    // Getters
    public int getDiagnosticId() { return diagnosticId; }
    public int getPatientId() { return patientId; }
    public LocalDate getDiagnosticDate() { return diagnosticDate; }
    public boolean isContactLensRx() { return isContactLensRx; }
    public String getContactLensDetails() { return contactLensDetails; }
    public BigDecimal getOdSphDist() { return odSphDist; }
    public BigDecimal getOdCylDist() { return odCylDist; }
    public Integer getOdAxisDist() { return odAxisDist; }
    public BigDecimal getOsSphDist() { return osSphDist; }
    public BigDecimal getOsCylDist() { return osCylDist; }
    public Integer getOsAxisDist() { return osAxisDist; }
    public BigDecimal getOdAdd() { return odAdd; }
    public BigDecimal getOsAdd() { return osAdd; }
    public BigDecimal getIpd() { return ipd; }
    public String getRemarks() { return remarks; }
    public Integer getCreatedByUserId() { return createdByUserId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setDiagnosticId(int diagnosticId) { this.diagnosticId = diagnosticId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public void setDiagnosticDate(LocalDate diagnosticDate) { this.diagnosticDate = diagnosticDate; }
    public void setContactLensRx(boolean contactLensRx) { isContactLensRx = contactLensRx; }
    public void setContactLensDetails(String contactLensDetails) { this.contactLensDetails = contactLensDetails; }
    public void setOdSphDist(BigDecimal odSphDist) { this.odSphDist = odSphDist; }
    public void setOdCylDist(BigDecimal odCylDist) { this.odCylDist = odCylDist; }
    public void setOdAxisDist(Integer odAxisDist) { this.odAxisDist = odAxisDist; }
    public void setOsSphDist(BigDecimal osSphDist) { this.osSphDist = osSphDist; }
    public void setOsCylDist(BigDecimal osCylDist) { this.osCylDist = osCylDist; }
    public void setOsAxisDist(Integer osAxisDist) { this.osAxisDist = osAxisDist; }
    public void setOdAdd(BigDecimal odAdd) { this.odAdd = odAdd; }
    public void setOsAdd(BigDecimal osAdd) { this.osAdd = osAdd; }
    public void setIpd(BigDecimal ipd) { this.ipd = ipd; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "OpticalDiagnosticDTO{" +
               "diagnosticId=" + diagnosticId +
               ", patientId=" + patientId +
               ", diagnosticDate=" + diagnosticDate +
               ", isContactLensRx=" + isContactLensRx +
               (isContactLensRx && contactLensDetails != null ? ", contactLensDetails='" + contactLensDetails + '\'' : "") +
               ", OD_Dist=" + odSphDist + "/" + odCylDist + "x" + odAxisDist +
               ", OS_Dist=" + osSphDist + "/" + osCylDist + "x" + osAxisDist +
               (odAdd != null || osAdd != null ? ", Add=" + odAdd + "/" + osAdd : "") +
               (ipd != null ? ", IPD=" + ipd : "") +
               (remarks != null && !remarks.isEmpty() ? ", remarks='" + remarks.substring(0, Math.min(remarks.length(), 30)) + "...'" : "") +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpticalDiagnosticDTO that = (OpticalDiagnosticDTO) o;
        return diagnosticId == that.diagnosticId &&
               patientId == that.patientId &&
               isContactLensRx == that.isContactLensRx &&
               Objects.equals(diagnosticDate, that.diagnosticDate) &&
               Objects.equals(contactLensDetails, that.contactLensDetails) &&
               Objects.equals(odSphDist, that.odSphDist) &&
               Objects.equals(odCylDist, that.odCylDist) &&
               Objects.equals(odAxisDist, that.odAxisDist) &&
               Objects.equals(osSphDist, that.osSphDist) &&
               Objects.equals(osCylDist, that.osCylDist) &&
               Objects.equals(osAxisDist, that.osAxisDist) &&
               Objects.equals(odAdd, that.odAdd) &&
               Objects.equals(osAdd, that.osAdd) &&
               Objects.equals(ipd, that.ipd) &&
               Objects.equals(remarks, that.remarks) &&
               Objects.equals(createdByUserId, that.createdByUserId) &&
               Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagnosticId, patientId, diagnosticDate, isContactLensRx, contactLensDetails,
                            odSphDist, odCylDist, odAxisDist, osSphDist, osCylDist, osAxisDist,
                            odAdd, osAdd, ipd, remarks, createdByUserId, createdAt);
    }
}
