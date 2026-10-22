package com.littlehorizons.erp.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta varias operaciones de DAOs dentro de UNA sola transacción: o se guardan todas o no se guarda ninguna.
 * Ejemplo: confirmar un pago guarda el pago, salda el cargo y crea la factura juntos.
 */
public interface TransactionManager {

    /**
     * Trabajo que se ejecuta dentro de la transacción; recibe la conexión compartida.
     * Es una interfaz funcional, por eso se puede escribir como lambda.
     *
     * @param <R> tipo del resultado que devuelve el trabajo
     */
    @FunctionalInterface
    interface TransactionalWork<R> {
        R execute(Connection connection) throws SQLException;
    }

    /**
     * Abre la transacción, ejecuta el trabajo y confirma (commit); si algo falla deshace todo (rollback).
     *
     * @param work trabajo a ejecutar
     * @return lo que devuelva el trabajo
     */
    <R> R inTransaction(TransactionalWork<R> work);
}
