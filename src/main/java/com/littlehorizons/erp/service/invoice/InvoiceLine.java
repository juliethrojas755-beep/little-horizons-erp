package com.littlehorizons.erp.service.invoice;

import java.math.BigDecimal;

/**
 * One detail row of the invoice.
 */
public record InvoiceLine(String description, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
}
