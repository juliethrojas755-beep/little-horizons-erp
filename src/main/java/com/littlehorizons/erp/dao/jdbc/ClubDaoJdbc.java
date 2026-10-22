package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.model.Club;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link ClubDao} sobre la tabla {@code clubes}.
 * Todo el SQL de clubes vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class ClubDaoJdbc extends BaseJdbcDao implements ClubDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM clubes ";

    /**
     * Busca un club por su id.
     */
    @Override
    public Optional<Club> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_club = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de clubes.
     */
    @Override
    public List<Club> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY nombre_club", this::map);
    }

    /**
     * Cuenta los miembros activos del club; se compara con el cupo máximo.
     */
    @Override
    public int countActiveMembers(int clubId) {
        return queryInt("SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'", clubId);
    }

    /**
     * Guarda un club nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Club c) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, "
                        + "esta_activo) VALUES (?, ?, ?, ?, ?, ?)",
                c.getName(), c.getDescription(), c.getSupervisorTeacherId(), c.getMaxCapacity(), c.getMeetingSchedule(), c.isActive());
    }

    /**
     * Actualiza los datos de un club.
     *
     * @return {@code true} si se modificó alguna fila
     */
    @Override
    public boolean update(Club c) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE clubes SET nombre_club = ?, descripcion = ?, id_profesor_supervisor = ?, capacidad_maxima = ?, "
                        + "horario_reunion = ?, esta_activo = ? WHERE id_club = ?",
                c.getName(), c.getDescription(), c.getSupervisorTeacherId(), c.getMaxCapacity(), c.getMeetingSchedule(),
                c.isActive(), c.getId()) > 0;
    }

    /**
     * Elimina un club por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int id) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM clubes WHERE id_club = ?", id) > 0;
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Club.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
