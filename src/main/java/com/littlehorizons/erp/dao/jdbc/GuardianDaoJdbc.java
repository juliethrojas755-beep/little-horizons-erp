package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.StudentGuardian;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link GuardianDao} sobre la tabla {@code acudientes}.
 * Todo el SQL de acudientes vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class GuardianDaoJdbc extends BaseJdbcDao implements GuardianDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM acudientes ";

    /**
     * Busca un acudiente por su id.
     */
    @Override
    public Optional<Guardian> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_acudiente = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de acudientes.
     */
    @Override
    public List<Guardian> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY primer_apellido, primer_nombre", this::map);
    }

    /**
     * Busca por número de documento (es único en la base de datos).
     */
    @Override
    public Optional<Guardian> findByDocument(String documentNumber) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE numero_documento = ?", this::map, documentNumber);
    }

    /**
     * Busca los registros de un estudiante.
     */
    @Override
    public List<Guardian> findByStudent(int studentId) {
        // JOIN con la tabla intermedia: trae los acudientes ligados al estudiante; el principal (es_principal = 1) sale primero
        return queryList("SELECT a.* FROM acudientes a JOIN estudiante_acudiente ea ON ea.id_acudiente = a.id_acudiente "
                + "WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC", this::map, studentId);
    }

    /**
     * Vincula al acudiente con un estudiante en la tabla intermedia estudiante_acudiente.
     */
    @Override
    public void linkStudent(StudentGuardian link) {
        update("INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) "
                        + "VALUES (?, ?, ?, ?)",
                link.getStudentId(), link.getGuardianId(), link.isPrimary(), link.isEmergencyContact());
    }

    /**
     * Guarda un acudiente nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Guardian g) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, "
                        + "segundo_apellido, parentesco, telefono_principal, telefono_secundario, correo_electronico, direccion, "
                        + "ocupacion, es_responsable_financiero) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                g.getDocumentType(), g.getDocumentNumber(), g.getFirstName(), g.getMiddleName(), g.getLastName(),
                g.getSecondLastName(), g.getRelationship(), g.getPhone(), g.getSecondaryPhone(), g.getEmail(),
                g.getAddress(), g.getOccupation(), g.isFinanciallyResponsible());
    }

    /**
     * Actualiza los datos de un acudiente.
     *
     * @return {@code true} si se modificó alguna fila
     */
    @Override
    public boolean update(Guardian g) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE acudientes SET primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, "
                        + "parentesco = ?, telefono_principal = ?, telefono_secundario = ?, correo_electronico = ?, direccion = ?, "
                        + "ocupacion = ?, es_responsable_financiero = ? WHERE id_acudiente = ?",
                g.getFirstName(), g.getMiddleName(), g.getLastName(), g.getSecondLastName(), g.getRelationship(),
                g.getPhone(), g.getSecondaryPhone(), g.getEmail(), g.getAddress(), g.getOccupation(),
                g.isFinanciallyResponsible(), g.getId()) > 0;
    }

    /**
     * Elimina un acudiente por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int id) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM acudientes WHERE id_acudiente = ?", id) > 0;
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Guardian.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private Guardian map(ResultSet rs) throws SQLException {
        Guardian guardian = new Guardian();
        mapPerson(rs, guardian, "id_acudiente");
        guardian.setRelationship(rs.getString("parentesco"));
        guardian.setPhone(rs.getString("telefono_principal"));
        guardian.setSecondaryPhone(rs.getString("telefono_secundario"));
        guardian.setAddress(rs.getString("direccion"));
        guardian.setOccupation(rs.getString("ocupacion"));
        guardian.setFinanciallyResponsible(rs.getBoolean("es_responsable_financiero"));
        return guardian;
    }
}
