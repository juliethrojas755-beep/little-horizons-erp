package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.ScheduleDao;
import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.enums.WeekDay;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementación JDBC de {@link ScheduleDao} sobre la tabla {@code horarios}.
 * Todo el SQL de bloques de horario vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class ScheduleDaoJdbc extends BaseJdbcDao implements ScheduleDao {

    // Orden común del listado (por apellidos y luego por nombre)
    private static final String ORDER = " ORDER BY FIELD(h.dia_semana, 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'), h.hora_inicio";

    /**
     * Guarda un bloque de horario nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Schedule s) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO horarios (id_asignacion, dia_semana, hora_inicio, hora_fin, salon) VALUES (?, ?, ?, ?, ?)",
                s.getAssignmentId(), s.getWeekDay(), s.getStartTime(), s.getEndTime(), s.getClassroom());
    }

    /**
     * Elimina un bloque de horario por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int scheduleId) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM horarios WHERE id_horario = ?", scheduleId) > 0;
    }

    /**
     * Busca los bloques de una asignación.
     */
    @Override
    public List<Schedule> findByAssignment(int assignmentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT h.* FROM horarios h WHERE h.id_asignacion = ?" + ORDER, this::map, assignmentId);
    }

    /**
     * Busca por profesor.
     */
    @Override
    public List<Schedule> findByTeacher(int teacherId) {
        // JOIN: de los bloques de horario pasa a la asignación para conocer al profesor
        return queryList("SELECT h.* FROM horarios h JOIN asignaciones_profesor a ON a.id_asignacion = h.id_asignacion "
                + "WHERE a.id_profesor = ?" + ORDER, this::map, teacherId);
    }

    /**
     * Busca los bloques de un salón.
     */
    @Override
    public List<Schedule> findByClassroom(String classroom) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT h.* FROM horarios h WHERE h.salon = ?" + ORDER, this::map, classroom);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Schedule.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private Schedule map(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();
        schedule.setId(rs.getInt("id_horario"));
        schedule.setAssignmentId(rs.getInt("id_asignacion"));
        schedule.setWeekDay(WeekDay.fromDbValue(rs.getString("dia_semana")));
        schedule.setStartTime(getTime(rs, "hora_inicio"));
        schedule.setEndTime(getTime(rs, "hora_fin"));
        schedule.setClassroom(rs.getString("salon"));
        return schedule;
    }
}
