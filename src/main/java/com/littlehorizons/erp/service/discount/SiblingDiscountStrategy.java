package com.littlehorizons.erp.service.discount;

import com.littlehorizons.erp.model.StudentDiscount;
import java.math.BigDecimal;
import java.util.function.IntPredicate;

/**
 * Sibling discount: it only applies while the student REALLY has a sibling in the school.
 * If the sibling graduates or leaves, the discount stops automatically.
 */
public class SiblingDiscountStrategy implements DiscountStrategy {

    private final PercentageDiscountStrategy percentage = new PercentageDiscountStrategy("Descuento por hermanos");
    private final IntPredicate hasActiveSibling;

    /**
     * @param hasActiveSibling tells, for a student id, whether a sibling is currently active
     */
    public SiblingDiscountStrategy(IntPredicate hasActiveSibling) {
        this.hasActiveSibling = hasActiveSibling;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal baseAmount, StudentDiscount discount) {
        return hasActiveSibling.test(discount.getStudentId())
                ? percentage.calculateDiscount(baseAmount, discount)
                : BigDecimal.ZERO;
    }

    @Override
    public String describe(StudentDiscount discount) {
        return percentage.describe(discount);
    }
}
