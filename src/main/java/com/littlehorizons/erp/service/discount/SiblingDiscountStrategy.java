package com.littlehorizons.erp.service.discount;

import com.littlehorizons.erp.model.StudentDiscount;
import java.math.BigDecimal;
import java.util.function.IntPredicate;

/**
 * Descuento por hermanos: solo aplica mientras el estudiante REALMENTE tenga un hermano activo en el colegio.
 * Si el hermano se gradúa o se retira, el descuento se detiene solo.
 */
public class SiblingDiscountStrategy implements DiscountStrategy {

    // Reutiliza el cálculo por porcentaje (composición en lugar de repetir código)
    private final PercentageDiscountStrategy percentage = new PercentageDiscountStrategy("Descuento por hermanos");
    // Función que dice, para un id de estudiante, si tiene un hermano activo
    private final IntPredicate hasActiveSibling;

    /**
     * @param hasActiveSibling indica, para el id de un estudiante, si actualmente tiene un hermano activo
     */
    public SiblingDiscountStrategy(IntPredicate hasActiveSibling) {
        this.hasActiveSibling = hasActiveSibling;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal baseAmount, StudentDiscount discount) {
        // Si tiene hermano activo se calcula el porcentaje; si no, el descuento es cero
        return hasActiveSibling.test(discount.getStudentId())
                ? percentage.calculateDiscount(baseAmount, discount)
                : BigDecimal.ZERO;
    }

    @Override
    public String describe(StudentDiscount discount) {
        return percentage.describe(discount);
    }
}
