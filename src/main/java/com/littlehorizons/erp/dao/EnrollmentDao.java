package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.enums.EnrollmentStatus;
import java.util.List;
import java.util.Optional;

/**
 * Data access for yearly enrollments ({@code matriculas}).
 */
public interface EnrollmentDao {

    int insert(Enrollment enrollment);

    boolean updateStatus(int enrollmentId, EnrollmentStatus status);

    List<Enrollment> findByStudent(int studentId);

    Optional<Enrollment> findByStudentAndYear(int studentId, int academicYear);

    List<Enrollment> findAll();
}
