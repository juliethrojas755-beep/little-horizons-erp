package com.littlehorizons.erp.service.discount;

import com.littlehorizons.erp.model.StudentDiscount;
import java.math.BigDecimal;

/**
 * Patrón Strategy: cada tipo de descuento sabe calcular su propio descuento.
 * {@link com.littlehorizons.erp.service.PaymentService} solo habla con esta interfaz, así que un nuevo
 * tipo de descuento es una clase nueva y no un nuevo {@code if} (principio Abierto/Cerrado).
 */
public interface DiscountStrategy {

    /**
     * Calcula cuánto se descuenta.
     *
     * @param baseAmount precio del concepto antes del descuento
     * @param discount   registro del descuento del estudiante
     * @return valor a restar (nunca negativo ni mayor que el precio base)
     */
    BigDecimal calculateDiscount(BigDecimal baseAmount, StudentDiscount discount);

    /** @return texto para mostrar al usuario, por ejemplo "Beca 50%" */
    String describe(StudentDiscount discount);
}
