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
 * JDBC implementation of {@link ResourceDao} over {@code recursos}.
 */
public class ResourceDaoJdbc extends BaseJdbcDao implements ResourceDao {

    private static final String SELECT = "SELECT * FROM recursos ";

    @Override
    public Optional<Resource> findById(int id) {
        return queryOne(SELECT + "WHERE id_recurso = ?", this::map, id);
    }

    @Override
    public List<Resource> findAll() {
        return queryList(SELECT + "ORDER BY codigo_recurso", this::map);
    }

    @Override
    public Optional<Resource> findByCode(String code) {
        return queryOne(SELECT + "WHERE codigo_recurso = ?", this::map, code);
    }

    @Override
    public List<Resource> findByStatus(ResourceStatus status) {
        return queryList(SELECT + "WHERE estado_recurso = ? ORDER BY codigo_recurso", this::map, status);
    }

    @Override
    public void updateStatus(Connection connection, int resourceId, ResourceStatus status) throws SQLException {
        update(connection, "UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?", status, resourceId);
    }

    @Override
    public int insert(Resource r) {
        return insert("INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, fecha_adquisicion, "
                        + "ubicacion_fisica, estado_recurso) VALUES (?, ?, ?, ?, ?, ?, ?)",
                r.getCode(), r.getName(), r.getCategoryId(), r.getDescription(), r.getAcquisitionDate(), r.getLocation(),
                r.getStatus());
    }

    @Override
    public boolean update(Resource r) {
        return update("UPDATE recursos SET nombre_recurso = ?, id_categoria = ?, descripcion = ?, ubicacion_fisica = ?, "
                        + "estado_recurso = ? WHERE id_recurso = ?",
                r.getName(), r.getCategoryId(), r.getDescription(), r.getLocation(), r.getStatus(), r.getId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return update("DELETE FROM recursos WHERE id_recurso = ?", id) > 0;
    }

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
