package com.littlehorizons.erp.model;

/**
 * Vínculo entre un estudiante y un acudiente (relación muchos a muchos).
 * <p>Tabla: {@code estudiante_acudiente}</p>
 */
public class StudentGuardian {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla estudiante_acudiente) ----------
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Id del acudiente (llave foránea hacia acudientes)
    private int guardianId;
    // true si es el acudiente principal para comunicaciones
    private boolean primary;
    // true si se le puede llamar en una emergencia
    private boolean emergencyContact;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el id del estudiante. */
    public int getStudentId() {
        return studentId;
    }

    /** Asigna el id del estudiante. */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /** Devuelve el id del acudiente. */
    public int getGuardianId() {
        return guardianId;
    }

    /** Asigna el id del acudiente. */
    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    /** Devuelve si es el acudiente principal. */
    public boolean isPrimary() {
        return primary;
    }

    /** Asigna si es el acudiente principal. */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /** Devuelve si es contacto de emergencia. */
    public boolean isEmergencyContact() {
        return emergencyContact;
    }

    /** Asigna si es contacto de emergencia. */
    public void setEmergencyContact(boolean emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
