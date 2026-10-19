package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Subject;
import java.util.List;
import java.util.Optional;

/**
 * Data access for subjects ({@code materias}).
 */
public interface SubjectDao extends CrudDao<Subject> {

    List<Subject> findByGrade(int gradeLevelId);

    Optional<Subject> findByCode(String code);
}
