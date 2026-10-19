package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;

/**
 * Price of a concept for one student after applying the best available discount.
 *
 * @param discountLabel description of the applied discount, or "Sin descuento"
 */
public record AmountQuote(BigDecimal baseAmount, BigDecimal discountAmount, BigDecimal finalAmount, String discountLabel) {
}
