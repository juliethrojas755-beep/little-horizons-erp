package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.ChargeDao;
import com.littlehorizons.erp.model.Charge;
import com.littlehorizons.erp.model.enums.ChargeStatus;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ChargeDao} over {@code cargos}.
 */
public class ChargeDaoJdbc extends BaseJdbcDao implements ChargeDao {

    private static final String SELECT = "SELECT * FROM cargos ";

    @Override
    public int insert(Charge c) {
        return insert("INSERT INTO cargos (id_estudiante, id_concepto, monto_cargo, fecha_generacion, fecha_vencimiento, estado_cargo) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                c.getStudentId(), c.getConceptId(), c.getAmount(), c.getGeneratedDate(), c.getDueDate(), c.getStatus());
    }

    @Override
    public Optional<Charge> findById(int chargeId) {
        return queryOne(SELECT + "WHERE id_cargo = ?", this::map, chargeId);
    }

    @Override
    public List<Charge> findPending() {
        return queryList(SELECT + "WHERE estado_cargo = ? ORDER BY fecha_vencimiento", this::map, ChargeStatus.PENDING);
    }

    @Override
    public List<Charge> findPendingByStudent(int studentId) {
        return queryList(SELECT + "WHERE id_estudiante = ? AND estado_cargo = ? ORDER BY fecha_vencimiento",
                this::map, studentId, ChargeStatus.PENDING);
    }

    @Override
    public List<Charge> findByStudent(int studentId) {
        return queryList(SELECT + "WHERE id_estudiante = ? ORDER BY fecha_vencimiento DESC", this::map, studentId);
    }

    @Override
    public void markPaid(Connection connection, int chargeId, int paymentId) throws SQLException {
        update(connection, "UPDATE cargos SET estado_cargo = ?, id_pago = ? WHERE id_cargo = ?",
                ChargeStatus.PAID, paymentId, chargeId);
    }

    private Charge map(ResultSet rs) throws SQLException {
        Charge charge = new Charge();
        charge.setId(rs.getInt("id_cargo"));
        charge.setStudentId(rs.getInt("id_estudiante"));
        charge.setConceptId(rs.getInt("id_concepto"));
        charge.setAmount(rs.getBigDecimal("monto_cargo"));
        charge.setGeneratedDate(getDate(rs, "fecha_generacion"));
        charge.setDueDate(getDate(rs, "fecha_vencimiento"));
        charge.setStatus(ChargeStatus.fromDbValue(rs.getString("estado_cargo")));
        charge.setPaymentId(getNullableInt(rs, "id_pago"));
        return charge;
    }
}
