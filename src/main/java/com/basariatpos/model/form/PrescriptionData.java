package com.basariatpos.model.form;

import java.math.BigDecimal;
import java.util.Objects;

public class PrescriptionData {
    private BigDecimal odSph;
    private BigDecimal odCyl;
    private Integer odAxis;
    private BigDecimal osSph;
    private BigDecimal osCyl;
    private Integer osAxis;
    private BigDecimal odAdd;
    private BigDecimal osAdd;
    private BigDecimal ipd; // Interpupillary Distance, can be single or far/near

    // Constructors
    public PrescriptionData() {}

    // Getters
    public BigDecimal getOdSph() { return odSph; }
    public BigDecimal getOdCyl() { return odCyl; }
    public Integer getOdAxis() { return odAxis; }
    public BigDecimal getOsSph() { return osSph; }
    public BigDecimal getOsCyl() { return osCyl; }
    public Integer getOsAxis() { return osAxis; }
    public BigDecimal getOdAdd() { return odAdd; }
    public BigDecimal getOsAdd() { return osAdd; }
    public BigDecimal getIpd() { return ipd; }

    // Setters
    public void setOdSph(BigDecimal odSph) { this.odSph = odSph; }
    public void setOdCyl(BigDecimal odCyl) { this.odCyl = odCyl; }
    public void setOdAxis(Integer odAxis) { this.odAxis = odAxis; }
    public void setOsSph(BigDecimal osSph) { this.osSph = osSph; }
    public void setOsCyl(BigDecimal osCyl) { this.osCyl = osCyl; }
    public void setOsAxis(Integer osAxis) { this.osAxis = osAxis; }
    public void setOdAdd(BigDecimal odAdd) { this.odAdd = odAdd; }
    public void setOsAdd(BigDecimal osAdd) { this.osAdd = osAdd; }
    public void setIpd(BigDecimal ipd) { this.ipd = ipd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrescriptionData that = (PrescriptionData) o;
        return Objects.equals(odSph, that.odSph) &&
               Objects.equals(odCyl, that.odCyl) &&
               Objects.equals(odAxis, that.odAxis) &&
               Objects.equals(osSph, that.osSph) &&
               Objects.equals(osCyl, that.osCyl) &&
               Objects.equals(osAxis, that.osAxis) &&
               Objects.equals(odAdd, that.odAdd) &&
               Objects.equals(osAdd, that.osAdd) &&
               Objects.equals(ipd, that.ipd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(odSph, odCyl, odAxis, osSph, osCyl, osAxis, odAdd, osAdd, ipd);
    }

    @Override
    public String toString() {
        return "PrescriptionData{" +
               "odSph=" + odSph + ", odCyl=" + odCyl + ", odAxis=" + odAxis +
               ", osSph=" + osSph + ", osCyl=" + osCyl + ", osAxis=" + osAxis +
               ", odAdd=" + odAdd + ", osAdd=" + osAdd + ", ipd=" + ipd +
               '}';
    }
}
