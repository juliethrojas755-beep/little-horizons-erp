package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Loan;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Shared operations for student and teacher loans. Two tables, one contract.
 *
 * @param <T> {@code StudentLoan} or {@code TeacherLoan}
 */
public interface LoanDao<T extends Loan> {

    int insert(Connection connection, T loan) throws SQLException;

    Optional<T> findById(int loanId);

    /** Loans not returned yet (Activo or Vencido). */
    List<T> findOpen();

    List<T> findAll();

    List<T> findByBorrower(int borrowerId);

    void markReturned(Connection connection, int loanId, LocalDate returnDate) throws SQLException;

    /** Changes Activo to Vencido for loans whose due date already passed. */
    int refreshOverdue(LocalDate today);
}
