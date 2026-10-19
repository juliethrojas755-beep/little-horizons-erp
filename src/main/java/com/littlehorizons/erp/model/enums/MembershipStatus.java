package com.littlehorizons.erp.model.enums;

/**
 * Status of a student inside a club.
 */
public enum MembershipStatus implements DbEnum {
    ACTIVE("Activa"),
    WITHDRAWN("Retirada");

    private final String dbValue;

    MembershipStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static MembershipStatus fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(MembershipStatus.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
