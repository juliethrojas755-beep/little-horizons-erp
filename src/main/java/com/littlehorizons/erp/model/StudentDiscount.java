package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Per-student discount over a payment concept (siblings, scholarship).
 * <p>Table: {@code descuentos_estudiante}</p>
 */
public class StudentDiscount {

    private int id;
    private int studentId;
    private int conceptId;
    private DiscountType type;
    private BigDecimal percentage;  // 0 to 100
    private LocalDate startDate;
    private LocalDate endDate;  // nullable

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

    public DiscountType getType() {
        return type;
    }

    public void setType(DiscountType type) {
        this.type = type;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /** @return {@code true} when the discount is valid on the given date */
    public boolean isValidOn(LocalDate date) {
        return !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate));
    }
}
