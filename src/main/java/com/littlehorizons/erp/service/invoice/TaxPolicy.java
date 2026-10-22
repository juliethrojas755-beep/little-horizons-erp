package com.littlehorizons.erp.service.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Reglas del IVA. Los servicios educativos (matrícula, pensión) están excluidos de IVA en Colombia
 * (Estatuto Tributario, art. 476); los bienes como los uniformes pagan 19 %.
 * <p>Los precios del catálogo YA INCLUYEN el impuesto, por eso el IVA se extrae del total.</p>
 */
public final class TaxPolicy {

    // Tarifa general del IVA en Colombia
    public static final BigDecimal IVA_RATE = new BigDecimal("0.19");
    // Códigos de concepto que sí pagan IVA
    private static final Set<String> TAXED_CONCEPTS = Set.of("UNIFORME");

    // Constructor privado: clase de utilidad
    private TaxPolicy() {
    }

    /** @return {@code true} si el concepto paga IVA */
    public static boolean isTaxed(String conceptCode) {
        return TAXED_CONCEPTS.contains(conceptCode);
    }

    /** @return subtotal antes de IVA */
    public static BigDecimal subtotal(BigDecimal total, String conceptCode) {
        // Con IVA: subtotal = total / 1.19. Sin IVA: el subtotal es el mismo total
        return isTaxed(conceptCode)
                ? total.divide(BigDecimal.ONE.add(IVA_RATE), 2, RoundingMode.HALF_UP)
                : total.setScale(2, RoundingMode.HALF_UP);
    }

    /** @return IVA incluido en el total */
    public static BigDecimal tax(BigDecimal total, String conceptCode) {
        // IVA = total - subtotal
        return total.setScale(2, RoundingMode.HALF_UP).subtract(subtotal(total, conceptCode));
    }
}
