package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Student;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de estudiantes (tabla {@code estudiantes}).
 * Solo declara QUÉ se puede hacer; el SQL está en StudentDaoJdbc.
 */
public interface StudentDao extends CrudDao<Student> {

    /**
     * Busca un estudiante por su número de documento (es único).
     */
    Optional<Student> findByDocument(String documentNumber);

    /**
     * Busca por cualquier parte del nombre o apellidos.
     */
    List<Student> searchByName(String text);

    /**
     * Estudiantes que tienen a ese acudiente (hermanos).
     */
    List<Student> findByGuardian(int guardianId);

    /**
     * Estudiantes matriculados en un grado y año.
     */
    List<Student> findByGrade(int gradeLevelId, int academicYear);
}
