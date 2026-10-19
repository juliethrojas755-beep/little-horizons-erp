package com.littlehorizons.erp.model.enums;

/**
 * Reason for a per-student discount.
 */
public enum DiscountType implements DbEnum {
    SIBLINGS("Hermanos"),
    SCHOLARSHIP("Beca"),
    OTHER("Otro");

    private final String dbValue;

    DiscountType(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static DiscountType fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(DiscountType.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
