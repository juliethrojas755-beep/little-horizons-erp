package com.littlehorizons.erp.model.enums;

/**
 * Status of an amount owed by a student.
 */
public enum ChargeStatus implements DbEnum {
    PENDING("Pendiente"),
    PAID("Pagado");

    private final String dbValue;

    ChargeStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static ChargeStatus fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(ChargeStatus.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
