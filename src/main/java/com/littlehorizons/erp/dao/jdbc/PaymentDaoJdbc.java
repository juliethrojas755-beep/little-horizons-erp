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
 * Implementación JDBC de {@link PaymentDao} sobre la tabla {@code pagos}.
 * Todo el SQL de pagos vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class PaymentDaoJdbc extends BaseJdbcDao implements PaymentDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM pagos ";

    /**
     * Guarda un pago nuevo dentro de una transacción abierta por el servicio.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Connection connection, Payment p) throws SQLException {
        return insert(connection, "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, "
                        + "metodo_pago, numero_recibo, anio_academico, periodo_academico, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getStudentId(), p.getGuardianId(), p.getConceptId(), p.getAmount(), p.getPaymentDate(), p.getMethod(),
                p.getReceiptNumber(), p.getAcademicYear(), p.getPeriod(), p.getNotes());
    }

    /**
     * Busca un pago por su id.
     */
    @Override
    public Optional<Payment> findById(int paymentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_pago = ?", this::map, paymentId);
    }

    /**
     * Busca los registros de un estudiante.
     */
    @Override
    public List<Payment> findByStudent(int studentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_estudiante = ? ORDER BY fecha_pago DESC", this::map, studentId);
    }

    /**
     * Busca por el acudiente al que están vinculados.
     */
    @Override
    public List<Payment> findByGuardian(int guardianId) {
        // JOIN: pagos de todos los estudiantes vinculados a ese acudiente (sirve para familias con hermanos)
        return queryList("SELECT p.* FROM pagos p JOIN estudiante_acudiente ea ON ea.id_estudiante = p.id_estudiante "
                + "WHERE ea.id_acudiente = ? ORDER BY p.fecha_pago DESC", this::map, guardianId);
    }

    /**
     * Devuelve todos los registros de pagos.
     */
    @Override
    public List<Payment> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY fecha_pago DESC", this::map);
    }

    /**
     * Calcula el siguiente consecutivo de recibo: el id más alto + 1 (COALESCE devuelve 0 si no hay pagos).
     */
    @Override
    public int nextReceiptSequence() {
        return queryInt("SELECT COALESCE(MAX(id_pago), 0) + 1 FROM pagos");
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Payment.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
