package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.enums.PaymentMethod;
import com.littlehorizons.erp.model.enums.PaymentPeriod;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link PaymentDao} over {@code pagos}.
 */
public class PaymentDaoJdbc extends BaseJdbcDao implements PaymentDao {

    private static final String SELECT = "SELECT * FROM pagos ";

    @Override
    public int insert(Connection connection, Payment p) throws SQLException {
        return insert(connection, "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, "
                        + "metodo_pago, numero_recibo, anio_academico, periodo_academico, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getStudentId(), p.getGuardianId(), p.getConceptId(), p.getAmount(), p.getPaymentDate(), p.getMethod(),
                p.getReceiptNumber(), p.getAcademicYear(), p.getPeriod(), p.getNotes());
    }

    @Override
    public Optional<Payment> findById(int paymentId) {
        return queryOne(SELECT + "WHERE id_pago = ?", this::map, paymentId);
    }

    @Override
    public List<Payment> findByStudent(int studentId) {
        return queryList(SELECT + "WHERE id_estudiante = ? ORDER BY fecha_pago DESC", this::map, studentId);
    }

    @Override
    public List<Payment> findByGuardian(int guardianId) {
        return queryList("SELECT p.* FROM pagos p JOIN estudiante_acudiente ea ON ea.id_estudiante = p.id_estudiante "
                + "WHERE ea.id_acudiente = ? ORDER BY p.fecha_pago DESC", this::map, guardianId);
    }

    @Override
    public List<Payment> findAll() {
        return queryList(SELECT + "ORDER BY fecha_pago DESC", this::map);
    }

    @Override
    public int nextReceiptSequence() {
        return queryInt("SELECT COALESCE(MAX(id_pago), 0) + 1 FROM pagos");
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id_pago"));
        payment.setStudentId(rs.getInt("id_estudiante"));
        payment.setGuardianId(getNullableInt(rs, "id_acudiente"));
        payment.setConceptId(rs.getInt("id_concepto"));
        payment.setAmount(rs.getBigDecimal("monto_pagado"));
        payment.setPaymentDate(getDate(rs, "fecha_pago"));
        payment.setMethod(PaymentMethod.fromDbValue(rs.getString("metodo_pago")));
        payment.setReceiptNumber(rs.getString("numero_recibo"));
        payment.setAcademicYear(getNullableInt(rs, "anio_academico"));
        payment.setPeriod(PaymentPeriod.fromDbValue(rs.getString("periodo_academico")));
        payment.setNotes(rs.getString("notas"));
        return payment;
    }
}
