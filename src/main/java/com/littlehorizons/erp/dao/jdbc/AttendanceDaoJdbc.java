package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.enums.AttendanceStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * JDBC implementation of {@link AttendanceDao} over {@code asistencias}.
 */
public class AttendanceDaoJdbc extends BaseJdbcDao implements AttendanceDao {

    @Override
    public int insert(Attendance a) {
        return insert("INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, observaciones, "
                        + "registrado_por) VALUES (?, ?, ?, ?, ?, ?)",
                a.getStudentId(), a.getSubjectId(), a.getDate(), a.getStatus(), a.getNotes(), a.getRecordedByTeacherId());
    }

    @Override
    public List<Attendance> findByStudent(int studentId) {
        return queryList("SELECT * FROM asistencias WHERE id_estudiante = ? ORDER BY fecha_asistencia DESC", this::map, studentId);
    }

    @Override
    public List<Attendance> findBetween(LocalDate from, LocalDate to) {
        return queryList("SELECT * FROM asistencias WHERE fecha_asistencia BETWEEN ? AND ? ORDER BY fecha_asistencia",
                this::map, from, to);
    }

    private Attendance map(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt("id_asistencia"));
        attendance.setStudentId(rs.getInt("id_estudiante"));
        attendance.setSubjectId(rs.getInt("id_materia"));
        attendance.setDate(getDate(rs, "fecha_asistencia"));
        attendance.setStatus(AttendanceStatus.fromDbValue(rs.getString("estado_asistencia")));
        attendance.setNotes(rs.getString("observaciones"));
        attendance.setRecordedByTeacherId(getNullableInt(rs, "registrado_por"));
        return attendance;
    }
}
