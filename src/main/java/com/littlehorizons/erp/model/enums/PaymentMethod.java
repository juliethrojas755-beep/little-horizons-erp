package com.littlehorizons.erp.model.enums;

/**
 * Accepted payment methods.
 */
public enum PaymentMethod implements DbEnum {
    CASH("Efectivo"),
    TRANSFER("Transferencia"),
    DEBIT_CARD("Tarjeta Débito"),
    CREDIT_CARD("Tarjeta Crédito"),
    CHECK("Cheque");

    private final String dbValue;

    PaymentMethod(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static PaymentMethod fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(PaymentMethod.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
