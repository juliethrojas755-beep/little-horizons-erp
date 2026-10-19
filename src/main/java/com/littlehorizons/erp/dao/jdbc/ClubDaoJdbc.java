package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.model.Club;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ClubDao} over {@code clubes}.
 */
public class ClubDaoJdbc extends BaseJdbcDao implements ClubDao {

    private static final String SELECT = "SELECT * FROM clubes ";

    @Override
    public Optional<Club> findById(int id) {
        return queryOne(SELECT + "WHERE id_club = ?", this::map, id);
    }

    @Override
    public List<Club> findAll() {
        return queryList(SELECT + "ORDER BY nombre_club", this::map);
    }

    @Override
    public int countActiveMembers(int clubId) {
        return queryInt("SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'", clubId);
    }

    @Override
    public int insert(Club c) {
        return insert("INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, "
                        + "esta_activo) VALUES (?, ?, ?, ?, ?, ?)",
                c.getName(), c.getDescription(), c.getSupervisorTeacherId(), c.getMaxCapacity(), c.getMeetingSchedule(), c.isActive());
    }

    @Override
    public boolean update(Club c) {
        return update("UPDATE clubes SET nombre_club = ?, descripcion = ?, id_profesor_supervisor = ?, capacidad_maxima = ?, "
                        + "horario_reunion = ?, esta_activo = ? WHERE id_club = ?",
                c.getName(), c.getDescription(), c.getSupervisorTeacherId(), c.getMaxCapacity(), c.getMeetingSchedule(),
                c.isActive(), c.getId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return update("DELETE FROM clubes WHERE id_club = ?", id) > 0;
    }

    private Club map(ResultSet rs) throws SQLException {
        Club club = new Club();
        club.setId(rs.getInt("id_club"));
        club.setName(rs.getString("nombre_club"));
        club.setDescription(rs.getString("descripcion"));
        club.setSupervisorTeacherId(getNullableInt(rs, "id_profesor_supervisor"));
        club.setMaxCapacity(rs.getInt("capacidad_maxima"));
        club.setMeetingSchedule(rs.getString("horario_reunion"));
        club.setActive(rs.getBoolean("esta_activo"));
        return club;
    }
}
