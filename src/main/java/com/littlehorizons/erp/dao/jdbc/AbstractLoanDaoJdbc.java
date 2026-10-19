package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.LoanDao;
import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.enums.LoanStatus;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Shared SQL for {@code prestamos_estudiante} and {@code prestamos_profesor}.
 * Both tables have the same columns except the borrower, so subclasses only say
 * which table, which borrower column and how to create the entity (Template Method).
 */
public abstract class AbstractLoanDaoJdbc<T extends Loan> extends BaseJdbcDao implements LoanDao<T> {

    /** @return table name */
    protected abstract String table();

    /** @return borrower column (id_estudiante or id_profesor) */
    protected abstract String borrowerColumn();

    /** Creates the concrete loan with its borrower id already set. */
    protected abstract T newLoan(int borrowerId);

    @Override
    public int insert(Connection connection, T loan) throws SQLException {
        return insert(connection, "INSERT INTO " + table() + " (id_recurso, " + borrowerColumn() + ", fecha_prestamo, "
                        + "fecha_devolucion_esperada, estado_prestamo, observaciones) VALUES (?, ?, ?, ?, ?, ?)",
                loan.getResourceId(), loan.getBorrowerId(), loan.getLoanDate(), loan.getDueDate(), loan.getStatus(),
                loan.getNotes());
    }

    @Override
    public Optional<T> findById(int loanId) {
        return queryOne("SELECT * FROM " + table() + " WHERE id_prestamo = ?", this::map, loanId);
    }

    @Override
    public List<T> findOpen() {
        return queryList("SELECT * FROM " + table() + " WHERE fecha_devolucion_real IS NULL ORDER BY fecha_devolucion_esperada",
                this::map);
    }

    @Override
    public List<T> findAll() {
        return queryList("SELECT * FROM " + table() + " ORDER BY fecha_prestamo DESC", this::map);
    }

    @Override
    public List<T> findByBorrower(int borrowerId) {
        return queryList("SELECT * FROM " + table() + " WHERE " + borrowerColumn() + " = ? ORDER BY fecha_prestamo DESC",
                this::map, borrowerId);
    }

    @Override
    public void markReturned(Connection connection, int loanId, LocalDate returnDate) throws SQLException {
        update(connection, "UPDATE " + table() + " SET fecha_devolucion_real = ?, estado_prestamo = ? WHERE id_prestamo = ?",
                returnDate, LoanStatus.RETURNED, loanId);
    }

    @Override
    public int refreshOverdue(LocalDate today) {
        return update("UPDATE " + table() + " SET estado_prestamo = ? WHERE estado_prestamo = ? "
                + "AND fecha_devolucion_real IS NULL AND fecha_devolucion_esperada < ?", LoanStatus.OVERDUE, LoanStatus.ACTIVE, today);
    }

    private T map(ResultSet rs) throws SQLException {
        T loan = newLoan(rs.getInt(borrowerColumn()));
        loan.setId(rs.getInt("id_prestamo"));
        loan.setResourceId(rs.getInt("id_recurso"));
        loan.setLoanDate(getDate(rs, "fecha_prestamo"));
        loan.setDueDate(getDate(rs, "fecha_devolucion_esperada"));
        loan.setReturnDate(getDate(rs, "fecha_devolucion_real"));
        loan.setStatus(LoanStatus.fromDbValue(rs.getString("estado_prestamo")));
        loan.setNotes(rs.getString("observaciones"));
        return loan;
    }
}
