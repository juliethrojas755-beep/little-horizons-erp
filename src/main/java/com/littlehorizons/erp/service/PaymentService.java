package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.ChargeDao;
import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.dao.InvoiceDao;
import com.littlehorizons.erp.dao.PaymentConceptDao;
import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.StudentDiscountDao;
import com.littlehorizons.erp.dao.TransactionManager;
import com.littlehorizons.erp.model.Charge;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentDiscount;
import com.littlehorizons.erp.model.dto.AmountQuote;
import com.littlehorizons.erp.model.dto.DelinquentAccount;
import com.littlehorizons.erp.model.dto.PaymentReceipt;
import com.littlehorizons.erp.model.enums.ChargeStatus;
import com.littlehorizons.erp.model.enums.DiscountType;
import com.littlehorizons.erp.model.enums.PaymentMethod;
import com.littlehorizons.erp.model.enums.PaymentPeriod;
import com.littlehorizons.erp.service.discount.DiscountStrategy;
import com.littlehorizons.erp.service.discount.DiscountStrategyFactory;
import com.littlehorizons.erp.service.invoice.CufeGenerator;
import com.littlehorizons.erp.service.invoice.InvoiceDocument;
import com.littlehorizons.erp.service.invoice.InvoiceDocumentGenerator;
import com.littlehorizons.erp.service.invoice.InvoiceLine;
import com.littlehorizons.erp.service.invoice.SchoolInfo;
import com.littlehorizons.erp.service.invoice.TaxPolicy;
import com.littlehorizons.erp.util.BusinessException;
import com.littlehorizons.erp.util.ErrorLogger;
import com.littlehorizons.erp.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Payment concepts, discounts, charges, payments, delinquent accounts and invoicing.
 */
public class PaymentService {

    private final PaymentConceptDao conceptDao;
    private final StudentDiscountDao discountDao;
    private final ChargeDao chargeDao;
    private final PaymentDao paymentDao;
    private final InvoiceDao invoiceDao;
    private final StudentDao studentDao;
    private final GuardianDao guardianDao;
    private final TransactionManager transactions;
    private final InvoiceDocumentGenerator invoiceGenerator;
    private final DiscountStrategyFactory discountStrategies;

    public PaymentService(PaymentConceptDao conceptDao, StudentDiscountDao discountDao, ChargeDao chargeDao,
                          PaymentDao paymentDao, InvoiceDao invoiceDao, StudentDao studentDao, GuardianDao guardianDao,
                          TransactionManager transactions, InvoiceDocumentGenerator invoiceGenerator,
                          DiscountStrategyFactory discountStrategies) {
        this.conceptDao = conceptDao;
        this.discountDao = discountDao;
        this.chargeDao = chargeDao;
        this.paymentDao = paymentDao;
        this.invoiceDao = invoiceDao;
        this.studentDao = studentDao;
        this.guardianDao = guardianDao;
        this.transactions = transactions;
        this.invoiceGenerator = invoiceGenerator;
        this.discountStrategies = discountStrategies;
    }

    // ---------- concepts and discounts ----------

    public List<PaymentConcept> findActiveConcepts() {
        return conceptDao.findAll().stream().filter(PaymentConcept::isActive).toList();
    }

    public PaymentConcept getConcept(int conceptId) {
        return conceptDao.findById(conceptId)
                .orElseThrow(() -> new BusinessException("No existe el concepto de pago con id " + conceptId + "."));
    }

    public int createConcept(PaymentConcept concept) {
        if (!Validator.isNotBlank(concept.getCode()) || !Validator.isPositive(concept.getBaseAmount())) {
            throw new BusinessException("El código es obligatorio y el valor base debe ser mayor que cero.");
        }
        concept.setActive(true);
        return conceptDao.insert(concept);
    }

    public int addDiscount(int studentId, int conceptId, DiscountType type, BigDecimal percentage) {
        getStudent(studentId);
        getConcept(conceptId);
        if (percentage.signum() <= 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("El porcentaje debe estar entre 1 y 100.");
        }
        StudentDiscount discount = new StudentDiscount();
        discount.setStudentId(studentId);
        discount.setConceptId(conceptId);
        discount.setType(type);
        discount.setPercentage(percentage);
        discount.setStartDate(LocalDate.now());
        return discountDao.insert(discount);
    }

    public List<StudentDiscount> getDiscounts(int studentId) {
        return discountDao.findByStudent(studentId);
    }

