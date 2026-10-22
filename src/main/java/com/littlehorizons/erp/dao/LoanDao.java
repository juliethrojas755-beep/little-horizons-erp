package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Loan;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Operaciones comunes de los préstamos a estudiantes y a profesores.
 * Son dos tablas distintas pero un solo contrato (genéricos).
 *
 * @param <T> {@code StudentLoan} o {@code TeacherLoan}
 */
public interface LoanDao<T extends Loan> {

    /**
     * Guarda el préstamo dentro de la transacción que también cambia el estado del recurso.
     *
     * @return el id generado
     */
    int insert(Connection connection, T loan) throws SQLException;

    /**
     * Busca un registro por su id.
     *
     * @return el registro, o vacío si no existe
     */
    Optional<T> findById(int loanId);

    /**
     * Préstamos sin devolver (Activo o Vencido).
     */
    List<T> findOpen();

    /**
     * Devuelve todos los registros.
     */
    List<T> findAll();

    /**
     * Préstamos de un estudiante o de un profesor.
     */
    List<T> findByBorrower(int borrowerId);

    /**
     * Marca el préstamo como Devuelto con la fecha real, dentro de la transacción.
     */
    void markReturned(Connection connection, int loanId, LocalDate returnDate) throws SQLException;

    /**
     * Cambia de Activo a Vencido los préstamos cuya fecha límite ya pasó.
     *
     * @return cuántos préstamos se actualizaron
     */
    int refreshOverdue(LocalDate today);
}
