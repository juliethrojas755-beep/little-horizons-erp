package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.enums.Term;
import java.util.List;

/**
 * Acceso a datos de inscripciones en materias por período (tabla {@code inscripciones_materia}).
 */
public interface SubjectEnrollmentDao {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(SubjectEnrollment enrollment);

    /**
     * Inscripciones de un estudiante.
     */
    List<SubjectEnrollment> findByStudent(int studentId);

    /**
     * Inscripciones de una materia en un año y período.
     */
    List<SubjectEnrollment> findBySubjectAndTerm(int subjectId, int academicYear, Term term);
}
