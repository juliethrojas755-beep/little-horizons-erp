package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.model.StudentLoan;

/**
 * Loans to students ({@code prestamos_estudiante}).
 */
public class StudentLoanDaoJdbc extends AbstractLoanDaoJdbc<StudentLoan> {

    @Override
    protected String table() {
        return "prestamos_estudiante";
    }

    @Override
    protected String borrowerColumn() {
        return "id_estudiante";
    }

    @Override
    protected StudentLoan newLoan(int borrowerId) {
        StudentLoan loan = new StudentLoan();
        loan.setStudentId(borrowerId);
        return loan;
    }
}
