package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.PaymentConceptDao;
import com.littlehorizons.erp.model.PaymentConcept;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link PaymentConceptDao} over {@code conceptos_pago}.
 */
public class PaymentConceptDaoJdbc extends BaseJdbcDao implements PaymentConceptDao {

    @Override
    public Optional<PaymentConcept> findById(int id) {
        return queryOne("SELECT * FROM conceptos_pago WHERE id_concepto = ?", this::map, id);
    }

    @Override
    public List<PaymentConcept> findAll() {
        return queryList("SELECT * FROM conceptos_pago ORDER BY nombre_concepto", this::map);
    }

    @Override
    public int insert(PaymentConcept c) {
        return insert("INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, monto_base, es_recurrente, "
                        + "esta_activo) VALUES (?, ?, ?, ?, ?, ?)",
                c.getCode(), c.getName(), c.getDescription(), c.getBaseAmount(), c.isRecurring(), c.isActive());
    }

    @Override
    public boolean update(PaymentConcept c) {
        return update("UPDATE conceptos_pago SET nombre_concepto = ?, descripcion = ?, monto_base = ?, es_recurrente = ?, "
                        + "esta_activo = ? WHERE id_concepto = ?",
                c.getName(), c.getDescription(), c.getBaseAmount(), c.isRecurring(), c.isActive(), c.getId()) > 0;
    }

    @Override
    public boolean delete(int id) {
        return update("DELETE FROM conceptos_pago WHERE id_concepto = ?", id) > 0;
    }

    private PaymentConcept map(ResultSet rs) throws SQLException {
        PaymentConcept concept = new PaymentConcept();
        concept.setId(rs.getInt("id_concepto"));
        concept.setCode(rs.getString("codigo_concepto"));
        concept.setName(rs.getString("nombre_concepto"));
        concept.setDescription(rs.getString("descripcion"));
        concept.setBaseAmount(rs.getBigDecimal("monto_base"));
        concept.setRecurring(rs.getBoolean("es_recurrente"));
        concept.setActive(rs.getBoolean("esta_activo"));
        return concept;
    }
}
