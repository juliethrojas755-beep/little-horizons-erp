package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.StudentDiscount;
import java.util.List;

/**
 * Data access for per-student discounts ({@code descuentos_estudiante}).
 */
public interface StudentDiscountDao {

    int insert(StudentDiscount discount);

    List<StudentDiscount> findByStudent(int studentId);

    List<StudentDiscount> findByStudentAndConcept(int studentId, int conceptId);
}
