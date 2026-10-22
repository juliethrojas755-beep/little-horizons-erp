package com.littlehorizons.erp.model.dto;

/**
 * Ocupación de un club: miembros activos frente al cupo máximo.
 *
 * @param clubId        id del club
 * @param clubName      nombre del club
 * @param activeMembers miembros con estado Activa
 * @param maxCapacity   cupo máximo del club
 */
public record ClubOccupancy(int clubId, String clubName, int activeMembers, int maxCapacity) {

    /**
     * Un record también puede tener métodos propios.
     *
     * @return porcentaje de ocupación de 0 a 100
     */
    public double occupancyPercent() {
        // Si el cupo es 0 se devuelve 0 para evitar dividir entre cero
        return maxCapacity == 0 ? 0 : activeMembers * 100.0 / maxCapacity;
    }

    /** @return {@code true} cuando el club ya no tiene cupos disponibles */
    public boolean isFull() {
        return activeMembers >= maxCapacity;
    }
}
