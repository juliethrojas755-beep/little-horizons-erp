package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.PaymentMethod;
import com.littlehorizons.erp.model.enums.PaymentPeriod;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dinero recibido por un concepto de un estudiante.
 * <p>Tabla: {@code pagos}</p>
 */
public class Payment {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla pagos) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Id del estudiante (llave foránea hacia estudiantes)
    private int studentId;
    // Id del acudiente que pagó (puede ser null)
    private Integer guardianId;
    // Id del concepto de pago (llave foránea hacia conceptos_pago)
    private int conceptId;
    // Valor en pesos colombianos
    private BigDecimal amount;
    // Fecha en que se recibió el pago
    private LocalDate paymentDate;
    // Método de pago (efectivo, transferencia, tarjeta...)
    private PaymentMethod method;
    // Número de recibo, único por pago (REC-2026-00001)
    private String receiptNumber;
    // Año académico, por ejemplo 2026
    private Integer academicYear;
    // Período al que aplica el pago (I, II, III, IV o Anual)
    private PaymentPeriod period;
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

    /** Devuelve el id del estudiante. */
    public int getStudentId() {
        return studentId;
    }

    /** Asigna el id del estudiante. */
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    /** Devuelve el id del acudiente pagador. */
    public Integer getGuardianId() {
        return guardianId;
    }

    /** Asigna el id del acudiente pagador. */
    public void setGuardianId(Integer guardianId) {
        this.guardianId = guardianId;
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

    /** Devuelve la fecha de pago. */
    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    /** Asigna la fecha de pago. */
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    /** Devuelve el método de pago. */
    public PaymentMethod getMethod() {
        return method;
    }

    /** Asigna el método de pago. */
    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    /** Devuelve el número de recibo. */
    public String getReceiptNumber() {
        return receiptNumber;
    }

    /** Asigna el número de recibo. */
    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    /** Devuelve el año académico. */
    public Integer getAcademicYear() {
        return academicYear;
    }

    /** Asigna el año académico. */
    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    /** Devuelve el período. */
    public PaymentPeriod getPeriod() {
        return period;
    }

    /** Asigna el período. */
    public void setPeriod(PaymentPeriod period) {
        this.period = period;
    }

    /** Devuelve las observaciones. */
    public String getNotes() {
        return notes;
    }

    /** Asigna las observaciones. */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
