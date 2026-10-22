package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.LoanStatus;
import java.time.LocalDate;

/**
 * Datos comunes de un préstamo de recurso.
 * Es abstracta: las subclases definen quién es el prestatario (estudiante o profesor).
 */
public abstract class Loan {

    // ---------- Atributos ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Id del recurso prestado (llave foránea hacia recursos)
    private int resourceId;
    // Fecha en que se entregó el recurso
    private LocalDate loanDate;
    // Fecha en la que debe devolverse el recurso
    private LocalDate dueDate;
    // Fecha de devolución real (null mientras no se devuelva)
    private LocalDate returnDate;
    // Estado del préstamo: Activo, Devuelto o Vencido
    private LoanStatus status;
    // Observaciones (texto libre, opcional)
    private String notes;

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

    /** Devuelve el id del recurso. */
    public int getResourceId() {
        return resourceId;
    }

    /** Asigna el id del recurso. */
    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    /** Devuelve la fecha de préstamo. */
    public LocalDate getLoanDate() {
        return loanDate;
    }

    /** Asigna la fecha de préstamo. */
    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    /** Devuelve la fecha de devolución programada. */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /** Asigna la fecha de devolución programada. */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /** Devuelve la fecha de devolución real. */
    public LocalDate getReturnDate() {
        return returnDate;
    }

    /** Asigna la fecha de devolución real. */
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    /** Devuelve el estado. */
    public LoanStatus getStatus() {
        return status;
    }

    /** Asigna el estado. */
    public void setStatus(LoanStatus status) {
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

    // ---------- Comportamiento común ----------

    /**
     * Método abstracto: cada subclase dice quién es el prestatario.
     *
     * @return el id del estudiante o del profesor que tiene el recurso
     */
    public abstract int getBorrowerId();

    /**
     * Un préstamo está vencido cuando no se ha devuelto y su fecha límite ya pasó.
     *
     * @param today fecha de referencia (normalmente la fecha de hoy)
     * @return {@code true} cuando el préstamo debe salir en el reporte de vencidos
     */
    public boolean isOverdue(LocalDate today) {
        // returnDate == null significa que el recurso todavía no ha sido devuelto
        return returnDate == null && dueDate.isBefore(today);
    }
}
