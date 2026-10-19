package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.enums.EnrollmentStatus;
import com.littlehorizons.erp.model.enums.Term;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC implementation of {@link SubjectEnrollmentDao} over {@code inscripciones_materia}.
 */
public class SubjectEnrollmentDaoJdbc extends BaseJdbcDao implements SubjectEnrollmentDao {

    @Override
    public int insert(SubjectEnrollment e) {
        return insert("INSERT INTO inscripciones_materia (id_estudiante, id_materia, anio_academico, periodo_academico, seccion, "
                        + "fecha_inscripcion, estado) VALUES (?, ?, ?, ?, ?, ?, ?)",
                e.getStudentId(), e.getSubjectId(), e.getAcademicYear(), e.getTerm(), e.getSection(),
                e.getEnrollmentDate(), e.getStatus());
    }

    @Override
    public List<SubjectEnrollment> findByStudent(int studentId) {
        return queryList("SELECT * FROM inscripciones_materia WHERE id_estudiante = ? ORDER BY anio_academico DESC, periodo_academico",
                this::map, studentId);
    }

    @Override
    public List<SubjectEnrollment> findBySubjectAndTerm(int subjectId, int academicYear, Term term) {
        return queryList("SELECT * FROM inscripciones_materia WHERE id_materia = ? AND anio_academico = ? AND periodo_academico = ?",
                this::map, subjectId, academicYear, term);
    }

    private SubjectEnrollment map(ResultSet rs) throws SQLException {
        SubjectEnrollment enrollment = new SubjectEnrollment();
        enrollment.setId(rs.getInt("id_inscripcion"));
        enrollment.setStudentId(rs.getInt("id_estudiante"));
        enrollment.setSubjectId(rs.getInt("id_materia"));
        enrollment.setAcademicYear(rs.getInt("anio_academico"));
        enrollment.setTerm(Term.fromDbValue(rs.getString("periodo_academico")));
        enrollment.setSection(rs.getString("seccion"));
        enrollment.setEnrollmentDate(getDate(rs, "fecha_inscripcion"));
        enrollment.setStatus(EnrollmentStatus.fromDbValue(rs.getString("estado")));
        return enrollment;
    }
}
