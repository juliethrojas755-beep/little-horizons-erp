package com.littlehorizons.erp.model.dto;

/**
 * Academic load of a teacher in a year.
 */
public record TeacherLoad(String teacherName, String specialization, long subjects, long weeklyBlocks) {
}
