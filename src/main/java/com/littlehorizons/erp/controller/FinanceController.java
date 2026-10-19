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
 * Actions of the financial screens.
 */
public class FinanceController extends BaseController {

    private final PaymentService service;

    public FinanceController(PaymentService service) {
        this.service = service;
    }

    public OperationResult<List<PaymentConcept>> findActiveConcepts() {
        return execute(null, service::findActiveConcepts);
    }

    public OperationResult<Map<Integer, PaymentConcept>> conceptsById() {
        return execute(null, service::conceptsById);
    }

    public OperationResult<Integer> createConcept(PaymentConcept concept) {
        return execute("Concepto de pago creado.", () -> service.createConcept(concept));
    }

    public OperationResult<Integer> addDiscount(int studentId, int conceptId, DiscountType type, BigDecimal percentage) {
        return execute("Descuento registrado.", () -> service.addDiscount(studentId, conceptId, type, percentage));
    }

    public OperationResult<List<StudentDiscount>> getDiscounts(int studentId) {
        return execute(null, () -> service.getDiscounts(studentId));
    }

    public OperationResult<AmountQuote> quote(int studentId, int conceptId) {
        return execute(null, () -> service.quote(studentId, conceptId, LocalDate.now()));
    }

    public OperationResult<Integer> createCharge(int studentId, int conceptId, LocalDate dueDate) {
        return execute("Cargo generado.", () -> service.createCharge(studentId, conceptId, dueDate));
    }

    public OperationResult<List<Charge>> getPendingCharges(int studentId) {
        return execute(null, () -> service.getPendingCharges(studentId));
    }

    public OperationResult<PaymentReceipt> confirmPayment(int studentId, int conceptId, Integer chargeId, int guardianId,
                                                         PaymentMethod method, PaymentPeriod period) {
        return execute("Pago confirmado.",
                () -> service.confirmPayment(studentId, conceptId, chargeId, guardianId, method, period));
    }

    public OperationResult<String> regenerateInvoice(String number) {
        return execute("Factura generada nuevamente.", () -> service.regenerateInvoice(number));
    }

    public OperationResult<List<Payment>> paymentsByStudent(int studentId) {
        return execute(null, () -> service.paymentsByStudent(studentId));
    }

    public OperationResult<List<Payment>> paymentsByGuardian(int guardianId) {
        return execute(null, () -> service.paymentsByGuardian(guardianId));
    }

    public OperationResult<List<Invoice>> findAllInvoices() {
        return execute(null, service::findAllInvoices);
    }

    public OperationResult<List<DelinquentAccount>> delinquentAccounts() {
        return execute(null, () -> service.delinquentAccounts(LocalDate.now()));
    }
}
