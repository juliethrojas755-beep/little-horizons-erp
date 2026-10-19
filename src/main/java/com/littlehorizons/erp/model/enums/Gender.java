package com.littlehorizons.erp.model.enums;

/**
 * Student gender as stored in the database.
 */
public enum Gender implements DbEnum {
    MALE("M"),
    FEMALE("F"),
    OTHER("Otro");

    private final String dbValue;

    Gender(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static Gender fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(Gender.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
