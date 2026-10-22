package com.littlehorizons.erp.model;

/**
 * Grado académico (Preescolar = 0, Primero = 1 ... Once = 11).
 * <p>Tabla: {@code grados}</p>
 */
public class GradeLevel {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla grados) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Nombre del grado: Preescolar, Primero ... Once
    private String name;
    // Nivel numérico del grado, sirve para ordenar (0 = Preescolar ... 11 = Once)
    private int numericLevel;

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

    /** Devuelve el nivel numérico. */
    public int getNumericLevel() {
        return numericLevel;
    }

    /** Asigna el nivel numérico. */
    public void setNumericLevel(int numericLevel) {
        this.numericLevel = numericLevel;
    }

    /** Al mostrar un grado en un menú se imprime su nombre. */
    @Override
    public String toString() {
        return name;
    }
}
