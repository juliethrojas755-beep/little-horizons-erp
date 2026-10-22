package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.EventParticipationDao;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.enums.EventPosition;
import com.littlehorizons.erp.model.enums.ParticipationRole;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementación JDBC de {@link EventParticipationDao} sobre la tabla {@code participantes_evento}.
 * Todo el SQL de participaciones vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class EventParticipationDaoJdbc extends BaseJdbcDao implements EventParticipationDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM participantes_evento ";

    /**
     * Guarda una participación nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(EventParticipation p) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, posicion_obtenida, "
                        + "observaciones) VALUES (?, ?, ?, ?, ?)",
                p.getEventId(), p.getStudentId(), p.getRole(), p.getPosition(), p.getNotes());
    }

    /**
     * Asigna la posición obtenida.
     */
    @Override
    public boolean updatePosition(int participationId, EventPosition position) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE participantes_evento SET posicion_obtenida = ? WHERE id_participacion = ?",
                position, participationId) > 0;
    }

    /**
     * Busca los participantes de un evento.
     */
    @Override
    public List<EventParticipation> findByEvent(int eventId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_evento = ? ORDER BY posicion_obtenida", this::map, eventId);
    }

    /**
     * Busca los registros de un estudiante.
     */
    @Override
    public List<EventParticipation> findByStudent(int studentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_estudiante = ?", this::map, studentId);
    }

    /**
     * Devuelve todos los registros de participaciones.
     */
    @Override
    public List<EventParticipation> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY id_evento", this::map);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto EventParticipation.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
