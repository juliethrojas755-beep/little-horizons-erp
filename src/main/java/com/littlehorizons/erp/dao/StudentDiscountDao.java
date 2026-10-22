package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.StudentDiscount;
import java.util.List;

/**
 * Acceso a datos de descuentos por estudiante (tabla {@code descuentos_estudiante}).
 */
public interface StudentDiscountDao {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(StudentDiscount discount);

    /**
     * Descuentos de un estudiante.
     */
    List<StudentDiscount> findByStudent(int studentId);

    /**
     * Descuentos de un estudiante para un concepto específico.
     */
    List<StudentDiscount> findByStudentAndConcept(int studentId, int conceptId);
}
