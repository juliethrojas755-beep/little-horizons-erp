package com.littlehorizons.erp.model.dto;

/**
 * Medal table row of competitive events.
 */
public record MedalCount(String studentName, long gold, long silver, long bronze) {

    public long total() {
        return gold + silver + bronze;
    }
}
