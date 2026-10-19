package com.littlehorizons.erp.model.dto;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;

/**
 * Result of confirming a payment: the stored payment, its invoice and the PDF path.
 */
public record PaymentReceipt(Payment payment, Invoice invoice, String pdfPath) {
}
