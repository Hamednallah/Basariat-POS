package com.basariatpos.model.form;

import java.util.Objects;

public class LensAttributes {
    private String material;
    private String shade;
    private String reflectionType;

    // Constructors
    public LensAttributes() {}

    public LensAttributes(String material, String shade, String reflectionType) {
        this.material = material;
        this.shade = shade;
        this.reflectionType = reflectionType;
    }

    // Getters
    public String getMaterial() { return material; }
    public String getShade() { return shade; }
    public String getReflectionType() { return reflectionType; }

    // Setters
    public void setMaterial(String material) { this.material = material; }
    public void setShade(String shade) { this.shade = shade; }
    public void setReflectionType(String reflectionType) { this.reflectionType = reflectionType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LensAttributes that = (LensAttributes) o;
        return Objects.equals(material, that.material) &&
               Objects.equals(shade, that.shade) &&
               Objects.equals(reflectionType, that.reflectionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, shade, reflectionType);
    }

    @Override
    public String toString() {
        return "LensAttributes{" +
               "material='" + material + '\'' +
               ", shade='" + shade + '\'' +
               ", reflectionType='" + reflectionType + '\'' +
               '}';
    }
}
