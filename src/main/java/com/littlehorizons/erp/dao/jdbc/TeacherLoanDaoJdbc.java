package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.model.TeacherLoan;

/**
 * Loans to teachers ({@code prestamos_profesor}).
 */
public class TeacherLoanDaoJdbc extends AbstractLoanDaoJdbc<TeacherLoan> {

    // Tabla donde se guardan los préstamos a profesores
    @Override
    protected String table() {
        return "prestamos_profesor";
    }

    // Columna de la llave foránea hacia profesores
    @Override
    protected String borrowerColumn() {
        return "id_profesor";
    }

    // Crea el préstamo del tipo correcto y le asigna el prestatario
    @Override
    protected TeacherLoan newLoan(int borrowerId) {
        TeacherLoan loan = new TeacherLoan();
        loan.setTeacherId(borrowerId);
        return loan;
    }
}
