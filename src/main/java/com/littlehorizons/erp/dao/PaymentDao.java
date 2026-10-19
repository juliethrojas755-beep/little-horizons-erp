package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Payment;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data access for received payments ({@code pagos}).
 */
public interface PaymentDao {

    /** Inserts inside a transaction opened by the service. */
    int insert(Connection connection, Payment payment) throws SQLException;

    Optional<Payment> findById(int paymentId);

    List<Payment> findByStudent(int studentId);

    /** Payments of every student linked to the guardian. */
    List<Payment> findByGuardian(int guardianId);

    List<Payment> findAll();

    /** @return the next receipt consecutive (max + 1) */
    int nextReceiptSequence();
}
