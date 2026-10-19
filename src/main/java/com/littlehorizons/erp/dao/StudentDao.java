package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Student;
import java.util.List;
import java.util.Optional;

/**
 * Data access for students ({@code estudiantes}).
 */
public interface StudentDao extends CrudDao<Student> {

    Optional<Student> findByDocument(String documentNumber);

    /** Searches by any part of the first or last names. */
    List<Student> searchByName(String text);

    List<Student> findByGuardian(int guardianId);

    /** Students with an enrollment in the given grade and year. */
    List<Student> findByGrade(int gradeLevelId, int academicYear);
}