    /**
     * Price of a concept for a student: applies every valid discount through its Strategy
     * and keeps the BEST one (discounts are not added together).
     */
    public AmountQuote quote(int studentId, int conceptId, LocalDate date) {
        BigDecimal base = getConcept(conceptId).getBaseAmount();

        record Candidate(BigDecimal amount, String label) {
        }

        Candidate best = discountDao.findByStudentAndConcept(studentId, conceptId).stream()
                .filter(discount -> discount.isValidOn(date))
                .map(discount -> {
                    DiscountStrategy strategy = discountStrategies.forType(discount.getType());
                    return new Candidate(strategy.calculateDiscount(base, discount), strategy.describe(discount));
                })
                .filter(candidate -> candidate.amount().signum() > 0)
                .max(Comparator.comparing(Candidate::amount))
                .orElse(new Candidate(BigDecimal.ZERO, "Sin descuento"));
        return new AmountQuote(base, best.amount(), base.subtract(best.amount()), best.label());
    }

    // ---------- charges ----------

    /** Creates an amount owed by the student, already discounted. */
    public int createCharge(int studentId, int conceptId, LocalDate dueDate) {
        getStudent(studentId);
        Charge charge = new Charge();
        charge.setStudentId(studentId);
        charge.setConceptId(conceptId);
        charge.setAmount(quote(studentId, conceptId, LocalDate.now()).finalAmount());
        charge.setGeneratedDate(LocalDate.now());
        charge.setDueDate(dueDate);
        charge.setStatus(ChargeStatus.PENDING);
        return chargeDao.insert(charge);
    }

    public List<Charge> getPendingCharges(int studentId) {
        return chargeDao.findPendingByStudent(studentId);
    }

    /**
     * Delinquent accounts: charges still pending more than one month after the due date,
     * with the primary guardian to contact.
     */
    public List<DelinquentAccount> delinquentAccounts(LocalDate today) {
        Map<Integer, PaymentConcept> concepts = conceptDao.findAll().stream()
                .collect(Collectors.toMap(PaymentConcept::getId, Function.identity()));
        return chargeDao.findPending().stream()
                .filter(charge -> charge.isDelinquent(today))
                .map(charge -> {
                    Student student = getStudent(charge.getStudentId());
                    Optional<Guardian> guardian = guardianDao.findByStudent(student.getId()).stream().findFirst();
                    return new DelinquentAccount(charge.getId(), student.getFullName(),
                            guardian.map(Guardian::getFullName).orElse("Sin acudiente"),
                            guardian.map(Guardian::getPhone).orElse("-"),
                            concepts.get(charge.getConceptId()).getName(), charge.getAmount(), charge.getDueDate(),
                            ChronoUnit.DAYS.between(charge.getDueDate(), today));
                })
                .sorted(Comparator.comparingLong(DelinquentAccount::daysOverdue).reversed())
                .toList();
    }

    // ---------- payments ----------

    /**
     * Confirms a payment. In ONE transaction: stores the payment, settles the charge (if any)
     * and stores the invoice. After the commit the PDF is generated and its path saved.
     *
     * @param chargeId pending charge being paid, or {@code null} for a direct payment
     */
    public PaymentReceipt confirmPayment(int studentId, int conceptId, Integer chargeId, int guardianId,
                                         PaymentMethod method, PaymentPeriod period) {
        Student student = getStudent(studentId);
        PaymentConcept concept = getConcept(conceptId);
        Guardian payer = guardianDao.findByStudent(studentId).stream()
                .filter(g -> g.getId() == guardianId)
                .findFirst()
                .orElseThrow(() -> new BusinessException("El acudiente pagador debe estar vinculado al estudiante."));

        BigDecimal amount;
        if (chargeId != null) {
            Charge charge = chargeDao.findById(chargeId)
                    .filter(c -> c.getStudentId() == studentId && c.getStatus() == ChargeStatus.PENDING)
                    .orElseThrow(() -> new BusinessException("El cargo no existe, ya fue pagado o es de otro estudiante."));
            amount = charge.getAmount();
        } else {
            amount = quote(studentId, conceptId, LocalDate.now()).finalAmount();
        }

        LocalDate today = LocalDate.now();
        Payment payment = new Payment();
        payment.setStudentId(studentId);
        payment.setGuardianId(guardianId);
        payment.setConceptId(conceptId);
        payment.setAmount(amount);
        payment.setPaymentDate(today);
        payment.setMethod(method);
        payment.setAcademicYear(today.getYear());
        payment.setPeriod(period);
        payment.setReceiptNumber(String.format("REC-%d-%05d", today.getYear(), paymentDao.nextReceiptSequence()));

        int sequence = invoiceDao.nextSequence();
        Invoice invoice = new Invoice();
        invoice.setNumber(SchoolInfo.INVOICE_PREFIX + String.format("%06d", sequence));
        invoice.setIssueDate(today);
        invoice.setTotal(amount);
        invoice.setSubtotal(TaxPolicy.subtotal(amount, concept.getCode()));
        invoice.setTax(TaxPolicy.tax(amount, concept.getCode()));
        invoice.setCufe(CufeGenerator.generate(invoice.getNumber(), SchoolInfo.NIT_DIGITS, today, amount));
        invoice.setPdfFileName(String.format("factura_%06d.pdf", sequence));

        transactions.inTransaction(connection -> {
            int paymentId = paymentDao.insert(connection, payment);
            payment.setId(paymentId);
            if (chargeId != null) {
                chargeDao.markPaid(connection, chargeId, paymentId);
            }
            invoice.setPaymentId(paymentId);
            invoice.setId(invoiceDao.insert(connection, invoice));
            return paymentId;
        });

        String pdfPath;
        try {
            pdfPath = generatePdf(invoice, payment, student, payer, concept);
        } catch (RuntimeException e) {
            // The payment is already committed: it must not be lost because the PDF failed
            ErrorLogger.getInstance().error("PaymentService", "PDF not generated for " + invoice.getNumber(), e);
            pdfPath = null;
        }
        ErrorLogger.getInstance().info("PaymentService", "Payment " + payment.getReceiptNumber() + " confirmed, invoice "
                + invoice.getNumber());
        return new PaymentReceipt(payment, invoice, pdfPath);
    }

