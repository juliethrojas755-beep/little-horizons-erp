package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.enums.EnrollmentStatus;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de matrículas anuales (tabla {@code matriculas}).
 */
public interface EnrollmentDao {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(Enrollment enrollment);

    /**
     * Cambia el estado de la matrícula.
     */
    boolean updateStatus(int enrollmentId, EnrollmentStatus status);

    /**
     * Matrículas de un estudiante, la más reciente primero.
     */
    List<Enrollment> findByStudent(int studentId);

    /**
     * Matrícula de un estudiante en un año (solo puede haber una).
     */
    Optional<Enrollment> findByStudentAndYear(int studentId, int academicYear);

    /**
     * Devuelve todos los registros.
     */
    List<Enrollment> findAll();
}
