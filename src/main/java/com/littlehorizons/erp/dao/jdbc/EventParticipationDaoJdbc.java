package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.EventParticipationDao;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.enums.EventPosition;
import com.littlehorizons.erp.model.enums.ParticipationRole;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC implementation of {@link EventParticipationDao} over {@code participantes_evento}.
 */
public class EventParticipationDaoJdbc extends BaseJdbcDao implements EventParticipationDao {

    private static final String SELECT = "SELECT * FROM participantes_evento ";

    @Override
    public int insert(EventParticipation p) {
        return insert("INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, posicion_obtenida, "
                        + "observaciones) VALUES (?, ?, ?, ?, ?)",
                p.getEventId(), p.getStudentId(), p.getRole(), p.getPosition(), p.getNotes());
    }

    @Override
    public boolean updatePosition(int participationId, EventPosition position) {
        return update("UPDATE participantes_evento SET posicion_obtenida = ? WHERE id_participacion = ?",
                position, participationId) > 0;
    }

    @Override
    public List<EventParticipation> findByEvent(int eventId) {
        return queryList(SELECT + "WHERE id_evento = ? ORDER BY posicion_obtenida", this::map, eventId);
    }

    @Override
    public List<EventParticipation> findByStudent(int studentId) {
        return queryList(SELECT + "WHERE id_estudiante = ?", this::map, studentId);
    }

    @Override
    public List<EventParticipation> findAll() {
        return queryList(SELECT + "ORDER BY id_evento", this::map);
    }

    private EventParticipation map(ResultSet rs) throws SQLException {
        EventParticipation participation = new EventParticipation();
        participation.setId(rs.getInt("id_participacion"));
        participation.setEventId(rs.getInt("id_evento"));
        participation.setStudentId(rs.getInt("id_estudiante"));
        participation.setRole(ParticipationRole.fromDbValue(rs.getString("rol_participacion")));
        participation.setPosition(EventPosition.fromDbValue(rs.getString("posicion_obtenida")));
        participation.setNotes(rs.getString("observaciones"));
        return participation;
    }
}
