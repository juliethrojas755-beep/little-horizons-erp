package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.InvoiceDao;
import com.littlehorizons.erp.model.Invoice;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link InvoiceDao} over {@code facturas}.
 */
public class InvoiceDaoJdbc extends BaseJdbcDao implements InvoiceDao {

    private static final String SELECT = "SELECT * FROM facturas ";

    @Override
    public int insert(Connection connection, Invoice i) throws SQLException {
        return insert(connection, "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, subtotal, iva, total, cufe, "
                        + "nombre_archivo_pdf, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                i.getNumber(), i.getPaymentId(), i.getIssueDate(), i.getSubtotal(), i.getTax(), i.getTotal(), i.getCufe(),
                i.getPdfFileName(), i.getFilePath());
    }

    @Override
    public boolean updateFile(int invoiceId, String fileName, String filePath) {
        return update("UPDATE facturas SET nombre_archivo_pdf = ?, ruta_archivo = ? WHERE id_factura = ?",
                fileName, filePath, invoiceId) > 0;
    }

    @Override
    public Optional<Invoice> findByPayment(int paymentId) {
        return queryOne(SELECT + "WHERE id_pago = ?", this::map, paymentId);
    }

    @Override
    public Optional<Invoice> findByNumber(String number) {
        return queryOne(SELECT + "WHERE numero_factura = ?", this::map, number);
    }

    @Override
    public List<Invoice> findAll() {
        return queryList(SELECT + "ORDER BY id_factura DESC", this::map);
    }

    @Override
    public int nextSequence() {
        return queryInt("SELECT COALESCE(MAX(id_factura), 0) + 1 FROM facturas");
    }

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
