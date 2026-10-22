package com.littlehorizons.erp.view;

import com.littlehorizons.erp.AppContext;
import com.littlehorizons.erp.model.Charge;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.dto.AmountQuote;
import com.littlehorizons.erp.model.dto.DelinquentAccount;
import com.littlehorizons.erp.model.dto.PaymentReceipt;
import com.littlehorizons.erp.model.enums.DiscountType;
import com.littlehorizons.erp.model.enums.PaymentMethod;
import com.littlehorizons.erp.model.enums.PaymentPeriod;
import com.littlehorizons.erp.util.Ansi;
import com.littlehorizons.erp.util.Formats;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.OperationCancelledException;
import com.littlehorizons.erp.view.components.ConsoleUI;
import com.littlehorizons.erp.view.components.Menu;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Módulo 5: conceptos, descuentos, cargos, pagos, facturas y morosos.
 * <p>Solo pide datos, llama al controlador y dibuja el resultado; los descuentos, el IVA y la transacción del pago están en el servicio.</p>
 */
public class FinanceView extends BaseView {

    // Título que aparece en el encabezado del menú
    private static final String MODULE = "Gestión Financiera";

    public FinanceView(AppContext app, InputReader input) {
        super(app, input);
    }

    /**
     * Arma el menú del módulo: cada opción es un texto y un método (referencia con ::).
     */
    @Override
    public void show() {
        new Menu(MODULE, input)
                .option("$", "Registrar pago y generar factura PDF", this::registerPayment)
                .option("⌕", "Consultar valor a pagar (con descuentos)", this::quote)
                .option("✚", "Generar cargo pendiente", this::createCharge)
                .separator()
                .option("☰", "Historial de pagos por estudiante", this::historyByStudent)
                .option("☰", "Historial de pagos por acudiente", this::historyByGuardian)
                .option("⚠", "Reporte de morosos (más de un mes)", this::delinquents)
                .separator()
                .option("☰", "Catálogo de conceptos de pago", this::concepts)
                .option("✚", "Crear concepto de pago", this::createConcept)
                .option("★", "Asignar descuento o beca a un estudiante", this::addDiscount)
                .separator()
                .option("☰", "Facturas emitidas", this::invoices)
                .option("↺", "Volver a generar el PDF de una factura", this::regenerateInvoice)
                .show();
    }

    private void registerPayment() {
        // PASO 1: el estudiante que recibe el servicio
        ConsoleUI.section("1. Estudiante");
        Student student = selectStudent();

        // PASO 2: el acudiente que paga (debe estar vinculado al estudiante)
        ConsoleUI.section("2. Acudiente pagador");
        List<Guardian> guardians = require(app.students().getGuardians(student.getId()));
        if (guardians.isEmpty()) {
            ConsoleUI.error("El estudiante no tiene acudientes vinculados.");
            throw new OperationCancelledException();
        }
        // Si hay un solo acudiente se usa; si hay varios, se escoge cuál paga
        Guardian payer = guardians.size() == 1 ? guardians.get(0)
                : input.readChoice("Acudiente que paga", guardians, g -> g.getFullName() + " (" + g.getRelationship() + ")");
        ConsoleUI.info("Pagador: " + payer.getFullName());

        // PASO 3: qué se paga: un cargo pendiente o un concepto del catálogo
        ConsoleUI.section("3. Qué se paga");
        Map<Integer, PaymentConcept> concepts = require(app.finance().conceptsById());
        // Cargos que el estudiante debe actualmente
        List<Charge> pending = require(app.finance().getPendingCharges(student.getId()));
        Integer chargeId = null;
        PaymentConcept concept;
        BigDecimal amount;
        if (!pending.isEmpty() && input.readYesNo("El estudiante tiene " + pending.size() + " cargo(s) pendiente(s). ¿Pagar uno?")) {
            // Pagar un cargo pendiente: el valor ya viene con el descuento aplicado
            Charge charge = input.readChoice("Cargo", pending, c -> concepts.get(c.getConceptId()).getName() + " · "
                    + Formats.money(c.getAmount()) + " · vence " + Formats.date(c.getDueDate()));
            chargeId = charge.getId();
            concept = concepts.get(charge.getConceptId());
            amount = charge.getAmount();
        } else {
            concept = input.readChoice("Concepto", require(app.finance().findActiveConcepts()),
                    c -> c.getName() + " · " + Formats.money(c.getBaseAmount()));
            // Pago directo: se calcula el valor del concepto con el mejor descuento
            AmountQuote quote = require(app.finance().quote(student.getId(), concept.getId()));
            printQuote(quote);
            amount = quote.finalAmount();
        }

        // PASO 4: método de pago y período
        ConsoleUI.section("4. Medio de pago");
        PaymentMethod method = chooseEnum("Método", PaymentMethod.class);
        PaymentPeriod period = chooseEnum("Período al que aplica", PaymentPeriod.class);

        // Resumen para que el usuario revise antes de confirmar
        ConsoleUI.card("Resumen del pago", List.of(
                new String[]{"Estudiante", student.getFullName()},
                new String[]{"Pagador", payer.getFullName()},
                new String[]{"Concepto", concept.getName()},
                new String[]{"Método", method.getDbValue()},
                new String[]{"Total", Ansi.BOLD + Ansi.BRIGHT_GREEN.apply(Formats.money(amount))}));
        // Si no confirma, se cancela y no se guarda nada
        if (!input.readYesNo("¿Confirmar el pago?")) {
            throw new OperationCancelledException();
        }
        // El servicio guarda pago, cargo y factura en una transacción y genera el PDF
        PaymentReceipt receipt = require(app.finance().confirmPayment(student.getId(), concept.getId(), chargeId,
                payer.getId(), method, period));
        ConsoleUI.success("Pago confirmado · Recibo " + receipt.payment().getReceiptNumber());
        ConsoleUI.card("Factura electrónica " + receipt.invoice().getNumber(), List.of(
                new String[]{"Subtotal", Formats.money(receipt.invoice().getSubtotal())},
                new String[]{"IVA", Formats.money(receipt.invoice().getTax())},
                new String[]{"Total", Formats.money(receipt.invoice().getTotal())},
                new String[]{"CUFE", ConsoleUI.truncate(receipt.invoice().getCufe(), 48)}));
        // Si el PDF falló, el pago igual quedó guardado y se puede regenerar
        if (receipt.pdfPath() != null) {
            ConsoleUI.success("PDF guardado en: " + Ansi.BOLD + receipt.pdfPath());
        } else {
            ConsoleUI.warning("El pago quedó guardado, pero el PDF no se pudo generar. Use la opción 'Volver a generar'.");
        }
    }

