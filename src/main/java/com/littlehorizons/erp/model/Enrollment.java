package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.EnrollmentStatus;
import java.time.LocalDate;

/**
 * Matrícula anual de un estudiante en un grado.
 * <p>Tabla: {@code matriculas}</p>
 */
public class Enrollment {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla matriculas) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Id del grado (llave foránea hacia grados)
    private int gradeLevelId;
    // Año académico, por ejemplo 2026
    private int academicYear;
    // Estado de la matrícula: Activa, Completada o Retirada
    private EnrollmentStatus status;
    // Fecha en que se hizo la matrícula o inscripción
    private LocalDate enrollmentDate;
    // Fecha de retiro (null mientras siga activo)
    private LocalDate withdrawalDate;
    // Observaciones (texto libre, opcional)
    private String notes;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el identificador único. */
    public int getId() {
        return id;
    }

    /** Asigna el identificador único. */
    public void setId(int id) {
        this.id = id;
    }

    /** Devuelve el id del estudiante. */
    public int getStudentId() {
        return studentId;
    }

    /** Asigna el id del estudiante. */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /** Devuelve el id del grado. */
    public int getGradeLevelId() {
        return gradeLevelId;
    }

    /** Asigna el id del grado. */
    public void setGradeLevelId(int gradeLevelId) {
        this.gradeLevelId = gradeLevelId;
    }

    /** Devuelve el año académico. */
    public int getAcademicYear() {
        return academicYear;
    }

    /** Asigna el año académico. */
    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    /** Devuelve el estado. */
    public EnrollmentStatus getStatus() {
        return status;
    }

    /** Asigna el estado. */
    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    /** Devuelve la fecha de matrícula. */
    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    /** Asigna la fecha de matrícula. */
    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    /** Devuelve la fecha de retiro. */
    public LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    /** Asigna la fecha de retiro. */
    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    /** Devuelve las observaciones. */
    public String getNotes() {
        return notes;
    }

    /** Asigna las observaciones. */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
