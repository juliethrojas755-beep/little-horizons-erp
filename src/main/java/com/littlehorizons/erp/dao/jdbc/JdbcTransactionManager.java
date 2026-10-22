package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.TransactionManager;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.ErrorLogger;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Transacción con JDBC: desactiva el guardado automático, confirma (commit) cuando el trabajo termina
 * bien y deshace todo (rollback) ante cualquier error.
 */
public class JdbcTransactionManager implements TransactionManager {

    @Override
    public <R> R inTransaction(TransactionalWork<R> work) {
        // La conexión se cierra sola al salir del try (try-with-resources)
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            // Por defecto cada sentencia se guarda sola; se apaga para decidir nosotros cuándo confirmar
            connection.setAutoCommit(false);
            try {
                R result = work.execute(connection);   // aquí se ejecutan los inserts/updates compartiendo la conexión
                connection.commit();                    // todo salió bien: se guardan los cambios juntos
                return result;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();                  // algo falló: se deshace TODO lo hecho en la transacción
                throw e;
            }
        } catch (SQLException e) {
            ErrorLogger.getInstance().error("JdbcTransactionManager", "Transaction rolled back", e);
            throw new DataAccessException("No se pudo completar la operación en la base de datos", e);
        }
    }
}
