package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.StudentGuardian;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link GuardianDao} over {@code acudientes} and {@code estudiante_acudiente}.
 */
public class GuardianDaoJdbc extends BaseJdbcDao implements GuardianDao {

    private static final String SELECT = "SELECT * FROM acudientes ";

    @Override
    public Optional<Guardian> findById(int id) {
        return queryOne(SELECT + "WHERE id_acudiente = ?", this::map, id);
    }

    @Override
    public List<Guardian> findAll() {
        return queryList(SELECT + "ORDER BY primer_apellido, primer_nombre", this::map);
    }

    @Override
    public Optional<Guardian> findByDocument(String documentNumber) {
        return queryOne(SELECT + "WHERE numero_documento = ?", this::map, documentNumber);
    }

    @Override
    public List<Guardian> findByStudent(int studentId) {
        return queryList("SELECT a.* FROM acudientes a JOIN estudiante_acudiente ea ON ea.id_acudiente = a.id_acudiente "
                + "WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC", this::map, studentId);
    }

    @Override
    public void linkStudent(StudentGuardian link) {
        update("INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) "
                        + "VALUES (?, ?, ?, ?)",
                link.getStudentId(), link.getGuardianId(), link.isPrimary(), link.isEmergencyContact());
    }

    @Override
    public int insert(Guardian g) {
        return insert("INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, "
                        + "segundo_apellido, parentesco, telefono_principal, telefono_secundario, correo_electronico, direccion, "
                        + "ocupacion, es_responsable_financiero) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                g.getDocumentType(), g.getDocumentNumber(), g.getFirstName(), g.getMiddleName(), g.getLastName(),
                g.getSecondLastName(), g.getRelationship(), g.getPhone(), g.getSecondaryPhone(), g.getEmail(),
                g.getAddress(), g.getOccupation(), g.isFinanciallyResponsible());
    }

    @Override
    public boolean update(Guardian g) {
        return update("UPDATE acudientes SET primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, "
                        + "parentesco = ?, telefono_principal = ?, telefono_secundario = ?, correo_electronico = ?, direccion = ?, "
                        + "ocupacion = ?, es_responsable_financiero = ? WHERE id_acudiente = ?",
                g.getFirstName(), g.getMiddleName(), g.getLastName(), g.getSecondLastName(), g.getRelationship(),
                g.getPhone(), g.getSecondaryPhone(), g.getEmail(), g.getAddress(), g.getOccupation(),
                g.isFinanciallyResponsible(), g.getId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return update("DELETE FROM acudientes WHERE id_acudiente = ?", id) > 0;
    }

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
