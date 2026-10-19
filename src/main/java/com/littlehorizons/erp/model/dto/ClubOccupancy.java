package com.littlehorizons.erp.model.dto;

/**
 * Members versus capacity of a club.
 */
public record ClubOccupancy(int clubId, String clubName, int activeMembers, int maxCapacity) {

    /** @return occupancy percentage from 0 to 100 */
    public double occupancyPercent() {
        return maxCapacity == 0 ? 0 : activeMembers * 100.0 / maxCapacity;
    }

    public boolean isFull() {
        return activeMembers >= maxCapacity;
    }
}
