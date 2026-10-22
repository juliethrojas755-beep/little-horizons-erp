package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
import java.util.Optional;

/**
 * Acceso a datos de profesores (tabla {@code profesores}).
 */
public interface TeacherDao extends CrudDao<Teacher> {

    /**
     * Busca un profesor por su número de documento.
     */
    Optional<Teacher> findByDocument(String documentNumber);
}
