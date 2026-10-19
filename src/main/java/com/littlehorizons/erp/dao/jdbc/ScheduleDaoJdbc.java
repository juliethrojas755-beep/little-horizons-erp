package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.ScheduleDao;
import com.littlehorizons.erp.model.Schedule;
import com.littlehorizons.erp.model.enums.WeekDay;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC implementation of {@link ScheduleDao} over {@code horarios}.
 */
public class ScheduleDaoJdbc extends BaseJdbcDao implements ScheduleDao {

    private static final String ORDER = " ORDER BY FIELD(h.dia_semana, 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'), h.hora_inicio";

    @Override
    public int insert(Schedule s) {
        return insert("INSERT INTO horarios (id_asignacion, dia_semana, hora_inicio, hora_fin, salon) VALUES (?, ?, ?, ?, ?)",
                s.getAssignmentId(), s.getWeekDay(), s.getStartTime(), s.getEndTime(), s.getClassroom());
    }

    @Override
    public boolean delete(int scheduleId) {
        return update("DELETE FROM horarios WHERE id_horario = ?", scheduleId) > 0;
    }

    @Override
    public List<Schedule> findByAssignment(int assignmentId) {
        return queryList("SELECT h.* FROM horarios h WHERE h.id_asignacion = ?" + ORDER, this::map, assignmentId);
    }

    @Override
    public List<Schedule> findByTeacher(int teacherId) {
        return queryList("SELECT h.* FROM horarios h JOIN asignaciones_profesor a ON a.id_asignacion = h.id_asignacion "
                + "WHERE a.id_profesor = ?" + ORDER, this::map, teacherId);
    }

    @Override
    public List<Schedule> findByClassroom(String classroom) {
        return queryList("SELECT h.* FROM horarios h WHERE h.salon = ?" + ORDER, this::map, classroom);
    }

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
