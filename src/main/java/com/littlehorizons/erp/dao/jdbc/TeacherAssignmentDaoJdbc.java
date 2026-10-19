package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.TeacherAssignmentDao;
import com.littlehorizons.erp.model.TeacherAssignment;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link TeacherAssignmentDao} over {@code asignaciones_profesor}.
 */
public class TeacherAssignmentDaoJdbc extends BaseJdbcDao implements TeacherAssignmentDao {

    private static final String SELECT = "SELECT * FROM asignaciones_profesor ";

    @Override
    public int insert(TeacherAssignment a) {
        return insert("INSERT INTO asignaciones_profesor (id_profesor, id_materia, anio_academico, seccion) VALUES (?, ?, ?, ?)",
                a.getTeacherId(), a.getSubjectId(), a.getAcademicYear(), a.getSection());
    }

    @Override
    public boolean delete(int assignmentId) {
        return update("DELETE FROM asignaciones_profesor WHERE id_asignacion = ?", assignmentId) > 0;
    }

    @Override
    public Optional<TeacherAssignment> findById(int assignmentId) {
        return queryOne(SELECT + "WHERE id_asignacion = ?", this::map, assignmentId);
    }

    @Override
    public List<TeacherAssignment> findByTeacher(int teacherId) {
        return queryList(SELECT + "WHERE id_profesor = ? ORDER BY anio_academico DESC", this::map, teacherId);
    }

    @Override
    public Optional<TeacherAssignment> findBySubjectAndYear(int subjectId, int academicYear) {
        return queryOne(SELECT + "WHERE id_materia = ? AND anio_academico = ? ORDER BY id_asignacion LIMIT 1",
                this::map, subjectId, academicYear);
    }

    @Override
    public List<TeacherAssignment> findAll() {
        return queryList(SELECT + "ORDER BY anio_academico DESC, id_profesor", this::map);
    }

    private TeacherAssignment map(ResultSet rs) throws SQLException {
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setId(rs.getInt("id_asignacion"));
        assignment.setTeacherId(rs.getInt("id_profesor"));
        assignment.setSubjectId(rs.getInt("id_materia"));
        assignment.setAcademicYear(rs.getInt("anio_academico"));
        assignment.setSection(rs.getString("seccion"));
        return assignment;
    }
}
