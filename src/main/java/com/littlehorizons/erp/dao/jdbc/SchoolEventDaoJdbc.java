package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.SchoolEventDao;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.model.enums.EventType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link SchoolEventDao} over {@code eventos}.
 */
public class SchoolEventDaoJdbc extends BaseJdbcDao implements SchoolEventDao {

    @Override
    public Optional<SchoolEvent> findById(int id) {
        return queryOne("SELECT * FROM eventos WHERE id_evento = ?", this::map, id);
    }

    @Override
    public List<SchoolEvent> findAll() {
        return queryList("SELECT * FROM eventos ORDER BY fecha_evento DESC", this::map);
    }

    @Override
    public int insert(SchoolEvent e) {
        return insert("INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, tiene_competencia, "
                        + "id_organizador, esta_activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                e.getName(), e.getDescription(), e.getType(), e.getDate(), e.getPlace(), e.isCompetitive(),
                e.getOrganizerTeacherId(), e.isActive());
    }

    @Override
    public boolean update(SchoolEvent e) {
        return update("UPDATE eventos SET nombre_evento = ?, descripcion = ?, tipo_evento = ?, fecha_evento = ?, lugar = ?, "
                        + "tiene_competencia = ?, id_organizador = ?, esta_activo = ? WHERE id_evento = ?",
                e.getName(), e.getDescription(), e.getType(), e.getDate(), e.getPlace(), e.isCompetitive(),
                e.getOrganizerTeacherId(), e.isActive(), e.getId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return update("DELETE FROM eventos WHERE id_evento = ?", id) > 0;
    }

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
