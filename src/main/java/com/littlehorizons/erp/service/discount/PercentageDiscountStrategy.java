package com.littlehorizons.erp.service.discount;

import com.littlehorizons.erp.model.StudentDiscount;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Aplica el porcentaje guardado sobre el valor base. Se usa para becas y otros descuentos.
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    // Texto que acompaña al porcentaje (por ejemplo "Beca")
    private final String label;

    public PercentageDiscountStrategy(String label) {
        this.label = label;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal baseAmount, StudentDiscount discount) {
        // El porcentaje se limita al rango 0 a 100 por seguridad
        BigDecimal percentage = discount.getPercentage().min(ONE_HUNDRED).max(BigDecimal.ZERO);
        // descuento = base * porcentaje / 100, redondeado a 2 decimales
        return baseAmount.multiply(percentage).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public String describe(StudentDiscount discount) {
        // stripTrailingZeros quita ceros sobrantes (50.00 se muestra como 50)
        return label + " " + discount.getPercentage().stripTrailingZeros().toPlainString() + "%";
    }
}
