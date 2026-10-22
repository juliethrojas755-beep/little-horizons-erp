package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.InvoiceDao;
import com.littlehorizons.erp.model.Invoice;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link InvoiceDao} sobre la tabla {@code facturas}.
 * Todo el SQL de facturas vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class InvoiceDaoJdbc extends BaseJdbcDao implements InvoiceDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM facturas ";

    /**
     * Guarda una factura nuevo dentro de una transacción abierta por el servicio.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(Connection connection, Invoice i) throws SQLException {
        return insert(connection, "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, subtotal, iva, total, cufe, "
                        + "nombre_archivo_pdf, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                i.getNumber(), i.getPaymentId(), i.getIssueDate(), i.getSubtotal(), i.getTax(), i.getTotal(), i.getCufe(),
                i.getPdfFileName(), i.getFilePath());
    }

    /**
     * Guarda el nombre y la ruta del PDF una vez generado el archivo.
     */
    @Override
    public boolean updateFile(int invoiceId, String fileName, String filePath) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE facturas SET nombre_archivo_pdf = ?, ruta_archivo = ? WHERE id_factura = ?",
                fileName, filePath, invoiceId) > 0;
    }

    /**
     * Busca la factura de un pago.
     */
    @Override
    public Optional<Invoice> findByPayment(int paymentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_pago = ?", this::map, paymentId);
    }

    /**
     * Busca una factura por su número (FE-000123).
     */
    @Override
    public Optional<Invoice> findByNumber(String number) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE numero_factura = ?", this::map, number);
    }

    /**
     * Devuelve todos los registros de facturas.
     */
    @Override
    public List<Invoice> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY id_factura DESC", this::map);
    }

    /**
     * Calcula el siguiente consecutivo: el id más alto + 1 (COALESCE devuelve 0 si la tabla está vacía).
     */
    @Override
    public int nextSequence() {
        return queryInt("SELECT COALESCE(MAX(id_factura), 0) + 1 FROM facturas");
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto Invoice.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private Invoice map(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getInt("id_factura"));
        invoice.setNumber(rs.getString("numero_factura"));
        invoice.setPaymentId(rs.getInt("id_pago"));
        invoice.setIssueDate(getDate(rs, "fecha_emision"));
        invoice.setSubtotal(rs.getBigDecimal("subtotal"));
        invoice.setTax(rs.getBigDecimal("iva"));
        invoice.setTotal(rs.getBigDecimal("total"));
        invoice.setCufe(rs.getString("cufe"));
        invoice.setPdfFileName(rs.getString("nombre_archivo_pdf"));
        invoice.setFilePath(rs.getString("ruta_archivo"));
        return invoice;
    }
}
