package com.littlehorizons.erp.model.enums;

/**
 * School days used by schedules.
 */
public enum WeekDay implements DbEnum {
    MONDAY("Lunes"),
    TUESDAY("Martes"),
    WEDNESDAY("Miércoles"),
    THURSDAY("Jueves"),
    FRIDAY("Viernes");

    private final String dbValue;

    WeekDay(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static WeekDay fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(WeekDay.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
