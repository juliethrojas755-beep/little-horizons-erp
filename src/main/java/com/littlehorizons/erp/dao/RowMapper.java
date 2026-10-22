package com.littlehorizons.erp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Convierte la fila actual de un {@link ResultSet} (resultado de un SELECT) en un objeto Java.
 * <p>
 * Es una interfaz funcional (un solo método), así que cada DAO la entrega como lambda o como
 * referencia a método: {@code this::map}. Esto evita repetir el ciclo de lectura en cada consulta.
 * </p>
 *
 * @param <T> tipo de objeto que se construye (Student, Payment...)
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Lee las columnas de la fila actual y arma el objeto.
     *
     * @param resultSet resultado ubicado en la fila que se debe leer
     * @throws SQLException si una columna no existe o no se puede leer
     */
    T map(ResultSet resultSet) throws SQLException;
}
