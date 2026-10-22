package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.AttendanceStatus;
import java.time.LocalDate;

/**
 * Asistencia diaria de un estudiante en una materia.
 * <p>Tabla: {@code asistencias}</p>
 */
public class Attendance {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla asistencias) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Id de la materia (llave foránea hacia materias)
    private int subjectId;
    // Fecha de la clase
    private LocalDate date;
    // Estado de asistencia: Presente, Ausente, Tardanza o Excusa Médica
    private AttendanceStatus status;
    // Observaciones (texto libre, opcional)
    private String notes;
    // Id del profesor que registró (puede ser null)
    private Integer recordedByTeacherId;

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

    /** Devuelve la fecha. */
    public LocalDate getDate() {
        return date;
    }

    /** Asigna la fecha. */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /** Devuelve el estado. */
    public AttendanceStatus getStatus() {
        return status;
    }

    /** Asigna el estado. */
    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    /** Devuelve las observaciones. */
    public String getNotes() {
        return notes;
    }

    /** Asigna las observaciones. */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /** Devuelve el id del profesor que registró. */
    public Integer getRecordedByTeacherId() {
        return recordedByTeacherId;
    }

    /** Asigna el id del profesor que registró. */
    public void setRecordedByTeacherId(Integer recordedByTeacherId) {
        this.recordedByTeacherId = recordedByTeacherId;
    }
}
