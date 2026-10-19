package com.littlehorizons.erp.model;

import com.littlehorizons.erp.model.enums.TeacherStatus;
import java.time.LocalDate;

/**
 * Teacher of the school.
 * <p>Table: {@code profesores}</p>
 */
public class Teacher extends Person {

    private String specialization;
    private LocalDate hireDate;
    private TeacherStatus status;

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public TeacherStatus getStatus() {
        return status;
    }

    public void setStatus(TeacherStatus status) {
        this.status = status;
    }
}
