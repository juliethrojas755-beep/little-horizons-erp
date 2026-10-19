package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.model.TeacherLoan;

/**
 * Loans to teachers ({@code prestamos_profesor}).
 */
public class TeacherLoanDaoJdbc extends AbstractLoanDaoJdbc<TeacherLoan> {

    @Override
    protected String table() {
        return "prestamos_profesor";
    }

    @Override
    protected String borrowerColumn() {
        return "id_profesor";
    }

    @Override
    protected TeacherLoan newLoan(int borrowerId) {
        TeacherLoan loan = new TeacherLoan();
        loan.setTeacherId(borrowerId);
        return loan;
    }
}
