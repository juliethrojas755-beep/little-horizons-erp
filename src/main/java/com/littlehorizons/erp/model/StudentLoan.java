package com.littlehorizons.erp.model;

/**
 * Préstamo de un recurso a un estudiante.
 * <p>Tabla: {@code prestamos_estudiante}</p>
 */
public class StudentLoan extends Loan {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla prestamos_estudiante) ----------
    // Id del estudiante que tiene prestado el recurso
    private int studentId;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el id del estudiante. */
    public int getStudentId() {
        return studentId;
    }

    /** Asigna el id del estudiante. */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // Implementa el método abstracto de Loan: aquí el prestatario es el estudiante
    @Override
    public int getBorrowerId() {
        return studentId;
    }
}
