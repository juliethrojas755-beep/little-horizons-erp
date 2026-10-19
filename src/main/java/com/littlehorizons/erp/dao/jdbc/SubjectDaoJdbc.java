package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.model.Subject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link SubjectDao} over {@code materias}.
 */
public class SubjectDaoJdbc extends BaseJdbcDao implements SubjectDao {

    private static final String SELECT = "SELECT * FROM materias ";

    @Override
    public Optional<Subject> findById(int id) {
        return queryOne(SELECT + "WHERE id_materia = ?", this::map, id);
    }

    @Override
    public List<Subject> findAll() {
        return queryList(SELECT + "ORDER BY id_grado, nombre_materia", this::map);
    }

    @Override
    public List<Subject> findByGrade(int gradeLevelId) {
        return queryList(SELECT + "WHERE id_grado = ? ORDER BY nombre_materia", this::map, gradeLevelId);
    }

    @Override
    public Optional<Subject> findByCode(String code) {
        return queryOne(SELECT + "WHERE codigo_materia = ?", this::map, code);
    }

    @Override
    public int insert(Subject s) {
        return insert("INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                s.getCode(), s.getName(), s.getDescription(), s.getGradeLevelId(), s.getWeeklyHours(), s.isActive());
    }

    @Override
    public boolean update(Subject s) {
        return update("UPDATE materias SET nombre_materia = ?, descripcion = ?, horas_semanales = ?, esta_activa = ? "
                        + "WHERE id_materia = ?",
                s.getName(), s.getDescription(), s.getWeeklyHours(), s.isActive(), s.getId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return update("DELETE FROM materias WHERE id_materia = ?", id) > 0;
    }

    private Subject map(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setId(rs.getInt("id_materia"));
        subject.setCode(rs.getString("codigo_materia"));
        subject.setName(rs.getString("nombre_materia"));
        subject.setDescription(rs.getString("descripcion"));
        subject.setGradeLevelId(rs.getInt("id_grado"));
        subject.setWeeklyHours(rs.getInt("horas_semanales"));
        subject.setActive(rs.getBoolean("esta_activa"));
        return subject;
    }
}