    /**
     * Consulta cuánto debe pagar un estudiante por un concepto, con su mejor descuento.
     */
    private void quote() {
        Student student = selectStudent();
        PaymentConcept concept = input.readChoice("Concepto", require(app.finance().findActiveConcepts()), PaymentConcept::getName);
        printQuote(require(app.finance().quote(student.getId(), concept.getId())));
    }

    /**
     * Genera un cargo pendiente (un valor que el estudiante debe) con su fecha de vencimiento.
     */
    private void createCharge() {
        Student student = selectStudent();
        PaymentConcept concept = input.readChoice("Concepto", require(app.finance().findActiveConcepts()), PaymentConcept::getName);
        printQuote(require(app.finance().quote(student.getId(), concept.getId())));
        ConsoleUI.show(app.finance().createCharge(student.getId(), concept.getId(), input.readDate("Fecha de vencimiento")));
    }

    /**
     * Historial de pagos de un estudiante y sus cargos pendientes.
     */
    private void historyByStudent() {
        Student student = selectStudent();
        List<Payment> payments = require(app.finance().paymentsByStudent(student.getId()));
        printPayments(payments);
        printPending(student);
    }

    /**
     * Historial de pagos de todos los estudiantes de un acudiente.
     */
    private void historyByGuardian() {
        Guardian guardian = selectGuardian();
        // Tabla id -> nombre de los estudiantes del acudiente
        Map<Integer, String> names = require(app.students().getStudentsOfGuardian(guardian.getId())).stream()
                .collect(Collectors.toMap(Student::getId, Student::getFullName));
        ConsoleUI.info("Estudiantes a cargo: " + String.join(", ", names.values()));
        printPayments(require(app.finance().paymentsByGuardian(guardian.getId())));
    }

    /**
     * Reporte de morosos: cargos pendientes por más de un mes, con días de mora y cartera total.
     */
    private void delinquents() {
        List<DelinquentAccount> accounts = require(app.finance().delinquentAccounts());
        BaseView.<DelinquentAccount>table()
                .column("Estudiante", DelinquentAccount::studentName)
                .column("Acudiente", DelinquentAccount::guardianName)
                .column("Teléfono", DelinquentAccount::guardianPhone)
                .column("Concepto", DelinquentAccount::conceptName)
                .numberColumn("Valor", a -> Formats.money(a.amount()))
                .column("Vencía", a -> Formats.date(a.dueDate()))
                .numberColumn("Días mora", a -> Ansi.RED.apply(String.valueOf(a.daysOverdue())))
                .print(accounts);
        // Cartera vencida total: suma de los valores de todos los morosos
        BigDecimal total = accounts.stream().map(DelinquentAccount::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!accounts.isEmpty()) {
            ConsoleUI.warning("Cartera vencida total: " + Ansi.BOLD + Formats.money(total));
        }
    }

    /**
     * Catálogo de conceptos de pago.
     */
    private void concepts() {
        BaseView.<PaymentConcept>table()
                .column("Código", PaymentConcept::getCode)
                .column("Concepto", PaymentConcept::getName)
                .numberColumn("Valor base", c -> Formats.money(c.getBaseAmount()))
                .column("Recurrente", c -> c.isRecurring() ? "Mensual" : "Único")
                .column("Descripción", PaymentConcept::getDescription)
                .print(require(app.finance().findActiveConcepts()));
    }

