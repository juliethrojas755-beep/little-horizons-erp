package com.littlehorizons.erp.model.enums;

/**
 * Academic term. FINAL is only used for grades.
 */
public enum Term implements DbEnum {
    I("I"),
    II("II"),
    III("III"),
    IV("IV"),
    FINAL("Final");

    private final String dbValue;

    Term(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static Term fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(Term.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
