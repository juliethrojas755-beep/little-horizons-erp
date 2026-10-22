package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.enums.TeacherStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link TeacherDao} sobre la tabla {@code profesores}.
 * Todo el SQL de profesores vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class TeacherDaoJdbc extends BaseJdbcDao implements TeacherDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM profesores ";

    /**
     * Busca un profesor por su id.
     */
    @Override
    public Optional<Teacher> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_profesor = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de profesores.
     */
    @Override
    public List<Teacher> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY primer_apellido, primer_nombre", this::map);
    }

    /**
     * Busca por número de documento (es único en la base de datos).
     */
    @Override
    public Optional<Teacher> findByDocument(String documentNumber) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE numero_documento = ?", this::map, documentNumber);
    }

    /**
     * Guarda un profesor nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Teacher t) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, "
                        + "segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                t.getDocumentType(), t.getDocumentNumber(), t.getFirstName(), t.getMiddleName(), t.getLastName(),
                t.getSecondLastName(), t.getEmail(), t.getPhone(), t.getSpecialization(), t.getHireDate(), t.getStatus());
    }

    /**
     * Actualiza los datos de un profesor.
     *
     * @return {@code true} si se modificó alguna fila
     */
    @Override
    public boolean update(Teacher t) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE profesores SET primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, "
                        + "correo_electronico = ?, telefono = ?, especializacion = ?, estado = ? WHERE id_profesor = ?",
                t.getFirstName(), t.getMiddleName(), t.getLastName(), t.getSecondLastName(), t.getEmail(), t.getPhone(),
                t.getSpecialization(), t.getStatus(), t.getId()) > 0;
    }

    /**
     * Elimina un profesor por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int id) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM profesores WHERE id_profesor = ?", id) > 0;
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Teacher.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private Teacher map(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        mapPerson(rs, teacher, "id_profesor");
        teacher.setPhone(rs.getString("telefono"));
        teacher.setSpecialization(rs.getString("especializacion"));
        teacher.setHireDate(getDate(rs, "fecha_contratacion"));
        teacher.setStatus(TeacherStatus.fromDbValue(rs.getString("estado")));
        return teacher;
    }
}
