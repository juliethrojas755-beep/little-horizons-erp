package com.littlehorizons.erp.model.enums;

/**
 * Kind of school event.
 */
public enum EventType implements DbEnum {
    ACADEMIC("Académico"),
    CULTURAL("Cultural"),
    SPORTS("Deportivo"),
    SOCIAL("Social"),
    SCIENTIFIC("Científico");

    private final String dbValue;

    EventType(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static EventType fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(EventType.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
