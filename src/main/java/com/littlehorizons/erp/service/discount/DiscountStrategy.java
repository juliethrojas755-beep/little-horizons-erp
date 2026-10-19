package com.littlehorizons.erp.service.discount;

import com.littlehorizons.erp.model.StudentDiscount;
import java.math.BigDecimal;

/**
 * Strategy pattern: each discount type knows how to compute its own discount.
 * {@link com.littlehorizons.erp.service.PaymentService} only talks to this interface,
 * so a new kind of discount is a new class, not a new {@code if} (Open/Closed).
 */
public interface DiscountStrategy {

    /**
     * @param baseAmount price of the concept before discount
     * @param discount   discount record of the student
     * @return amount to subtract (never negative, never above the base)
     */
    BigDecimal calculateDiscount(BigDecimal baseAmount, StudentDiscount discount);

    /** @return text shown to the user, e.g. "Beca 50%" */
    String describe(StudentDiscount discount);
}
