/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EXAMEN;

/**
 *
 * @author camper
 */
public class model {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla asignaciones_profesor) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Id del profesor (llave foránea hacia profesores)
    private int teacherId;
    // Id de la materia (llave foránea hacia materias)
    private int subjectId;
    // Año académico, por ejemplo 2026
    private int academicYear;
    // Sección o grupo (A, B, C)
    private String section;

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

    /** Devuelve el id del profesor. */
    public int getTeacherId() {
        return teacherId;
    }

    /** Asigna el id del profesor. */
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
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

    /** Devuelve la sección. */
    public String getSection() {
        return section;
    }

    /** Asigna la sección. */
    public void setSection(String section) {
        this.section = section;
    }
}

