package com.littlehorizons.erp.dao;

/**
 * Full create, read, update and delete operations for editable entities.
 */
public interface CrudDao<T> extends ReadDao<T> {

    /** @return the generated id */
    int insert(T entity);

    /** @return {@code true} when a row was updated */
    boolean update(T entity);

    /** @return {@code true} when a row was deleted */
    boolean delete(int id);
}
