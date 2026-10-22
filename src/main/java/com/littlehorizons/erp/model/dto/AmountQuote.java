package com.littlehorizons.erp.model.dto;

import java.math.BigDecimal;

/**
 * Precio de un concepto para un estudiante después de aplicar el mejor descuento.
 *
 * @param baseAmount     valor del catálogo antes del descuento
 * @param discountAmount valor que se descuenta (cero si no hay descuento)
 * @param finalAmount    valor final que debe pagar
 * @param discountLabel  descripción del descuento aplicado, o "Sin descuento"
 */
public record AmountQuote(BigDecimal baseAmount, BigDecimal discountAmount, BigDecimal finalAmount, String discountLabel) {
}
