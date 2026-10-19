package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.TransactionManager;
import com.littlehorizons.erp.util.DataAccessException;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.ErrorLogger;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC transaction: disables auto-commit, commits when the work ends and rolls back on any error.
 */
public class JdbcTransactionManager implements TransactionManager {

    @Override
    public <R> R inTransaction(TransactionalWork<R> work) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                R result = work.execute(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            ErrorLogger.getInstance().error("JdbcTransactionManager", "Transaction rolled back", e);
            throw new DataAccessException("No se pudo completar la operación en la base de datos", e);
        }
    }
}
