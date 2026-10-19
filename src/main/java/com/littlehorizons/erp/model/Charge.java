package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.ChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Amount a student owes for a concept, pending until a payment settles it.
 * <p>Table: {@code cargos}</p>
 */
public class Charge {

    private int id;
    private int studentId;
    private int conceptId;
    private BigDecimal amount;  // discount already applied
    private LocalDate generatedDate;
    private LocalDate dueDate;
    private ChargeStatus status;
    private Integer paymentId;  // nullable

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDate generatedDate) {
        this.generatedDate = generatedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public ChargeStatus getStatus() {
        return status;
    }

    public void setStatus(ChargeStatus status) {
        this.status = status;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    /**
     * A charge is delinquent when it is still pending more than one month after its due date.
     *
     * @param today reference date
     * @return {@code true} when the charge belongs to the overdue report
     */
    public boolean isDelinquent(LocalDate today) {
        return status == ChargeStatus.PENDING && dueDate.plusMonths(1).isBefore(today);
    }
}
