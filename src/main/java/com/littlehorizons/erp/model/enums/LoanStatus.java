package com.littlehorizons.erp.model.enums;

/**
 * Status of a resource loan.
 */
public enum LoanStatus implements DbEnum {
    ACTIVE("Activo"),
    RETURNED("Devuelto"),
    OVERDUE("Vencido");

    private final String dbValue;

    LoanStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static LoanStatus fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(LoanStatus.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
