package com.littlehorizons.erp.model;

/**
 * Academic grade (Preescolar = 0, Primero = 1 ... Once = 11).
 * <p>Table: {@code grados}</p>
 */
public class GradeLevel {

    private int id;
    private String name;
    private int numericLevel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumericLevel() {
        return numericLevel;
    }

    public void setNumericLevel(int numericLevel) {
        this.numericLevel = numericLevel;
    }

    @Override
    public String toString() {
        return name;
    }
}
