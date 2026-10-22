package com.littlehorizons.erp.util;

/**
 * Se lanza cuando se rompe una regla de negocio (cupo lleno, conflicto de horario, nota inválida...).
 * Su mensaje está pensado para mostrárselo tal cual al usuario.
 * <p>Extiende RuntimeException (excepción no verificada): no obliga a declarar throws en cada método.</p>
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
