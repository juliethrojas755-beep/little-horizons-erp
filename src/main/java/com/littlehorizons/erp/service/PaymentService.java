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
 * Conceptos de pago, descuentos, cargos, pagos, morosos y facturación.
 * <p>
 * Es el servicio con más reglas de negocio. Para el PDF no depende de OpenPDF sino de la interfaz
 * {@link InvoiceDocumentGenerator} (Inversión de Dependencias), y para los descuentos usa el patrón Strategy.
 * </p>
 */
public class PaymentService {

    private final PaymentConceptDao conceptDao;
    private final StudentDiscountDao discountDao;
    private final ChargeDao chargeDao;
    private final PaymentDao paymentDao;
    private final InvoiceDao invoiceDao;
    private final StudentDao studentDao;
    private final GuardianDao guardianDao;
    // Para guardar pago, cargo y factura juntos (todo o nada)
    private final TransactionManager transactions;
    // Quien escribe el PDF (interfaz: hoy es OpenPDF, mañana podría ser otra librería)
    private final InvoiceDocumentGenerator invoiceGenerator;
    // Fábrica que entrega la estrategia de cada tipo de descuento
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

    // ====================== CONCEPTOS Y DESCUENTOS ======================

    /** Conceptos de pago que están activos en el catálogo. */
    public List<PaymentConcept> findActiveConcepts() {
        return conceptDao.findAll().stream().filter(PaymentConcept::isActive).toList();
    }

    public PaymentConcept getConcept(int conceptId) {
        return conceptDao.findById(conceptId)
                .orElseThrow(() -> new BusinessException("No existe el concepto de pago con id " + conceptId + "."));
    }

    /** Crea un concepto nuevo; necesita código y un valor base mayor que cero. */
    public int createConcept(PaymentConcept concept) {
        if (!Validator.isNotBlank(concept.getCode()) || !Validator.isPositive(concept.getBaseAmount())) {
            throw new BusinessException("El código es obligatorio y el valor base debe ser mayor que cero.");
        }
        concept.setActive(true);
        return conceptDao.insert(concept);
    }

