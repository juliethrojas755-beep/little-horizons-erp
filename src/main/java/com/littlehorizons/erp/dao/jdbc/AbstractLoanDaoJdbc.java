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
 * SQL compartido de las tablas {@code prestamos_estudiante} y {@code prestamos_profesor}.
 * <p>
 * Las dos tablas tienen las mismas columnas excepto la del prestatario, así que las subclases solo dicen
 * qué tabla usar, cuál es la columna del prestatario y cómo crear el objeto (patrón Template Method).
 * Se escribe el SQL una sola vez en lugar de duplicarlo.
 * </p>
 *
 * @param <T> StudentLoan o TeacherLoan
 */
public abstract class AbstractLoanDaoJdbc<T extends Loan> extends BaseJdbcDao implements LoanDao<T> {

    // ---------- Piezas que cada subclase debe definir ----------

    /** @return nombre de la tabla */
    protected abstract String table();

    /** @return columna del prestatario (id_estudiante o id_profesor) */
    protected abstract String borrowerColumn();

    /** Crea el préstamo concreto con el id del prestatario ya asignado. */
    protected abstract T newLoan(int borrowerId);

    // ---------- Operaciones (el nombre de la tabla se arma con table() y borrowerColumn()) ----------

    @Override
    public int insert(Connection connection, T loan) throws SQLException {
        // Se usa la conexión recibida para que el préstamo y el cambio de estado del recurso vayan en la misma transacción
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
        // "Abierto" = todavía no tiene fecha de devolución real
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
        // Pasa a Vencido todo préstamo Activo, sin devolver, cuya fecha límite ya pasó
        return update("UPDATE " + table() + " SET estado_prestamo = ? WHERE estado_prestamo = ? "
                + "AND fecha_devolucion_real IS NULL AND fecha_devolucion_esperada < ?", LoanStatus.OVERDUE, LoanStatus.ACTIVE, today);
    }

    // ---------- Conversión de una fila en objeto ----------

    private T map(ResultSet rs) throws SQLException {
        T loan = newLoan(rs.getInt(borrowerColumn()));   // la subclase decide si es StudentLoan o TeacherLoan
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
