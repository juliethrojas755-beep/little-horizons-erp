package com.littlehorizons.erp.model;

/**
 * Préstamo de un recurso a un profesor.
 * <p>Tabla: {@code prestamos_profesor}</p>
 */
public class TeacherLoan extends Loan {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla prestamos_profesor) ----------
    // Id del profesor que tiene prestado el recurso
    private int teacherId;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el id del profesor. */
    public int getTeacherId() {
        return teacherId;
    }

    /** Asigna el id del profesor. */
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    // Implementa el método abstracto de Loan: aquí el prestatario es el profesor
    @Override
    public int getBorrowerId() {
        return teacherId;
    }
}
