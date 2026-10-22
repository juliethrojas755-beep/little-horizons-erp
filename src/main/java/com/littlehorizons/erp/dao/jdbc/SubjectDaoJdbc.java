package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.model.Subject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link SubjectDao} sobre la tabla {@code materias}.
 * Todo el SQL de materias vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class SubjectDaoJdbc extends BaseJdbcDao implements SubjectDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM materias ";

    /**
     * Busca una materia por su id.
     */
    @Override
    public Optional<Subject> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_materia = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de materias.
     */
    @Override
    public List<Subject> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY id_grado, nombre_materia", this::map);
    }

    /**
     * Busca por grado.
     */
    @Override
    public List<Subject> findByGrade(int gradeLevelId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_grado = ? ORDER BY nombre_materia", this::map, gradeLevelId);
    }

    /**
     * Busca por código único.
     */
    @Override
    public Optional<Subject> findByCode(String code) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE codigo_materia = ?", this::map, code);
    }

    /**
     * Guarda una materia nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Subject s) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                s.getCode(), s.getName(), s.getDescription(), s.getGradeLevelId(), s.getWeeklyHours(), s.isActive());
    }

    /**
     * Actualiza los datos de una materia.
     *
     * @return {@code true} si se modificó alguna fila
     */
    @Override
    public boolean update(Subject s) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE materias SET nombre_materia = ?, descripcion = ?, horas_semanales = ?, esta_activa = ? "
                        + "WHERE id_materia = ?",
                s.getName(), s.getDescription(), s.getWeeklyHours(), s.isActive(), s.getId()) > 0;
    }

    /**
     * Elimina una materia por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int id) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM materias WHERE id_materia = ?", id) > 0;
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Subject.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
