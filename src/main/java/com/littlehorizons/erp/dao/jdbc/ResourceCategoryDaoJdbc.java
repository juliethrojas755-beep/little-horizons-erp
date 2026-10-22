package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.ResourceCategoryDao;
import com.littlehorizons.erp.model.ResourceCategory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link ResourceCategoryDao} sobre la tabla {@code categorias_recurso}.
 * Todo el SQL de categorías de recursos vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class ResourceCategoryDaoJdbc extends BaseJdbcDao implements ResourceCategoryDao {

    /**
     * Busca una categoría por su id.
     */
    @Override
    public Optional<ResourceCategory> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne("SELECT * FROM categorias_recurso WHERE id_categoria = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de categorías de recursos.
     */
    @Override
    public List<ResourceCategory> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM categorias_recurso ORDER BY nombre_categoria", this::map);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto ResourceCategory.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private ResourceCategory map(ResultSet rs) throws SQLException {
        ResourceCategory category = new ResourceCategory();
        category.setId(rs.getInt("id_categoria"));
        category.setName(rs.getString("nombre_categoria"));
        category.setDescription(rs.getString("descripcion"));
        return category;
    }
}