    /** Regenerates the PDF of an existing invoice (for example, if the file was deleted). */
    public String regenerateInvoice(String invoiceNumber) {
        Invoice invoice = invoiceDao.findByNumber(invoiceNumber)
                .orElseThrow(() -> new BusinessException("No existe la factura " + invoiceNumber + "."));
        Payment payment = paymentDao.findById(invoice.getPaymentId())
                .orElseThrow(() -> new BusinessException("La factura no tiene pago asociado."));
        Guardian payer = payment.getGuardianId() == null ? null : guardianDao.findById(payment.getGuardianId()).orElse(null);
        if (invoice.getPdfFileName() == null) {
            invoice.setPdfFileName("factura_" + invoiceNumber.replace(SchoolInfo.INVOICE_PREFIX, "") + ".pdf");
        }
        return generatePdf(invoice, payment, getStudent(payment.getStudentId()), payer, getConcept(payment.getConceptId()));
    }

    public List<Payment> paymentsByStudent(int studentId) {
        return paymentDao.findByStudent(studentId);
    }

    public List<Payment> paymentsByGuardian(int guardianId) {
        return paymentDao.findByGuardian(guardianId);
    }

    public List<Payment> findAllPayments() {
        return paymentDao.findAll();
    }

    public List<Invoice> findAllInvoices() {
        return invoiceDao.findAll();
    }

    public Map<Integer, PaymentConcept> conceptsById() {
        return conceptDao.findAll().stream().collect(Collectors.toMap(PaymentConcept::getId, Function.identity()));
    }

    private String generatePdf(Invoice invoice, Payment payment, Student student, Guardian payer, PaymentConcept concept) {
        String description = concept.getName() + " - " + student.getFullName()
                + (payment.getPeriod() != null ? " (período " + payment.getPeriod() + ")" : "");
        List<InvoiceLine> lines = List.of(new InvoiceLine(description, 1, invoice.getSubtotal(), invoice.getSubtotal()));
        String qr = "NumFac: " + invoice.getNumber() + "\nFecFac: " + invoice.getIssueDate()
                + "\nNitFac: " + SchoolInfo.NIT_DIGITS + "\nDocAdq: " + (payer != null ? payer.getDocumentNumber() : "-")
                + "\nValFac: " + invoice.getSubtotal() + "\nValIva: " + invoice.getTax()
                + "\nValTolFac: " + invoice.getTotal() + "\nCUFE: " + invoice.getCufe();
        String path = invoiceGenerator.generate(new InvoiceDocument(invoice, payment, student, payer, lines, qr));
        invoiceDao.updateFile(invoice.getId(), invoice.getPdfFileName(), path);
        invoice.setFilePath(path);
        return path;
    }

    private Student getStudent(int studentId) {
        return studentDao.findById(studentId)
                .orElseThrow(() -> new BusinessException("No existe un estudiante con id " + studentId + "."));
    }
}
