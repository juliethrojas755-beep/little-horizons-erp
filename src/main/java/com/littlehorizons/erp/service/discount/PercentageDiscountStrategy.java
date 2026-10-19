package com.littlehorizons.erp.service.discount;

import com.littlehorizons.erp.model.StudentDiscount;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Applies the stored percentage over the base amount. Used for scholarships and other discounts.
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final String label;

    public PercentageDiscountStrategy(String label) {
        this.label = label;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal baseAmount, StudentDiscount discount) {
        BigDecimal percentage = discount.getPercentage().min(ONE_HUNDRED).max(BigDecimal.ZERO);
        return baseAmount.multiply(percentage).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public String describe(StudentDiscount discount) {
        return label + " " + discount.getPercentage().stripTrailingZeros().toPlainString() + "%";
    }
}
