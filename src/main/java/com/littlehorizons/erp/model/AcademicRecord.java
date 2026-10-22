package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.Term;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Nota obtenida por un estudiante en una materia y período (de 0.0 a 5.0).
 * <p>Tabla: {@code registros_academicos}</p>
 */
public class AcademicRecord {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla registros_academicos) ----------
    // Identificador único del registro de nota
    private int id;
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Id de la materia (llave foránea hacia materias)
    private int subjectId;
    // Id del profesor (llave foránea hacia profesores)
    private int teacherId;
    // Año académico, por ejemplo 2026
    private int academicYear;
    // Período académico (I, II, III, IV o Final)
    private Term term;
    // Nota de 0.0 a 5.0 con un decimal (BigDecimal evita errores de redondeo)
    private BigDecimal grade;
    // Observaciones (texto libre, opcional)
    private String notes;
    // Fecha en que se registró la nota
    private LocalDate recordDate;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el identificador del registro. */
    public int getId() {
        return id;
    }

    /** Asigna el identificador del registro. */
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

    /** Devuelve el id del profesor. */
    public int getTeacherId() {
        return teacherId;
    }

    /** Asigna el id del profesor. */
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
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

    /** Devuelve la nota. */
    public BigDecimal getGrade() {
        return grade;
    }

    /** Asigna la nota. */
    public void setGrade(BigDecimal grade) {
        this.grade = grade;
    }

    /** Devuelve las observaciones. */
    public String getNotes() {
        return notes;
    }

    /** Asigna las observaciones. */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /** Devuelve la fecha de registro. */
    public LocalDate getRecordDate() {
        return recordDate;
    }

    /** Asigna la fecha de registro. */
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    // ---------- Regla de negocio de la entidad ----------

    /** Nota mínima para aprobar una materia en la escala colombiana (constante en MAYÚSCULAS). */
    public static final BigDecimal PASSING_GRADE = new BigDecimal("3.0");

    /**
     * Indica si la nota aprueba la materia.
     *
     * @return {@code true} cuando la nota es 3.0 o más
     */
    public boolean isPassing() {
        // compareTo devuelve >= 0 cuando grade es mayor o igual que PASSING_GRADE
        // (con BigDecimal no se usa >= porque no es un número primitivo)
        return grade.compareTo(PASSING_GRADE) >= 0;
    }
}
