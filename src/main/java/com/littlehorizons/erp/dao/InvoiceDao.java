package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Invoice;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de facturas electrónicas (tabla {@code facturas}).
 */
public interface InvoiceDao {

    /**
     * Guarda la factura dentro de la transacción del pago.
     *
     * @return el id generado
     */
    int insert(Connection connection, Invoice invoice) throws SQLException;

    /**
     * Guarda el nombre y la ruta del PDF una vez escrito el archivo.
     */
    boolean updateFile(int invoiceId, String fileName, String filePath);

    /**
     * Factura asociada a un pago.
     */
    Optional<Invoice> findByPayment(int paymentId);

    /**
     * Busca una factura por su número (FE-000123).
     */
    Optional<Invoice> findByNumber(String number);

    /**
     * Devuelve todos los registros.
     */
    List<Invoice> findAll();

    /**
     * Siguiente consecutivo de factura (el id máximo + 1).
     */
    int nextSequence();
}
