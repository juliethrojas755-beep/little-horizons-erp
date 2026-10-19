package com.littlehorizons.erp.service.discount;

import com.littlehorizons.erp.model.enums.DiscountType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntPredicate;

/**
 * Factory pattern: returns the strategy that matches each {@link DiscountType}.
 * An {@link EnumMap} replaces a long switch, and the compiler-safe enum key avoids typos.
 */
public class DiscountStrategyFactory {

    private final Map<DiscountType, DiscountStrategy> strategies = new EnumMap<>(DiscountType.class);

    public DiscountStrategyFactory(IntPredicate hasActiveSibling) {
        strategies.put(DiscountType.SIBLINGS, new SiblingDiscountStrategy(hasActiveSibling));
        strategies.put(DiscountType.SCHOLARSHIP, new PercentageDiscountStrategy("Beca"));
        strategies.put(DiscountType.OTHER, new PercentageDiscountStrategy("Descuento especial"));
    }

    public DiscountStrategy forType(DiscountType type) {
        return strategies.get(type);
    }
}
