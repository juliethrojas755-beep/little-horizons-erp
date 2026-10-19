package com.littlehorizons.erp.model.enums;

/**
 * Employment status of a teacher.
 */
public enum TeacherStatus implements DbEnum {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    ON_LEAVE("Licencia");

    private final String dbValue;

    TeacherStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static TeacherStatus fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(TeacherStatus.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
