package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.enums.EnrollmentStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link EnrollmentDao} sobre la tabla {@code matriculas}.
 * Todo el SQL de matrículas vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class EnrollmentDaoJdbc extends BaseJdbcDao implements EnrollmentDao {

    /**
     * Guarda una matrícula nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Enrollment e) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO matriculas (id_estudiante, id_grado, anio_academico, estado_matricula, fecha_matricula, observaciones) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                e.getStudentId(), e.getGradeLevelId(), e.getAcademicYear(), e.getStatus(), e.getEnrollmentDate(), e.getNotes());
    }

    /**
     * Cambia el estado.
     */
    @Override
    public boolean updateStatus(int enrollmentId, EnrollmentStatus status) {
        LocalDate withdrawal = status == EnrollmentStatus.WITHDRAWN ? LocalDate.now() : null;
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE matriculas SET estado_matricula = ?, fecha_retiro = ? WHERE id_matricula = ?",
                status, withdrawal, enrollmentId) > 0;
    }

    /**
     * Busca los registros de un estudiante.
     */
    @Override
    public List<Enrollment> findByStudent(int studentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM matriculas WHERE id_estudiante = ? ORDER BY anio_academico DESC", this::map, studentId);
    }

    /**
     * Busca la matrícula de un estudiante en un año (solo hay una por año).
     */
    @Override
    public Optional<Enrollment> findByStudentAndYear(int studentId, int academicYear) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne("SELECT * FROM matriculas WHERE id_estudiante = ? AND anio_academico = ?", this::map, studentId, academicYear);
    }

    /**
     * Devuelve todos los registros de matrículas.
     */
    @Override
    public List<Enrollment> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM matriculas ORDER BY anio_academico DESC, id_grado", this::map);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Enrollment.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private Enrollment map(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(rs.getInt("id_matricula"));
        enrollment.setStudentId(rs.getInt("id_estudiante"));
        enrollment.setGradeLevelId(rs.getInt("id_grado"));
        enrollment.setAcademicYear(rs.getInt("anio_academico"));
        enrollment.setStatus(EnrollmentStatus.fromDbValue(rs.getString("estado_matricula")));
        enrollment.setEnrollmentDate(getDate(rs, "fecha_matricula"));
        enrollment.setWithdrawalDate(getDate(rs, "fecha_retiro"));
        enrollment.setNotes(rs.getString("observaciones"));
        return enrollment;
    }
}
