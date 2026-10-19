package com.littlehorizons.erp.model.enums;

import java.util.Arrays;

/**
 * Contract for enums whose values are stored in Spanish in the database.
 * The Java constant stays in English and {@link #getDbValue()} returns the exact database text.
 */
public interface DbEnum {

    /** @return the exact value stored in the database column */
    String getDbValue();

    /**
     * Finds the enum constant that matches a database value.
     *
     * @param type    enum class to search
     * @param dbValue value read from the database
     * @return the matching constant, or {@code null} when the column is NULL
     * @throws IllegalArgumentException when the value does not exist in the enum
     */
    static <E extends Enum<E> & DbEnum> E fromDbValue(Class<E> type, String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return Arrays.stream(type.getEnumConstants())
                .filter(constant -> constant.getDbValue().equals(dbValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown value '" + dbValue + "' for " + type.getSimpleName()));
    }
}
