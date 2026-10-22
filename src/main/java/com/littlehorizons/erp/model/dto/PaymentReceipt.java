package com.littlehorizons.erp.model.dto;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;

/**
 * Resultado de confirmar un pago: el pago guardado, su factura y la ruta del PDF.
 *
 * @param pdfPath ruta del PDF generado, o {@code null} si el pago se guardó pero el PDF falló
 */
public record PaymentReceipt(Payment payment, Invoice invoice, String pdfPath) {
}
