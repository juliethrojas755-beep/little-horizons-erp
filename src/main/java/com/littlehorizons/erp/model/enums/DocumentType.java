package com.littlehorizons.erp.model.enums;

/**
 * Identity document types accepted by the school.
 */
public enum DocumentType implements DbEnum {
    TI("TI"),
    CC("CC"),
    CE("CE"),
    PASSPORT("Pasaporte");

    private final String dbValue;

    DocumentType(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static DocumentType fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(DocumentType.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
