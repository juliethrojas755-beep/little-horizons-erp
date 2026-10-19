package com.littlehorizons.erp.util;

/**
 * Unchecked exception that wraps a {@link java.sql.SQLException}.
 * Upper layers do not need to know JDBC details, they only know the operation failed.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
