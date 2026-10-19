package com.littlehorizons.erp.dao;

import java.util.List;
import java.util.Optional;

/**
 * Read-only operations. Catalogs (grades, resource categories) only implement this one,
 * so nobody can call insert or delete on them (Interface Segregation).
 */
public interface ReadDao<T> {

    Optional<T> findById(int id);

    List<T> findAll();
}
