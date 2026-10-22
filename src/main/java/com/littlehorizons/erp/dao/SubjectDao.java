package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Subject;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de materias (tabla {@code materias}).
 */
public interface SubjectDao extends CrudDao<Subject> {

    /**
     * Materias de un grado.
     */
    List<Subject> findByGrade(int gradeLevelId);

    /**
     * Busca una materia por su código único (por ejemplo MAT-5).
     */
    Optional<Subject> findByCode(String code);
}