    /** Asigna un descuento o beca a un estudiante para un concepto. */
    public int addDiscount(int studentId, int conceptId, DiscountType type, BigDecimal percentage) {
        getStudent(studentId);      // verifica que el estudiante exista
        getConcept(conceptId);      // verifica que el concepto exista
        // El porcentaje debe estar entre 1 y 100
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
     * Precio de un concepto para un estudiante: aplica cada descuento vigente con su Strategy
     * y se queda con el MEJOR (los descuentos no se suman).
     */
    public AmountQuote quote(int studentId, int conceptId, LocalDate date) {
        BigDecimal base = getConcept(conceptId).getBaseAmount();

        // Record local: pareja (valor del descuento, texto que lo describe)
        record Candidate(BigDecimal amount, String label) {
        }

        Candidate best = discountDao.findByStudentAndConcept(studentId, conceptId).stream()
                .filter(discount -> discount.isValidOn(date))                       // solo descuentos vigentes hoy
                .map(discount -> {
                    // La fábrica entrega la estrategia según el tipo (hermanos, beca, otro)
                    DiscountStrategy strategy = discountStrategies.forType(discount.getType());
                    return new Candidate(strategy.calculateDiscount(base, discount), strategy.describe(discount));
                })
                .filter(candidate -> candidate.amount().signum() > 0)               // se ignoran los que no descuentan nada
                .max(Comparator.comparing(Candidate::amount))                       // el mayor descuento gana
                .orElse(new Candidate(BigDecimal.ZERO, "Sin descuento"));           // si no hay ninguno, no hay descuento
        return new AmountQuote(base, best.amount(), base.subtract(best.amount()), best.label());
    }

    // ====================== CARGOS ======================

    /** Crea un valor que el estudiante debe, ya con el descuento aplicado. */
    public int createCharge(int studentId, int conceptId, LocalDate dueDate) {
        getStudent(studentId);
        Charge charge = new Charge();
        charge.setStudentId(studentId);
        charge.setConceptId(conceptId);
        charge.setAmount(quote(studentId, conceptId, LocalDate.now()).finalAmount());   // valor con descuento
        charge.setGeneratedDate(LocalDate.now());
        charge.setDueDate(dueDate);
        charge.setStatus(ChargeStatus.PENDING);
        return chargeDao.insert(charge);
    }

    public List<Charge> getPendingCharges(int studentId) {
        return chargeDao.findPendingByStudent(studentId);
    }

    /**
     * Morosos: cargos que siguen pendientes más de un mes después del vencimiento,
     * con el acudiente principal al que hay que contactar.
     */
    public List<DelinquentAccount> delinquentAccounts(LocalDate today) {
        // Tabla id -> concepto para mostrar el nombre del concepto
        Map<Integer, PaymentConcept> concepts = conceptDao.findAll().stream()
                .collect(Collectors.toMap(PaymentConcept::getId, Function.identity()));
        return chargeDao.findPending().stream()
                .filter(charge -> charge.isDelinquent(today))        // regla de la entidad: más de un mes vencido
                .map(charge -> {
                    Student student = getStudent(charge.getStudentId());
                    // El primer acudiente de la lista es el principal
                    Optional<Guardian> guardian = guardianDao.findByStudent(student.getId()).stream().findFirst();
                    return new DelinquentAccount(charge.getId(), student.getFullName(),
                            guardian.map(Guardian::getFullName).orElse("Sin acudiente"),
                            guardian.map(Guardian::getPhone).orElse("-"),
                            concepts.get(charge.getConceptId()).getName(), charge.getAmount(), charge.getDueDate(),
                            ChronoUnit.DAYS.between(charge.getDueDate(), today));    // días de mora
                })
                .sorted(Comparator.comparingLong(DelinquentAccount::daysOverdue).reversed())   // los más atrasados primero
                .toList();
    }

    // ====================== PAGOS ======================

    /**
     * Confirma un pago. En UNA transacción: guarda el pago, salda el cargo (si lo hay) y guarda la factura.
     * Después del commit genera el PDF y guarda su ruta.
     *
     * @param chargeId cargo pendiente que se está pagando, o {@code null} si es un pago directo
     */
    public PaymentReceipt confirmPayment(int studentId, int conceptId, Integer chargeId, int guardianId,
                                         PaymentMethod method, PaymentPeriod period) {
        Student student = getStudent(studentId);
        PaymentConcept concept = getConcept(conceptId);
        // El pagador debe ser un acudiente vinculado al estudiante
        Guardian payer = guardianDao.findByStudent(studentId).stream()
                .filter(g -> g.getId() == guardianId)
                .findFirst()
                .orElseThrow(() -> new BusinessException("El acudiente pagador debe estar vinculado al estudiante."));

        // ---- 1) Valor a pagar: el del cargo pendiente o el precio del concepto con descuento ----
        BigDecimal amount;
        if (chargeId != null) {
            Charge charge = chargeDao.findById(chargeId)
                    .filter(c -> c.getStudentId() == studentId && c.getStatus() == ChargeStatus.PENDING)
                    .orElseThrow(() -> new BusinessException("El cargo no existe, ya fue pagado o es de otro estudiante."));
            amount = charge.getAmount();
        } else {
            amount = quote(studentId, conceptId, LocalDate.now()).finalAmount();
        }

        // ---- 2) Se arma el pago con su número de recibo consecutivo (REC-2026-00001) ----
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

        // ---- 3) Se arma la factura: consecutivo, subtotal, IVA, total y CUFE ----
        int sequence = invoiceDao.nextSequence();
        Invoice invoice = new Invoice();
        invoice.setNumber(SchoolInfo.INVOICE_PREFIX + String.format("%06d", sequence));        // FE-000123
        invoice.setIssueDate(today);
        invoice.setTotal(amount);
        invoice.setSubtotal(TaxPolicy.subtotal(amount, concept.getCode()));                   // valor antes de IVA
        invoice.setTax(TaxPolicy.tax(amount, concept.getCode()));                              // IVA incluido
        invoice.setCufe(CufeGenerator.generate(invoice.getNumber(), SchoolInfo.NIT_DIGITS, today, amount));
        invoice.setPdfFileName(String.format("factura_%06d.pdf", sequence));                  // factura_000123.pdf

        // ---- 4) TRANSACCIÓN: pago + cargo saldado + factura se guardan juntos, o no se guarda nada ----
        transactions.inTransaction(connection -> {
            int paymentId = paymentDao.insert(connection, payment);
            payment.setId(paymentId);
            if (chargeId != null) {
                chargeDao.markPaid(connection, chargeId, paymentId);     // el cargo queda Pagado
            }
            invoice.setPaymentId(paymentId);
            invoice.setId(invoiceDao.insert(connection, invoice));
            return paymentId;
        });

        // ---- 5) Fuera de la transacción se genera el PDF ----
        String pdfPath;
        try {
            pdfPath = generatePdf(invoice, payment, student, payer, concept);
        } catch (RuntimeException e) {
            // El pago ya quedó confirmado en la base de datos: no se pierde porque falle el PDF
            ErrorLogger.getInstance().error("PaymentService", "PDF not generated for " + invoice.getNumber(), e);
            pdfPath = null;
        }
        ErrorLogger.getInstance().info("PaymentService", "Payment " + payment.getReceiptNumber() + " confirmed, invoice "
                + invoice.getNumber());
        return new PaymentReceipt(payment, invoice, pdfPath);
    }

    /** Vuelve a generar el PDF de una factura existente (por ejemplo, si se borró el archivo). */
    public String regenerateInvoice(String invoiceNumber) {
        Invoice invoice = invoiceDao.findByNumber(invoiceNumber)
                .orElseThrow(() -> new BusinessException("No existe la factura " + invoiceNumber + "."));
        Payment payment = paymentDao.findById(invoice.getPaymentId())
                .orElseThrow(() -> new BusinessException("La factura no tiene pago asociado."));
        Guardian payer = payment.getGuardianId() == null ? null : guardianDao.findById(payment.getGuardianId()).orElse(null);
        // Facturas viejas pueden no tener nombre de archivo: se arma a partir del número
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

    // ====================== AYUDAS ======================

    /** Arma los datos de la factura, delega la escritura del PDF al generador y guarda la ruta del archivo. */
    private String generatePdf(Invoice invoice, Payment payment, Student student, Guardian payer, PaymentConcept concept) {
        // Descripción de la única línea de detalle
        String description = concept.getName() + " - " + student.getFullName()
                + (payment.getPeriod() != null ? " (período " + payment.getPeriod() + ")" : "");
        List<InvoiceLine> lines = List.of(new InvoiceLine(description, 1, invoice.getSubtotal(), invoice.getSubtotal()));
        // Texto que se codifica dentro del código QR (datos de la factura, al estilo de la DIAN)
        String qr = "NumFac: " + invoice.getNumber() + "\nFecFac: " + invoice.getIssueDate()
                + "\nNitFac: " + SchoolInfo.NIT_DIGITS + "\nDocAdq: " + (payer != null ? payer.getDocumentNumber() : "-")
                + "\nValFac: " + invoice.getSubtotal() + "\nValIva: " + invoice.getTax()
                + "\nValTolFac: " + invoice.getTotal() + "\nCUFE: " + invoice.getCufe();
        String path = invoiceGenerator.generate(new InvoiceDocument(invoice, payment, student, payer, lines, qr));
        invoiceDao.updateFile(invoice.getId(), invoice.getPdfFileName(), path);   // se guarda dónde quedó el PDF
        invoice.setFilePath(path);
        return path;
    }

    private Student getStudent(int studentId) {
        return studentDao.findById(studentId)
                .orElseThrow(() -> new BusinessException("No existe un estudiante con id " + studentId + "."));
    }
}
