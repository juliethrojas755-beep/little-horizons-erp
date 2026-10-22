package com.littlehorizons.erp.service.discount;

import com.littlehorizons.erp.model.enums.DiscountType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntPredicate;

/**
 * Patrón Factory: devuelve la estrategia que corresponde a cada {@link DiscountType}.
 * Un {@link EnumMap} reemplaza un switch largo, y la llave de tipo enum evita errores de escritura.
 */
public class DiscountStrategyFactory {

    // Una estrategia por cada tipo de descuento
    private final Map<DiscountType, DiscountStrategy> strategies = new EnumMap<>(DiscountType.class);

    public DiscountStrategyFactory(IntPredicate hasActiveSibling) {
        strategies.put(DiscountType.SIBLINGS, new SiblingDiscountStrategy(hasActiveSibling));
        strategies.put(DiscountType.SCHOLARSHIP, new PercentageDiscountStrategy("Beca"));
        strategies.put(DiscountType.OTHER, new PercentageDiscountStrategy("Descuento especial"));
    }

    /** Entrega la estrategia del tipo de descuento indicado. */
    public DiscountStrategy forType(DiscountType type) {
        return strategies.get(type);
    }
}
