package com.littlehorizons.erp.service.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * IVA rules. Educational services (enrollment, monthly fee) are excluded from IVA in Colombia
 * (Estatuto Tributario, art. 476); goods such as uniforms pay 19 %.
 * <p>Prices in the catalog already INCLUDE the tax, so the tax is extracted from the total.</p>
 */
public final class TaxPolicy {

    public static final BigDecimal IVA_RATE = new BigDecimal("0.19");
    private static final Set<String> TAXED_CONCEPTS = Set.of("UNIFORME");

    private TaxPolicy() {
    }

    public static boolean isTaxed(String conceptCode) {
        return TAXED_CONCEPTS.contains(conceptCode);
    }

    /** @return subtotal before IVA */
    public static BigDecimal subtotal(BigDecimal total, String conceptCode) {
        return isTaxed(conceptCode)
                ? total.divide(BigDecimal.ONE.add(IVA_RATE), 2, RoundingMode.HALF_UP)
                : total.setScale(2, RoundingMode.HALF_UP);
    }

    /** @return IVA included in the total */
    public static BigDecimal tax(BigDecimal total, String conceptCode) {
        return total.setScale(2, RoundingMode.HALF_UP).subtract(subtotal(total, conceptCode));
    }
}
