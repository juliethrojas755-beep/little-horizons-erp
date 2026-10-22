package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.EnrollmentStatus;
import com.littlehorizons.erp.model.enums.Term;
import java.time.LocalDate;

/**
 * Inscripción de un estudiante en una materia por período (muchos a muchos).
 * <p>Tabla: {@code inscripciones_materia}</p>
 */
public class SubjectEnrollment {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla inscripciones_materia) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Id de la materia (llave foránea hacia materias)
    private int subjectId;
    // Año académico, por ejemplo 2026
    private int academicYear;
    // Período académico (I, II, III, IV o Final)
    private Term term;
    // Sección o grupo (A, B, C)
    private String section;
    // Fecha en que se hizo la matrícula o inscripción
    private LocalDate enrollmentDate;
    // Estado de la inscripción: Activa, Retirada o Completada
    private EnrollmentStatus status;

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

    /** Devuelve el id de la materia. */
    public int getSubjectId() {
        return subjectId;
    }

    /** Asigna el id de la materia. */
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    /** Devuelve el año académico. */
    public int getAcademicYear() {
        return academicYear;
    }

    /** Asigna el año académico. */
    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    /** Devuelve el período. */
    public Term getTerm() {
        return term;
    }

    /** Asigna el período. */
    public void setTerm(Term term) {
        this.term = term;
    }

    /** Devuelve la sección. */
    public String getSection() {
        return section;
    }

    /** Asigna la sección. */
    public void setSection(String section) {
        this.section = section;
    }

    /** Devuelve la fecha de matrícula. */
    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    /** Asigna la fecha de matrícula. */
    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    /** Devuelve el estado. */
    public EnrollmentStatus getStatus() {
        return status;
    }

    /** Asigna el estado. */
    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
}
