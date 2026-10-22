package com.littlehorizons.erp.util;

/**
 * Envuelve una {@link java.sql.SQLException}.
 * Las capas superiores no necesitan conocer los detalles de JDBC: solo saben que falló la operación con la base de datos.
 */
public class DataAccessException extends RuntimeException {

    /**
     * @param message mensaje entendible para el usuario
     * @param cause   el error original de SQL (se conserva para el log)
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
