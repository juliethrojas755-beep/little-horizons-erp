package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.Charge;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.StudentDiscount;
import com.littlehorizons.erp.model.dto.AmountQuote;
import com.littlehorizons.erp.model.dto.DelinquentAccount;
import com.littlehorizons.erp.model.dto.PaymentReceipt;
import com.littlehorizons.erp.model.enums.DiscountType;
import com.littlehorizons.erp.model.enums.PaymentMethod;
import com.littlehorizons.erp.model.enums.PaymentPeriod;
import com.littlehorizons.erp.service.PaymentService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Acciones de las pantallas financieras: conceptos, descuentos, cargos, pagos, facturas y morosos.
 * <p>Un controlador NO tiene reglas de negocio ni SQL: llama al servicio y envuelve el resultado en un
 * {@link OperationResult} para que la vista nunca reciba excepciones.</p>
 */
public class FinanceController extends BaseController {

    // Servicio al que se delegan las reglas de negocio
    private final PaymentService service;

    // El servicio llega por el constructor (inyección de dependencias): lo entrega AppContext
    public FinanceController(PaymentService service) {
        this.service = service;
    }

    /** Conceptos de pago activos. */
    public OperationResult<List<PaymentConcept>> findActiveConcepts() {
        // execute(mensaje, acción): ejecuta la acción y convierte errores en un resultado fallido; null = sin mensaje de éxito
        return execute(null, service::findActiveConcepts);
    }

    /** Tabla id -> concepto para mostrar nombres. */
    public OperationResult<Map<Integer, PaymentConcept>> conceptsById() {
        return execute(null, service::conceptsById);
    }

    /** Crea un concepto de pago. */
    public OperationResult<Integer> createConcept(PaymentConcept concept) {
        return execute("Concepto de pago creado.", () -> service.createConcept(concept));
    }

    /** Asigna un descuento o beca a un estudiante. */
    public OperationResult<Integer> addDiscount(int studentId, int conceptId, DiscountType type, BigDecimal percentage) {
        return execute("Descuento registrado.", () -> service.addDiscount(studentId, conceptId, type, percentage));
    }

    /** Descuentos de un estudiante. */
    public OperationResult<List<StudentDiscount>> getDiscounts(int studentId) {
        return execute(null, () -> service.getDiscounts(studentId));
    }

    /** Calcula el valor a pagar con el mejor descuento aplicado. */
    public OperationResult<AmountQuote> quote(int studentId, int conceptId) {
        return execute(null, () -> service.quote(studentId, conceptId, LocalDate.now()));
    }

    /** Genera un cargo pendiente para un estudiante. */
    public OperationResult<Integer> createCharge(int studentId, int conceptId, LocalDate dueDate) {
        return execute("Cargo generado.", () -> service.createCharge(studentId, conceptId, dueDate));
    }

    /** Cargos pendientes de un estudiante. */
    public OperationResult<List<Charge>> getPendingCharges(int studentId) {
        return execute(null, () -> service.getPendingCharges(studentId));
    }

    public OperationResult<PaymentReceipt> confirmPayment(int studentId, int conceptId, Integer chargeId, int guardianId,
                                                         PaymentMethod method, PaymentPeriod period) {
        return execute("Pago confirmado.",
                () -> service.confirmPayment(studentId, conceptId, chargeId, guardianId, method, period));
    }

    /** Vuelve a generar el PDF de una factura. */
    public OperationResult<String> regenerateInvoice(String number) {
        return execute("Factura generada nuevamente.", () -> service.regenerateInvoice(number));
    }

    /** Historial de pagos de un estudiante. */
    public OperationResult<List<Payment>> paymentsByStudent(int studentId) {
        return execute(null, () -> service.paymentsByStudent(studentId));
    }

    /** Historial de pagos de las familias de un acudiente. */
    public OperationResult<List<Payment>> paymentsByGuardian(int guardianId) {
        return execute(null, () -> service.paymentsByGuardian(guardianId));
    }

    /** Todas las facturas. */
    public OperationResult<List<Invoice>> findAllInvoices() {
        return execute(null, service::findAllInvoices);
    }

    /** Reporte de morosos (cargos pendientes más de un mes). */
    public OperationResult<List<DelinquentAccount>> delinquentAccounts() {
        return execute(null, () -> service.delinquentAccounts(LocalDate.now()));
    }
}
