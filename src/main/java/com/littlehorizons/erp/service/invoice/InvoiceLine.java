package com.littlehorizons.erp.service.invoice;

import java.math.BigDecimal;

/**
 * Una fila del detalle de la factura.
 *
 * @param description descripción del producto o servicio
 * @param quantity    cantidad
 * @param unitPrice   valor unitario
 * @param subtotal    cantidad por valor unitario
 */
public record InvoiceLine(String description, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
}
