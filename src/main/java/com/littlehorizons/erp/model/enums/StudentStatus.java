package com.littlehorizons.erp.model.enums;

/**
 * Lifecycle status of a student.
 */
public enum StudentStatus implements DbEnum {
    ACTIVE("Activo"),
    GRADUATED("Graduado"),
    WITHDRAWN("Retirado"),
    SUSPENDED("Suspendido");

    private final String dbValue;

    StudentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static StudentStatus fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(StudentStatus.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
