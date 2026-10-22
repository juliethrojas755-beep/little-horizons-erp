package com.littlehorizons.erp.dao;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones de SOLO LECTURA.
 * Los catálogos (grados, categorías de recursos) implementan únicamente esta interfaz,
 * así nadie puede llamar insert ni delete sobre ellos (principio de Segregación de Interfaces).
 *
 * @param <T> entidad que maneja el DAO
 */
public interface ReadDao<T> {

    /**
     * Busca un registro por su id.
     *
     * @return el registro dentro de un Optional (vacío si no existe, así se evita devolver null)
     */
    Optional<T> findById(int id);

    /** Devuelve todos los registros de la tabla. */
    List<T> findAll();
}
