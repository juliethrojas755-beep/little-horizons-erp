package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Descuento personalizado de un estudiante sobre un concepto (hermanos, beca).
 * <p>Tabla: {@code descuentos_estudiante}</p>
 */
public class StudentDiscount {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla descuentos_estudiante) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Id del concepto de pago (llave foránea hacia conceptos_pago)
    private int conceptId;
    // Tipo de descuento: Hermanos, Beca u Otro
    private DiscountType type;
    // Porcentaje de descuento, de 0 a 100
    private BigDecimal percentage;
    // Fecha desde la cual aplica
    private LocalDate startDate;
    // Fecha hasta la cual aplica (null = sin vencimiento)
    private LocalDate endDate;

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

    /** Devuelve el tipo de descuento. */
    public DiscountType getType() {
        return type;
    }

    /** Asigna el tipo de descuento. */
    public void setType(DiscountType type) {
        this.type = type;
    }

    /** Devuelve el porcentaje. */
    public BigDecimal getPercentage() {
        return percentage;
    }

    /** Asigna el porcentaje. */
    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    /** Devuelve la fecha de inicio. */
    public LocalDate getStartDate() {
        return startDate;
    }

    /** Asigna la fecha de inicio. */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /** Devuelve la fecha de fin. */
    public LocalDate getEndDate() {
        return endDate;
    }

    /** Asigna la fecha de fin. */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // ---------- Regla de negocio de la entidad ----------

    /**
     * Un descuento es válido desde su fecha de inicio hasta su fecha de fin (si tiene).
     *
     * @return {@code true} cuando el descuento aplica en la fecha dada
     */
    public boolean isValidOn(LocalDate date) {
        // no debe ser anterior al inicio y, si hay fecha de fin, no debe ser posterior a ella
        return !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate));
    }
}
