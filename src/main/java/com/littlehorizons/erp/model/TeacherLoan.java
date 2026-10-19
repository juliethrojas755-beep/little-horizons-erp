package com.littlehorizons.erp.model;

/**
 * Loan of a resource to a teacher.
 * <p>Table: {@code prestamos_profesor}</p>
 */
public class TeacherLoan extends Loan {

    private int teacherId;

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public int getBorrowerId() {
        return teacherId;
    }
}
