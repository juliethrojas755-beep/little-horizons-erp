package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.SchoolEventDao;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.model.enums.EventType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link SchoolEventDao} sobre la tabla {@code eventos}.
 * Todo el SQL de eventos vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class SchoolEventDaoJdbc extends BaseJdbcDao implements SchoolEventDao {

    /**
     * Busca un evento por su id.
     */
    @Override
    public Optional<SchoolEvent> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne("SELECT * FROM eventos WHERE id_evento = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de eventos.
     */
    @Override
    public List<SchoolEvent> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM eventos ORDER BY fecha_evento DESC", this::map);
    }

    /**
     * Guarda un evento nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(SchoolEvent e) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, tiene_competencia, "
                        + "id_organizador, esta_activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                e.getName(), e.getDescription(), e.getType(), e.getDate(), e.getPlace(), e.isCompetitive(),
                e.getOrganizerTeacherId(), e.isActive());
    }

    /**
     * Actualiza los datos de un evento.
     *
     * @return {@code true} si se modificó alguna fila
     */
    @Override
    public boolean update(SchoolEvent e) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE eventos SET nombre_evento = ?, descripcion = ?, tipo_evento = ?, fecha_evento = ?, lugar = ?, "
                        + "tiene_competencia = ?, id_organizador = ?, esta_activo = ? WHERE id_evento = ?",
                e.getName(), e.getDescription(), e.getType(), e.getDate(), e.getPlace(), e.isCompetitive(),
                e.getOrganizerTeacherId(), e.isActive(), e.getId()) > 0;
    }

    /**
     * Elimina un evento por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int id) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM eventos WHERE id_evento = ?", id) > 0;
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto SchoolEvent.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private SchoolEvent map(ResultSet rs) throws SQLException {
        SchoolEvent event = new SchoolEvent();
        event.setId(rs.getInt("id_evento"));
        event.setName(rs.getString("nombre_evento"));
        event.setDescription(rs.getString("descripcion"));
        event.setType(EventType.fromDbValue(rs.getString("tipo_evento")));
        event.setDate(getDate(rs, "fecha_evento"));
        event.setPlace(rs.getString("lugar"));
        event.setCompetitive(rs.getBoolean("tiene_competencia"));
        event.setOrganizerTeacherId(getNullableInt(rs, "id_organizador"));
        event.setActive(rs.getBoolean("esta_activo"));
        return event;
    }
}
