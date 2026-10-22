package com.littlehorizons.erp.model;

/**
 * Materia que se dicta en un grado.
 * <p>Tabla: {@code materias}</p>
 */
public class Subject {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla materias) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Código único de la materia, por ejemplo MAT-5
    private String code;
    // Nombre de la materia
    private String name;
    // Descripción (texto libre)
    private String description;
    // Id del grado (llave foránea hacia grados)
    private int gradeLevelId;
    // Horas semanales de clase
    private int weeklyHours;
    // true si sigue activo; false si fue desactivado
    private boolean active;

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

    /** Devuelve el código. */
    public String getCode() {
        return code;
    }

    /** Asigna el código. */
    public void setCode(String code) {
        this.code = code;
    }

    /** Devuelve el nombre. */
    public String getName() {
        return name;
    }

    /** Asigna el nombre. */
    public void setName(String name) {
        this.name = name;
    }

    /** Devuelve la descripción. */
    public String getDescription() {
        return description;
    }

    /** Asigna la descripción. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Devuelve el id del grado. */
    public int getGradeLevelId() {
        return gradeLevelId;
    }

    /** Asigna el id del grado. */
    public void setGradeLevelId(int gradeLevelId) {
        this.gradeLevelId = gradeLevelId;
    }

    /** Devuelve las horas semanales. */
    public int getWeeklyHours() {
        return weeklyHours;
    }

    /** Asigna las horas semanales. */
    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    /** Devuelve si está activo. */
    public boolean isActive() {
        return active;
    }

    /** Asigna si está activo. */
    public void setActive(boolean active) {
        this.active = active;
    }

    /** Al mostrar una materia se imprime "CÓDIGO - Nombre". */
    @Override
    public String toString() {
        return code + " - " + name;
    }
}
