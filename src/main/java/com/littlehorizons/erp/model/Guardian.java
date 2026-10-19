package com.littlehorizons.erp.model;

/**
 * Legal guardian responsible for one or more students.
 * <p>Table: {@code acudientes}</p>
 */
public class Guardian extends Person {

    private String relationship;  // Padre, Madre, Abuelo...
    private String secondaryPhone;
    private String address;
    private String occupation;
    private boolean financiallyResponsible;

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public boolean isFinanciallyResponsible() {
        return financiallyResponsible;
    }

    public void setFinanciallyResponsible(boolean financiallyResponsible) {
        this.financiallyResponsible = financiallyResponsible;
    }
}
