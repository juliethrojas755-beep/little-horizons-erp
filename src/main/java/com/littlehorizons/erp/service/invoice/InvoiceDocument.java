package com.littlehorizons.erp.service.invoice;

import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.Student;
import java.util.List;

/**
 * Everything a generator needs to draw the invoice. It decouples the PDF library from the services.
 */
public record InvoiceDocument(Invoice invoice, Payment payment, Student student, Guardian payer, List<InvoiceLine> lines,
                              String qrContent) {
}
