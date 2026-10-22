package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Payment;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de pagos recibidos (tabla {@code pagos}).
 */
public interface PaymentDao {

    /**
     * Guarda el pago dentro de una transacción abierta por el servicio.
     *
     * @param connection conexión compartida de la transacción
     * @return el id generado
     */
    int insert(Connection connection, Payment payment) throws SQLException;

    /**
     * Busca un registro por su id.
     *
     * @return el registro, o vacío si no existe
     */
    Optional<Payment> findById(int paymentId);

    /**
     * Pagos de un estudiante, el más reciente primero.
     */
    List<Payment> findByStudent(int studentId);

    /**
     * Pagos de todos los estudiantes vinculados a un acudiente.
     */
    List<Payment> findByGuardian(int guardianId);

    /**
     * Devuelve todos los registros.
     */
    List<Payment> findAll();

    /**
     * Siguiente consecutivo para el número de recibo (el id máximo + 1).
     */
    int nextReceiptSequence();
}
