package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.PaymentConceptDao;
import com.littlehorizons.erp.model.PaymentConcept;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link PaymentConceptDao} sobre la tabla {@code conceptos_pago}.
 * Todo el SQL de conceptos de pago vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class PaymentConceptDaoJdbc extends BaseJdbcDao implements PaymentConceptDao {

    /**
     * Busca un concepto de pago por su id.
     */
    @Override
    public Optional<PaymentConcept> findById(int id) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne("SELECT * FROM conceptos_pago WHERE id_concepto = ?", this::map, id);
    }

    /**
     * Devuelve todos los registros de conceptos de pago.
     */
    @Override
    public List<PaymentConcept> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM conceptos_pago ORDER BY nombre_concepto", this::map);
    }

    /**
     * Guarda un concepto de pago nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(PaymentConcept c) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, monto_base, es_recurrente, "
                        + "esta_activo) VALUES (?, ?, ?, ?, ?, ?)",
                c.getCode(), c.getName(), c.getDescription(), c.getBaseAmount(), c.isRecurring(), c.isActive());
    }

    /**
     * Actualiza los datos de un concepto de pago.
     *
     * @return {@code true} si se modificó alguna fila
     */
    @Override
    public boolean update(PaymentConcept c) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE conceptos_pago SET nombre_concepto = ?, descripcion = ?, monto_base = ?, es_recurrente = ?, "
                        + "esta_activo = ? WHERE id_concepto = ?",
                c.getName(), c.getDescription(), c.getBaseAmount(), c.isRecurring(), c.isActive(), c.getId()) > 0;
    }

    /**
     * Elimina un concepto de pago por su id.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    @Override
    public boolean delete(int id) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("DELETE FROM conceptos_pago WHERE id_concepto = ?", id) > 0;
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto PaymentConcept.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
