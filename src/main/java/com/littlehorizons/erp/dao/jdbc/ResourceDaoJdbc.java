package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.enums.ResourceStatus;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link ResourceDao} sobre la tabla {@code recursos}.
 * Todo el SQL de recursos vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class ResourceDaoJdbc extends BaseJdbcDao implements ResourceDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM recursos ";

    /**
     * Busca un recurso por su id.
     */
    @Override
    public Optional<Resource> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_recurso = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de recursos.
     */
    @Override
    public List<Resource> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY codigo_recurso", this::map);
    }

    /**
     * Busca por código único.
     */
    @Override
    public Optional<Resource> findByCode(String code) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE codigo_recurso = ?", this::map, code);
    }

    /**
     * Busca los recursos que están en un estado.
     */
    @Override
    public List<Resource> findByStatus(ResourceStatus status) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE estado_recurso = ? ORDER BY codigo_recurso", this::map, status);
    }

    /**
     * Cambia el estado dentro de la transacción de un préstamo o una devolución.
     */
    @Override
    public void updateStatus(Connection connection, int resourceId, ResourceStatus status) throws SQLException {
        update(connection, "UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?", status, resourceId);
    }

    /**
     * Guarda un recurso nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Resource r) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, fecha_adquisicion, "
                        + "ubicacion_fisica, estado_recurso) VALUES (?, ?, ?, ?, ?, ?, ?)",
                r.getCode(), r.getName(), r.getCategoryId(), r.getDescription(), r.getAcquisitionDate(), r.getLocation(),
                r.getStatus());
    }

    /**
     * Actualiza los datos de un recurso.
     *
     * @return {@code true} si se modificó alguna fila
     */
    @Override
    public boolean update(Resource r) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE recursos SET nombre_recurso = ?, id_categoria = ?, descripcion = ?, ubicacion_fisica = ?, "
                        + "estado_recurso = ? WHERE id_recurso = ?",
                r.getName(), r.getCategoryId(), r.getDescription(), r.getLocation(), r.getStatus(), r.getId()) > 0;
    }

    /**
     * Elimina un recurso por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int id) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM recursos WHERE id_recurso = ?", id) > 0;
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Resource.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private Resource map(ResultSet rs) throws SQLException {
        Resource resource = new Resource();
        resource.setId(rs.getInt("id_recurso"));
        resource.setCode(rs.getString("codigo_recurso"));
        resource.setName(rs.getString("nombre_recurso"));
        resource.setCategoryId(rs.getInt("id_categoria"));
        resource.setDescription(rs.getString("descripcion"));
        resource.setAcquisitionDate(getDate(rs, "fecha_adquisicion"));
        resource.setLocation(rs.getString("ubicacion_fisica"));
        resource.setStatus(ResourceStatus.fromDbValue(rs.getString("estado_recurso")));
        return resource;
    }
}
