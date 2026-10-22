package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.enums.EnrollmentStatus;
import com.littlehorizons.erp.model.enums.Term;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementación JDBC de {@link SubjectEnrollmentDao} sobre la tabla {@code inscripciones_materia}.
 * Todo el SQL de inscripciones vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class SubjectEnrollmentDaoJdbc extends BaseJdbcDao implements SubjectEnrollmentDao {

    /**
     * Guarda una inscripción nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(SubjectEnrollment e) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO inscripciones_materia (id_estudiante, id_materia, anio_academico, periodo_academico, seccion, "
                        + "fecha_inscripcion, estado) VALUES (?, ?, ?, ?, ?, ?, ?)",
                e.getStudentId(), e.getSubjectId(), e.getAcademicYear(), e.getTerm(), e.getSection(),
                e.getEnrollmentDate(), e.getStatus());
    }

    /**
     * Busca los registros de un estudiante.
     */
    @Override
    public List<SubjectEnrollment> findByStudent(int studentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM inscripciones_materia WHERE id_estudiante = ? ORDER BY anio_academico DESC, periodo_academico",
                this::map, studentId);
    }

    /**
     * Busca las inscripciones de una materia en un año y período.
     */
    @Override
    public List<SubjectEnrollment> findBySubjectAndTerm(int subjectId, int academicYear, Term term) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM inscripciones_materia WHERE id_materia = ? AND anio_academico = ? AND periodo_academico = ?",
                this::map, subjectId, academicYear, term);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto SubjectEnrollment.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
