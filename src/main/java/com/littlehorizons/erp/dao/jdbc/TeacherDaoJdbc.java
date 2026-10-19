package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.enums.TeacherStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link TeacherDao} over {@code profesores}.
 */
public class TeacherDaoJdbc extends BaseJdbcDao implements TeacherDao {

    private static final String SELECT = "SELECT * FROM profesores ";

    @Override
    public Optional<Teacher> findById(int id) {
        return queryOne(SELECT + "WHERE id_profesor = ?", this::map, id);
    }

    @Override
    public List<Teacher> findAll() {
        return queryList(SELECT + "ORDER BY primer_apellido, primer_nombre", this::map);
    }

    @Override
    public Optional<Teacher> findByDocument(String documentNumber) {
        return queryOne(SELECT + "WHERE numero_documento = ?", this::map, documentNumber);
    }

    @Override
    public int insert(Teacher t) {
        return insert("INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, "
                        + "segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                t.getDocumentType(), t.getDocumentNumber(), t.getFirstName(), t.getMiddleName(), t.getLastName(),
                t.getSecondLastName(), t.getEmail(), t.getPhone(), t.getSpecialization(), t.getHireDate(), t.getStatus());
    }

    @Override
    public boolean update(Teacher t) {
        return update("UPDATE profesores SET primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, "
                        + "correo_electronico = ?, telefono = ?, especializacion = ?, estado = ? WHERE id_profesor = ?",
                t.getFirstName(), t.getMiddleName(), t.getLastName(), t.getSecondLastName(), t.getEmail(), t.getPhone(),
                t.getSpecialization(), t.getStatus(), t.getId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return update("DELETE FROM profesores WHERE id_profesor = ?", id) > 0;
    }

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
