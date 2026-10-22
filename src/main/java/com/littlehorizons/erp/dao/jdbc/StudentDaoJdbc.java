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
 * Implementación JDBC de {@link StudentDao} sobre la tabla {@code estudiantes}.
 * Todo el SQL de estudiantes vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class StudentDaoJdbc extends BaseJdbcDao implements StudentDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM estudiantes ";
    // Orden común del listado (por apellidos y luego por nombre)
    private static final String ORDER = " ORDER BY primer_apellido, segundo_apellido, primer_nombre";

    /**
     * Busca un estudiante por su id.
     */
    @Override
    public Optional<Student> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_estudiante = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de estudiantes.
     */
    @Override
    public List<Student> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + ORDER, this::map);
    }

    /**
     * Busca por número de documento (es único en la base de datos).
     */
    @Override
    public Optional<Student> findByDocument(String documentNumber) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE numero_documento = ?", this::map, documentNumber);
    }

    /**
     * Busca por cualquier parte del nombre o de los apellidos.
     */
    @Override
    public List<Student> searchByName(String text) {
        // Los % son comodines: busca el texto en cualquier posición del nombre
        String pattern = "%" + text + "%";
        // CONCAT_WS une los 4 nombres con espacio (ignora los NULL) y LIKE ? busca el texto dentro del nombre completo
        return queryList(SELECT + "WHERE CONCAT_WS(' ', primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) LIKE ?"
                + ORDER, this::map, pattern);
    }

    /**
     * Busca por el acudiente al que están vinculados.
     */
    @Override
    public List<Student> findByGuardian(int guardianId) {
        // JOIN con la tabla intermedia: estudiantes que comparten ese acudiente (hermanos)
        return queryList("SELECT e.* FROM estudiantes e JOIN estudiante_acudiente ea ON ea.id_estudiante = e.id_estudiante "
                + "WHERE ea.id_acudiente = ?" + ORDER, this::map, guardianId);
    }

    /**
     * Busca por grado.
     */
    @Override
    public List<Student> findByGrade(int gradeLevelId, int academicYear) {
        // JOIN con matrículas: estudiantes matriculados en ese grado y año
        return queryList("SELECT e.* FROM estudiantes e JOIN matriculas m ON m.id_estudiante = e.id_estudiante "
                + "WHERE m.id_grado = ? AND m.anio_academico = ?" + ORDER, this::map, gradeLevelId, academicYear);
    }

    /**
     * Guarda un estudiante nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Student s) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                        + "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, "
                        + "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                s.getDocumentType(), s.getDocumentNumber(), s.getFirstName(), s.getMiddleName(), s.getLastName(),
                s.getSecondLastName(), s.getBirthDate(), s.getGender(), s.getAddress(), s.getPhone(), s.getEmail(),
                s.getBloodType(), s.getAllergies(), s.getMedicalConditions(), s.getAdmissionDate(), s.getStatus());
    }

    /**
     * Actualiza los datos de un estudiante.
     *
     * @return {@code true} si se modificó alguna fila
     */
    @Override
    public boolean update(Student s) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE estudiantes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, segundo_nombre = ?, "
                        + "primer_apellido = ?, segundo_apellido = ?, fecha_nacimiento = ?, genero = ?, direccion = ?, telefono = ?, "
                        + "correo_electronico = ?, tipo_sangre = ?, alergias = ?, condiciones_medicas = ?, estado = ? "
                        + "WHERE id_estudiante = ?",
                s.getDocumentType(), s.getDocumentNumber(), s.getFirstName(), s.getMiddleName(), s.getLastName(),
                s.getSecondLastName(), s.getBirthDate(), s.getGender(), s.getAddress(), s.getPhone(), s.getEmail(),
                s.getBloodType(), s.getAllergies(), s.getMedicalConditions(), s.getStatus(), s.getId()) > 0;
    }

    /**
     * Elimina un estudiante por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int id) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM estudiantes WHERE id_estudiante = ?", id) > 0;
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Student.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
