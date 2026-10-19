package com.littlehorizons.erp.model.enums;

/**
 * Availability of a school resource.
 */
public enum ResourceStatus implements DbEnum {
    AVAILABLE("Disponible"),
    LOANED("Prestado"),
    IN_MAINTENANCE("En Mantenimiento"),
    DECOMMISSIONED("Dado de Baja");

    private final String dbValue;

    ResourceStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }

    /** Converts a database value into this enum. */
    public static ResourceStatus fromDbValue(String dbValue) {
        return DbEnum.fromDbValue(ResourceStatus.class, dbValue);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
