package com.littlehorizons.erp.model;

/**
 * Loan of a resource to a student.
 * <p>Table: {@code prestamos_estudiante}</p>
 */
public class StudentLoan extends Loan {

    private int studentId;

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    @Override
    public int getBorrowerId() {
        return studentId;
    }
}
