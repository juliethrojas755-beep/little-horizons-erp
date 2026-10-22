package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.TeacherAssignmentDao;
import com.littlehorizons.erp.model.TeacherAssignment;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link TeacherAssignmentDao} sobre la tabla {@code asignaciones_profesor}.
 * Todo el SQL de asignaciones vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class TeacherAssignmentDaoJdbc extends BaseJdbcDao implements TeacherAssignmentDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM asignaciones_profesor ";

    /**
     * Guarda una asignación nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(TeacherAssignment a) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO asignaciones_profesor (id_profesor, id_materia, anio_academico, seccion) VALUES (?, ?, ?, ?)",
                a.getTeacherId(), a.getSubjectId(), a.getAcademicYear(), a.getSection());
    }

    /**
     * Elimina una asignación por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int assignmentId) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM asignaciones_profesor WHERE id_asignacion = ?", assignmentId) > 0;
    }

    /**
     * Busca una asignación por su id.
     */
    @Override
    public Optional<TeacherAssignment> findById(int assignmentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_asignacion = ?", this::map, assignmentId);
    }

    /**
     * Busca por profesor.
     */
    @Override
    public List<TeacherAssignment> findByTeacher(int teacherId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_profesor = ? ORDER BY anio_academico DESC", this::map, teacherId);
    }

    /**
     * Busca el profesor encargado de una materia en un año (se toma el primero).
     */
    @Override
    public Optional<TeacherAssignment> findBySubjectAndYear(int subjectId, int academicYear) {
        // LIMIT 1: si hay varias secciones se toma la primera asignación
        return queryOne(SELECT + "WHERE id_materia = ? AND anio_academico = ? ORDER BY id_asignacion LIMIT 1",
                this::map, subjectId, academicYear);
    }

    /**
     * Devuelve todos los registros de asignaciones.
     */
    @Override
    public List<TeacherAssignment> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY anio_academico DESC, id_profesor", this::map);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto TeacherAssignment.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
