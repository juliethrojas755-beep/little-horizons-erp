package com.littlehorizons.erp.model.dto;

/**
 * Fila del medallero de los eventos con competencia.
 *
 * @param studentName nombre del estudiante
 * @param gold        cantidad de primeros lugares
 * @param silver      cantidad de segundos lugares
 * @param bronze      cantidad de terceros lugares
 */
public record MedalCount(String studentName, long gold, long silver, long bronze) {

    /** @return total de medallas del estudiante */
    public long total() {
        return gold + silver + bronze;
    }
}
