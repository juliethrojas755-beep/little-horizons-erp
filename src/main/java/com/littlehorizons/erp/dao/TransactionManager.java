package com.littlehorizons.erp.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Runs several DAO calls in a single database transaction (all or nothing).
 */
public interface TransactionManager {

    /** Work executed inside the transaction. It receives the shared connection. */
    @FunctionalInterface
    interface TransactionalWork<R> {
        R execute(Connection connection) throws SQLException;
    }

    <R> R inTransaction(TransactionalWork<R> work);
}
