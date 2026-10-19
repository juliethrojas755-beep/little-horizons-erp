package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Charge;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data access for charges owed by students ({@code cargos}).
 */
public interface ChargeDao {

    int insert(Charge charge);

    Optional<Charge> findById(int chargeId);

    List<Charge> findPending();

    List<Charge> findPendingByStudent(int studentId);

    List<Charge> findByStudent(int studentId);

    /** Marks the charge as paid inside the payment transaction. */
    void markPaid(Connection connection, int chargeId, int paymentId) throws SQLException;
}
