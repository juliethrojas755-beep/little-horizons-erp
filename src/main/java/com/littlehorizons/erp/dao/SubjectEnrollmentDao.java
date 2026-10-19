package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.enums.Term;
import java.util.List;

/**
 * Data access for subject enrollments per term ({@code inscripciones_materia}).
 */
public interface SubjectEnrollmentDao {

    int insert(SubjectEnrollment enrollment);

    List<SubjectEnrollment> findByStudent(int studentId);

    List<SubjectEnrollment> findBySubjectAndTerm(int subjectId, int academicYear, Term term);
}
