package com.littlehorizons.erp.model.enums;

/**
 * Position reached in a competitive event.
 */
public enum EventPosition implements DbEnum {
    FIRST_PLACE("1er Lugar"),
    SECOND_PLACE("2do Lugar"),
    THIRD_PLACE("3er Lugar"),
    PARTICIPANT("Participante");

    private final String dbValue;

    EventPosition(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static EventPosition fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(EventPosition.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
