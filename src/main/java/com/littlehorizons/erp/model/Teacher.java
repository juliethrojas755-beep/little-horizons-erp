package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.TeacherStatus;
import java.time.LocalDate;

/**
 * Profesor del colegio.
 * <p>Tabla: {@code profesores}</p>
 */
public class Teacher extends Person {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla profesores) ----------
    // Área de especialización (Matemáticas, Inglés...)
    private String specialization;
    // Fecha en que fue contratado
    private LocalDate hireDate;
    // Estado del profesor: Activo, Inactivo o Licencia
    private TeacherStatus status;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve la especialización. */
    public String getSpecialization() {
        return specialization;
    }

    /** Asigna la especialización. */
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    /** Devuelve la fecha de contratación. */
    public LocalDate getHireDate() {
        return hireDate;
    }

    /** Asigna la fecha de contratación. */
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    /** Devuelve el estado. */
    public TeacherStatus getStatus() {
        return status;
    }

    /** Asigna el estado. */
    public void setStatus(TeacherStatus status) {
        this.status = status;
    }
}
