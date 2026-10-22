package com.littlehorizons.erp.model.enums;

/**
 * Período al que aplica un pago.
 * <p>El nombre de la constante está en inglés (convención del código) y el texto entre paréntesis
 * es EXACTAMENTE el valor que se guarda en la base de datos (en español).</p>
 */
public enum PaymentPeriod implements DbEnum {
    // Constantes del enum: NOMBRE_EN_INGLÉS("texto guardado en la base de datos")
    I("I"),
    II("II"),
    III("III"),
    IV("IV"),
    ANNUAL("Anual");

    // Texto exacto que se guarda en la columna de la base de datos
    private final String dbValue;

    // Constructor: cada constante recibe su texto de la base de datos
    PaymentPeriod(String dbValue) {
        this.dbValue = dbValue;
    }

    /** Devuelve el texto que se guarda en la base de datos. */
    @Override
    public String getDbValue() {
        return dbValue;
    }

    /**
     * Convierte el texto leído de la base de datos en la constante del enum
     * (por ejemplo "Excusa Médica" pasa a MEDICAL_EXCUSE).
     */
    public static PaymentPeriod fromDbValue(String dbValue) {
        // La búsqueda genérica está en DbEnum; aquí solo se indica de qué enum se trata
        return DbEnum.fromDbValue(PaymentPeriod.class, dbValue);
    }

    /** Al mostrar la constante en pantalla se imprime el texto en español. */
    @Override
    public String toString() {
        return dbValue;
    }
}
