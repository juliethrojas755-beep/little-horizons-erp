package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
import java.util.Optional;

/**
 * Data access for teachers ({@code profesores}).
 */
public interface TeacherDao extends CrudDao<Teacher> {

    Optional<Teacher> findByDocument(String documentNumber);
}
