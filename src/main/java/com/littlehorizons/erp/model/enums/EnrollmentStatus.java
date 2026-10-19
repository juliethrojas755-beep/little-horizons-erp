package com.littlehorizons.erp.model.enums;

/**
 * Status of a yearly enrollment or a subject enrollment.
 */
public enum EnrollmentStatus implements DbEnum {
    ACTIVE("Activa"),
    COMPLETED("Completada"),
    WITHDRAWN("Retirada");

    private final String dbValue;

    EnrollmentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static EnrollmentStatus fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(EnrollmentStatus.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
