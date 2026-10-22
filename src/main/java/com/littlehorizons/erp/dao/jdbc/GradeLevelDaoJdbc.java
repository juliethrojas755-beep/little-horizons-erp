package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.GradeLevelDao;
import com.littlehorizons.erp.model.GradeLevel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link GradeLevelDao} sobre la tabla {@code grados}.
 * Todo el SQL de grados vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class GradeLevelDaoJdbc extends BaseJdbcDao implements GradeLevelDao {

    /**
     * Busca un grado por su id.
     */
    @Override
    public Optional<GradeLevel> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne("SELECT * FROM grados WHERE id_grado = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de grados.
     */
    @Override
    public List<GradeLevel> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM grados ORDER BY nivel_numerico", this::map);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto GradeLevel.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private GradeLevel map(ResultSet rs) throws SQLException {
        GradeLevel grade = new GradeLevel();
        grade.setId(rs.getInt("id_grado"));
        grade.setName(rs.getString("nombre_grado"));
        grade.setNumericLevel(rs.getInt("nivel_numerico"));
        return grade;
    }
}
