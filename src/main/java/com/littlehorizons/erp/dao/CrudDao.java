package com.littlehorizons.erp.dao;

/**
 * Operaciones completas de crear, leer, actualizar y eliminar (CRUD).
 * Extiende {@link ReadDao}, por lo que también hereda findById y findAll.
 *
 * @param <T> entidad que maneja el DAO
 */
public interface CrudDao<T> extends ReadDao<T> {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(T entity);

    /**
     * Actualiza un registro existente.
     *
     * @return {@code true} si se actualizó alguna fila
     */
    boolean update(T entity);

    /**
     * Elimina un registro.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    boolean delete(int id);
}
