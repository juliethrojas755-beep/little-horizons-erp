package com.littlehorizons.erp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Converts the current row of a {@link ResultSet} into an object.
 * It is a functional interface, so every DAO passes its mapping as a lambda or method reference.
 */
@FunctionalInterface
public interface RowMapper<T> {

    T map(ResultSet resultSet) throws SQLException;
}
