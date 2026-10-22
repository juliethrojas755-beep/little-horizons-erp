package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.GradeLevel;

/**
 * Catálogo de solo lectura de los grados (tabla {@code grados}).
 * Extiende ReadDao y no CrudDao: nadie puede insertar ni borrar grados desde el programa.
 */
public interface GradeLevelDao extends ReadDao<GradeLevel> {
}
