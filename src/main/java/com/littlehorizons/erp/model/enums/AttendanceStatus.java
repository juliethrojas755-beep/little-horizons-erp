package com.littlehorizons.erp.model.enums;

/**
 * Daily attendance status per subject.
 */
public enum AttendanceStatus implements DbEnum {
    PRESENT("Presente"),
    ABSENT("Ausente"),
    LATE("Tardanza"),
    MEDICAL_EXCUSE("Excusa Médica");

    private final String dbValue;

    AttendanceStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static AttendanceStatus fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(AttendanceStatus.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
