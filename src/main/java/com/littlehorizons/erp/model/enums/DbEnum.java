package com.littlehorizons.erp.model.enums;

import java.util.Arrays;

/**
 * Contrato de los enums cuyos valores se guardan en español en la base de datos.
 * La constante de Java queda en inglés y {@link #getDbValue()} devuelve el texto exacto de la base de datos.
 */
public interface DbEnum {

    /** @return el valor exacto que se guarda en la columna de la base de datos */
    String getDbValue();

    /**
     * Busca la constante del enum que corresponde a un texto de la base de datos.
     * Es un método genérico: sirve para cualquier enum que implemente DbEnum.
     *
     * @param type    clase del enum donde se busca
     * @param dbValue texto leído de la base de datos
     * @return la constante encontrada, o {@code null} cuando la columna es NULL
     * @throws IllegalArgumentException cuando el texto no existe en el enum
     */
    static <E extends Enum<E> & DbEnum> E fromDbValue(Class<E> type, String dbValue) {
        // Una columna NULL en la base de datos se convierte en null en Java
        if (dbValue == null) {
            return null;
        }
        // getEnumConstants() devuelve todas las constantes del enum
        return Arrays.stream(type.getEnumConstants())
                // se queda con las que tengan el mismo texto que viene de la base de datos
                .filter(constant -> constant.getDbValue().equals(dbValue))
                // toma la primera coincidencia
                .findFirst()
                // si no hay ninguna, el dato de la base no es válido y se avisa con un error claro
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown value '" + dbValue + "' for " + type.getSimpleName()));
    }
}
