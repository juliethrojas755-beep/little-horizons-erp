package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.TeacherAssignment;
import java.util.List;
import java.util.Optional;

/**
 * Data access for teacher-subject assignments ({@code asignaciones_profesor}).
 */
public interface TeacherAssignmentDao {

    int insert(TeacherAssignment assignment);

    boolean delete(int assignmentId);

    Optional<TeacherAssignment> findById(int assignmentId);

    List<TeacherAssignment> findByTeacher(int teacherId);

    /** Teacher in charge of a subject in a year, used when a grade is recorded. */
    Optional<TeacherAssignment> findBySubjectAndYear(int subjectId, int academicYear);

    List<TeacherAssignment> findAll();
}
