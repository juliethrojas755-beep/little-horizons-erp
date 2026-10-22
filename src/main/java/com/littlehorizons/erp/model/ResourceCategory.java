package com.littlehorizons.erp.model;

/**
 * Categoría de un recurso (Libro, Equipo de Laboratorio...).
 * <p>Tabla: {@code categorias_recurso}</p>
 */
public class ResourceCategory {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla categorias_recurso) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Nombre de la categoría
    private String name;
    // Descripción (texto libre)
    private String description;

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

    /** Al mostrar una categoría en un menú se imprime su nombre. */
    @Override
    public String toString() {
        return name;
    }
}
