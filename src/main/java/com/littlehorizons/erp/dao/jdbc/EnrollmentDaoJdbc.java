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
 * JDBC implementation of {@link EnrollmentDao} over {@code matriculas}.
 */
public class EnrollmentDaoJdbc extends BaseJdbcDao implements EnrollmentDao {

    @Override
    public int insert(Enrollment e) {
        return insert("INSERT INTO matriculas (id_estudiante, id_grado, anio_academico, estado_matricula, fecha_matricula, observaciones) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                e.getStudentId(), e.getGradeLevelId(), e.getAcademicYear(), e.getStatus(), e.getEnrollmentDate(), e.getNotes());
    }

    @Override
    public boolean updateStatus(int enrollmentId, EnrollmentStatus status) {
        LocalDate withdrawal = status == EnrollmentStatus.WITHDRAWN ? LocalDate.now() : null;
        return update("UPDATE matriculas SET estado_matricula = ?, fecha_retiro = ? WHERE id_matricula = ?",
                status, withdrawal, enrollmentId) > 0;
    }

    @Override
    public List<Enrollment> findByStudent(int studentId) {
        return queryList("SELECT * FROM matriculas WHERE id_estudiante = ? ORDER BY anio_academico DESC", this::map, studentId);
    }

    @Override
    public Optional<Enrollment> findByStudentAndYear(int studentId, int academicYear) {
        return queryOne("SELECT * FROM matriculas WHERE id_estudiante = ? AND anio_academico = ?", this::map, studentId, academicYear);
    }

    @Override
    public List<Enrollment> findAll() {
        return queryList("SELECT * FROM matriculas ORDER BY anio_academico DESC, id_grado", this::map);
    }

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
