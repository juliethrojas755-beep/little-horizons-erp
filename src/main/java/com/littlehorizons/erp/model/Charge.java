package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Valor que un estudiante debe; queda pendiente hasta que un pago lo salde.
 * <p>Tabla: {@code cargos}</p>
 */
public class Charge {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla cargos) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Id del concepto de pago (llave foránea hacia conceptos_pago)
    private int conceptId;
    // Valor a cobrar, ya con el descuento aplicado
    private BigDecimal amount;
    // Fecha en que se generó el cargo
    private LocalDate generatedDate;
    // Fecha de vencimiento del cargo (a partir de ella se cuenta la mora)
    private LocalDate dueDate;
    // Estado del cargo: Pendiente o Pagado
    private ChargeStatus status;
    // Id del pago (llave foránea hacia pagos)
    private Integer paymentId;

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

    /** Devuelve el id del concepto. */
    public int getConceptId() {
        return conceptId;
    }

    /** Asigna el id del concepto. */
    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    /** Devuelve el valor. */
    public BigDecimal getAmount() {
        return amount;
    }

    /** Asigna el valor. */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /** Devuelve la fecha de generación. */
    public LocalDate getGeneratedDate() {
        return generatedDate;
    }

    /** Asigna la fecha de generación. */
    public void setGeneratedDate(LocalDate generatedDate) {
        this.generatedDate = generatedDate;
    }

    /** Devuelve la fecha de vencimiento. */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /** Asigna la fecha de vencimiento. */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /** Devuelve el estado. */
    public ChargeStatus getStatus() {
        return status;
    }

    /** Asigna el estado. */
    public void setStatus(ChargeStatus status) {
        this.status = status;
    }

    /** Devuelve el id del pago. */
    public Integer getPaymentId() {
        return paymentId;
    }

    /** Asigna el id del pago. */
    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    // ---------- Regla de negocio de la entidad ----------

    /**
     * Un cargo está en mora cuando sigue pendiente y ya pasó más de un mes desde su vencimiento.
     *
     * @param today fecha de referencia (normalmente la fecha de hoy)
     * @return {@code true} cuando el cargo debe salir en el reporte de morosos
     */
    public boolean isDelinquent(LocalDate today) {
        // 1) debe seguir pendiente y 2) un mes después del vencimiento ya debe haber pasado
        return status == ChargeStatus.PENDING && dueDate.plusMonths(1).isBefore(today);
    }
}
