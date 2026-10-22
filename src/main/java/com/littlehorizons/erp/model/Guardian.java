package com.littlehorizons.erp.model;

/**
 * Acudiente (tutor legal) responsable de uno o varios estudiantes.
 * <p>Tabla: {@code acudientes}</p>
 */
public class Guardian extends Person {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla acudientes) ----------
    // Parentesco con el estudiante (Padre, Madre, Abuelo...)
    private String relationship;
    // Teléfono alterno (opcional)
    private String secondaryPhone;
    // Dirección de residencia
    private String address;
    // Ocupación laboral (opcional)
    private String occupation;
    // true si esta persona es la responsable de los pagos
    private boolean financiallyResponsible;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el parentesco. */
    public String getRelationship() {
        return relationship;
    }

    /** Asigna el parentesco. */
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    /** Devuelve el teléfono secundario. */
    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    /** Asigna el teléfono secundario. */
    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    /** Devuelve la dirección. */
    public String getAddress() {
        return address;
    }

    /** Asigna la dirección. */
    public void setAddress(String address) {
        this.address = address;
    }

    /** Devuelve la ocupación. */
    public String getOccupation() {
        return occupation;
    }

    /** Asigna la ocupación. */
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    /** Devuelve si es responsable financiero. */
    public boolean isFinanciallyResponsible() {
        return financiallyResponsible;
    }

    /** Asigna si es responsable financiero. */
    public void setFinanciallyResponsible(boolean financiallyResponsible) {
        this.financiallyResponsible = financiallyResponsible;
    }
}
