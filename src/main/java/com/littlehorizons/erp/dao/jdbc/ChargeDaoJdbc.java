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
 * Implementación JDBC de {@link ChargeDao} sobre la tabla {@code cargos}.
 * Todo el SQL de cargos vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class ChargeDaoJdbc extends BaseJdbcDao implements ChargeDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM cargos ";

    /**
     * Guarda un cargo nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Charge c) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO cargos (id_estudiante, id_concepto, monto_cargo, fecha_generacion, fecha_vencimiento, estado_cargo) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                c.getStudentId(), c.getConceptId(), c.getAmount(), c.getGeneratedDate(), c.getDueDate(), c.getStatus());
    }

    /**
     * Busca un cargo por su id.
     */
    @Override
    public Optional<Charge> findById(int chargeId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_cargo = ?", this::map, chargeId);
    }

    /**
     * Busca los cargos que siguen pendientes, los más antiguos primero.
     */
    @Override
    public List<Charge> findPending() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE estado_cargo = ? ORDER BY fecha_vencimiento", this::map, ChargeStatus.PENDING);
    }

    /**
     * Busca los cargos pendientes de un estudiante.
     */
    @Override
    public List<Charge> findPendingByStudent(int studentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_estudiante = ? AND estado_cargo = ? ORDER BY fecha_vencimiento",
                this::map, studentId, ChargeStatus.PENDING);
    }

    /**
     * Busca los registros de un estudiante.
     */
    @Override
    public List<Charge> findByStudent(int studentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_estudiante = ? ORDER BY fecha_vencimiento DESC", this::map, studentId);
    }

    /**
     * Marca el cargo como Pagado y guarda el pago que lo saldó (dentro de la transacción del pago).
     */
    @Override
    public void markPaid(Connection connection, int chargeId, int paymentId) throws SQLException {
        update(connection, "UPDATE cargos SET estado_cargo = ?, id_pago = ? WHERE id_cargo = ?",
                ChargeStatus.PAID, paymentId, chargeId);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Charge.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