    /**
     * Pide los datos de un concepto de pago nuevo y lo crea.
     */
    private void createConcept() {
        PaymentConcept concept = new PaymentConcept();
        concept.setCode(input.readText("Código (ej: EXCURSION)").toUpperCase());
        concept.setName(input.readText("Nombre"));
        concept.setDescription(input.readOptionalText("Descripción"));
        concept.setBaseAmount(input.readAmount("Valor base en pesos"));
        concept.setRecurring(input.readYesNo("¿Es un cobro mensual?"));
        ConsoleUI.show(app.finance().createConcept(concept));
    }

    /**
     * Asigna un descuento o beca a un estudiante y muestra cómo queda el valor a pagar.
     */
    private void addDiscount() {
        Student student = selectStudent();
        PaymentConcept concept = input.readChoice("Concepto", require(app.finance().findActiveConcepts()), PaymentConcept::getName);
        DiscountType type = chooseEnum("Tipo de descuento", DiscountType.class);
        // Porcentaje entre 1 y 100; la lambda final es la regla de validación
        BigDecimal percentage = input.read("Porcentaje (1 a 100)", BigDecimal::new,
                value -> value.signum() > 0 && value.compareTo(BigDecimal.valueOf(100)) <= 0, "Ingrese un valor entre 1 y 100.");
        if (ConsoleUI.show(app.finance().addDiscount(student.getId(), concept.getId(), type, percentage))) {
            printQuote(require(app.finance().quote(student.getId(), concept.getId())));
        }
    }

    /**
     * Lista las facturas emitidas (las 30 más recientes).
     */
    private void invoices() {
        BaseView.<Invoice>table()
                .column("Número", Invoice::getNumber)
                .column("Fecha", i -> Formats.date(i.getIssueDate()))
                .numberColumn("Subtotal", i -> Formats.money(i.getSubtotal()))
                .numberColumn("IVA", i -> Formats.money(i.getTax()))
                .numberColumn("Total", i -> Formats.money(i.getTotal()))
                .column("Archivo", i -> i.getPdfFileName() == null ? "-" : i.getPdfFileName())
                .print(require(app.finance().findAllInvoices()).stream().limit(30).toList());
        ConsoleUI.info("Se muestran las 30 facturas más recientes.");
    }

    /**
     * Vuelve a generar el PDF de una factura a partir de su número.
     */
    private void regenerateInvoice() {
        // El número se convierte a mayúsculas (fe-000012 pasa a FE-000012)
        String number = input.readText("Número de factura (ej: FE-000012)").toUpperCase();
        String path = require(app.finance().regenerateInvoice(number));
        ConsoleUI.success("PDF guardado en: " + Ansi.BOLD + path);
    }

    // ====================== AYUDAS ======================

    /**
     * Muestra la ficha con valor base, descuento y total a pagar.
     */
    private void printQuote(AmountQuote quote) {
        ConsoleUI.card("Valor a pagar", List.of(
                new String[]{"Valor base", Formats.money(quote.baseAmount())},
                new String[]{"Descuento", quote.discountAmount().signum() > 0
                        ? Ansi.GREEN.apply("- " + Formats.money(quote.discountAmount()) + "  (" + quote.discountLabel() + ")")
                        : Ansi.GRAY.apply(quote.discountLabel())},
                new String[]{"Total", Ansi.BOLD + Ansi.BRIGHT_GREEN.apply(Formats.money(quote.finalAmount()))}));
    }

    /**
     * Imprime pagos en una tabla y el total pagado.
     */
    private void printPayments(List<Payment> payments) {
        Map<Integer, PaymentConcept> concepts = require(app.finance().conceptsById());
        BaseView.<Payment>table()
                .column("Recibo", Payment::getReceiptNumber)
                .column("Fecha", p -> Formats.date(p.getPaymentDate()))
                .column("Concepto", p -> concepts.get(p.getConceptId()).getName())
                .column("Método", p -> p.getMethod().getDbValue())
                .numberColumn("Valor", p -> Formats.money(p.getAmount()))
                .print(payments);
        // Total pagado: suma de los valores de todos los pagos
        BigDecimal total = payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        ConsoleUI.info("Total pagado: " + Ansi.BOLD + Formats.money(total));
    }

    /**
     * Muestra los cargos pendientes de un estudiante, o avisa que está al día.
     */
    private void printPending(Student student) {
        List<Charge> pending = require(app.finance().getPendingCharges(student.getId()));
        if (pending.isEmpty()) {
            ConsoleUI.success("Está al día: no tiene cargos pendientes.");
            return;
        }
        Map<Integer, PaymentConcept> concepts = require(app.finance().conceptsById());
        ConsoleUI.section("Cargos pendientes");
        BaseView.<Charge>table()
                .column("Concepto", c -> concepts.get(c.getConceptId()).getName())
                .numberColumn("Valor", c -> Formats.money(c.getAmount()))
                .column("Vence", c -> Formats.date(c.getDueDate()))
                .column("Estado", c -> ConsoleUI.status(c.getStatus().getDbValue()))
                .print(pending);
    }
}
