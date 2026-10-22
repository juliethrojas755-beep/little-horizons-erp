package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Charge;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de cargos (valores que debe cada estudiante, tabla {@code cargos}).
 */
public interface ChargeDao {

    /**
     * Guarda un cargo pendiente.
     *
     * @return el id generado
     */
    int insert(Charge charge);

    /**
     * Busca un registro por su id.
     *
     * @return el registro, o vacío si no existe
     */
    Optional<Charge> findById(int chargeId);

    /**
     * Todos los cargos pendientes, los más antiguos primero.
     */
    List<Charge> findPending();

    /**
     * Cargos pendientes de un estudiante.
     */
    List<Charge> findPendingByStudent(int studentId);

    /**
     * Todos los cargos de un estudiante (pagados y pendientes).
     */
    List<Charge> findByStudent(int studentId);

    /**
     * Marca el cargo como Pagado dentro de la transacción del pago.
     */
    void markPaid(Connection connection, int chargeId, int paymentId) throws SQLException;
}
