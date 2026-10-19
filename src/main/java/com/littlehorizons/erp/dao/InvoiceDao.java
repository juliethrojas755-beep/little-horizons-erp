package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Invoice;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data access for electronic invoices ({@code facturas}).
 */
public interface InvoiceDao {

    int insert(Connection connection, Invoice invoice) throws SQLException;

    /** Stores the PDF name and path once the file is written. */
    boolean updateFile(int invoiceId, String fileName, String filePath);

    Optional<Invoice> findByPayment(int paymentId);

    Optional<Invoice> findByNumber(String number);

    List<Invoice> findAll();

    /** @return the next invoice consecutive (max id + 1) */
    int nextSequence();
}
