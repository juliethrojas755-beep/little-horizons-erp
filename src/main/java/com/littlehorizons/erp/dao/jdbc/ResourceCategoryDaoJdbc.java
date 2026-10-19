package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.ResourceCategoryDao;
import com.littlehorizons.erp.model.ResourceCategory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ResourceCategoryDao} over {@code categorias_recurso}.
 */
public class ResourceCategoryDaoJdbc extends BaseJdbcDao implements ResourceCategoryDao {

    @Override
    public Optional<ResourceCategory> findById(int id) {
        return queryOne("SELECT * FROM categorias_recurso WHERE id_categoria = ?", this::map, id);
    }

    @Override
    public List<ResourceCategory> findAll() {
        return queryList("SELECT * FROM categorias_recurso ORDER BY nombre_categoria", this::map);
    }

    private ResourceCategory map(ResultSet rs) throws SQLException {
        ResourceCategory category = new ResourceCategory();
        category.setId(rs.getInt("id_categoria"));
        category.setName(rs.getString("nombre_categoria"));
        category.setDescription(rs.getString("descripcion"));
        return category;
    }
}
