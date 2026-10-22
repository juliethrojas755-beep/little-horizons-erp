package com.littlehorizons.erp.service.invoice;

import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.Student;
import java.util.List;

/**
 * Todo lo que un generador necesita para dibujar la factura.
 * Separa la librería de PDF de los servicios: el generador no necesita consultar la base de datos.
 *
 * @param invoice   datos de la factura (número, CUFE, totales)
 * @param payment   pago asociado
 * @param student   estudiante beneficiario
 * @param payer     acudiente que paga (puede ser null)
 * @param lines     líneas del detalle
 * @param qrContent texto que se codifica dentro del código QR
 */
public record InvoiceDocument(Invoice invoice, Payment payment, Student student, Guardian payer, List<InvoiceLine> lines,
                              String qrContent) {
}
