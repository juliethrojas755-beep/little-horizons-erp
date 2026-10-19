package com.littlehorizons.erp.model.enums;

/**
 * Period a payment applies to.
 */
public enum PaymentPeriod implements DbEnum {
    I("I"),
    II("II"),
    III("III"),
    IV("IV"),
    ANNUAL("Anual");

    private final String dbValue;

    PaymentPeriod(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static PaymentPeriod fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(PaymentPeriod.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
