package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.model.StudentLoan;

/**
 * Loans to students ({@code prestamos_estudiante}).
 */
public class StudentLoanDaoJdbc extends AbstractLoanDaoJdbc<StudentLoan> {

    // Tabla donde se guardan los préstamos a estudiantes
    @Override
    protected String table() {
        return "prestamos_estudiante";
    }

    // Columna de la llave foránea hacia estudiantes
    @Override
    protected String borrowerColumn() {
        return "id_estudiante";
    }

    // Crea el préstamo del tipo correcto y le asigna el prestatario
    @Override
    protected StudentLoan newLoan(int borrowerId) {
        StudentLoan loan = new StudentLoan();
        loan.setStudentId(borrowerId);
        return loan;
    }
}
