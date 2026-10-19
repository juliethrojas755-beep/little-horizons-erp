package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.enums.Gender;
import com.littlehorizons.erp.model.enums.StudentStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link StudentDao}. Maps the English entity to table {@code estudiantes}.
 */
public class StudentDaoJdbc extends BaseJdbcDao implements StudentDao {

    private static final String SELECT = "SELECT * FROM estudiantes ";
    private static final String ORDER = " ORDER BY primer_apellido, segundo_apellido, primer_nombre";

    @Override
    public Optional<Student> findById(int id) {
        return queryOne(SELECT + "WHERE id_estudiante = ?", this::map, id);
    }

    @Override
    public List<Student> findAll() {
        return queryList(SELECT + ORDER, this::map);
    }

    @Override
    public Optional<Student> findByDocument(String documentNumber) {
        return queryOne(SELECT + "WHERE numero_documento = ?", this::map, documentNumber);
    }

    @Override
    public List<Student> searchByName(String text) {
        String pattern = "%" + text + "%";
        return queryList(SELECT + "WHERE CONCAT_WS(' ', primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) LIKE ?"
                + ORDER, this::map, pattern);
    }

    @Override
    public List<Student> findByGuardian(int guardianId) {
        return queryList("SELECT e.* FROM estudiantes e JOIN estudiante_acudiente ea ON ea.id_estudiante = e.id_estudiante "
                + "WHERE ea.id_acudiente = ?" + ORDER, this::map, guardianId);
    }

    @Override
    public List<Student> findByGrade(int gradeLevelId, int academicYear) {
        return queryList("SELECT e.* FROM estudiantes e JOIN matriculas m ON m.id_estudiante = e.id_estudiante "
                + "WHERE m.id_grado = ? AND m.anio_academico = ?" + ORDER, this::map, gradeLevelId, academicYear);
    }

    @Override
    public int insert(Student s) {
        return insert("INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                        + "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, "
                        + "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                s.getDocumentType(), s.getDocumentNumber(), s.getFirstName(), s.getMiddleName(), s.getLastName(),
                s.getSecondLastName(), s.getBirthDate(), s.getGender(), s.getAddress(), s.getPhone(), s.getEmail(),
                s.getBloodType(), s.getAllergies(), s.getMedicalConditions(), s.getAdmissionDate(), s.getStatus());
    }

    @Override
    public boolean update(Student s) {
        return update("UPDATE estudiantes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, segundo_nombre = ?, "
                        + "primer_apellido = ?, segundo_apellido = ?, fecha_nacimiento = ?, genero = ?, direccion = ?, telefono = ?, "
                        + "correo_electronico = ?, tipo_sangre = ?, alergias = ?, condiciones_medicas = ?, estado = ? "
                        + "WHERE id_estudiante = ?",
                s.getDocumentType(), s.getDocumentNumber(), s.getFirstName(), s.getMiddleName(), s.getLastName(),
                s.getSecondLastName(), s.getBirthDate(), s.getGender(), s.getAddress(), s.getPhone(), s.getEmail(),
                s.getBloodType(), s.getAllergies(), s.getMedicalConditions(), s.getStatus(), s.getId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return update("DELETE FROM estudiantes WHERE id_estudiante = ?", id) > 0;
    }

    private Student map(ResultSet rs) throws SQLException {
        Student student = new Student();
        mapPerson(rs, student, "id_estudiante");
        student.setPhone(rs.getString("telefono"));
        student.setBirthDate(getDate(rs, "fecha_nacimiento"));
        student.setGender(Gender.fromDbValue(rs.getString("genero")));
        student.setAddress(rs.getString("direccion"));
        student.setBloodType(rs.getString("tipo_sangre"));
        student.setAllergies(rs.getString("alergias"));
        student.setMedicalConditions(rs.getString("condiciones_medicas"));
        student.setAdmissionDate(getDate(rs, "fecha_ingreso"));
        student.setStatus(StudentStatus.fromDbValue(rs.getString("estado")));
        return student;
    }
}
