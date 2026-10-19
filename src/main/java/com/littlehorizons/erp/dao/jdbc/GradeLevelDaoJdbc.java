package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.GradeLevelDao;
import com.littlehorizons.erp.model.GradeLevel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link GradeLevelDao} over {@code grados}.
 */
public class GradeLevelDaoJdbc extends BaseJdbcDao implements GradeLevelDao {

    @Override
    public Optional<GradeLevel> findById(int id) {
        return queryOne("SELECT * FROM grados WHERE id_grado = ?", this::map, id);
    }

    @Override
    public List<GradeLevel> findAll() {
        return queryList("SELECT * FROM grados ORDER BY nivel_numerico", this::map);
    }

    private GradeLevel map(ResultSet rs) throws SQLException {
        GradeLevel grade = new GradeLevel();
        grade.setId(rs.getInt("id_grado"));
        grade.setName(rs.getString("nombre_grado"));
        grade.setNumericLevel(rs.getInt("nivel_numerico"));
        return grade;
    }
}
