package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Schedule;
import java.util.List;

/**
 * Data access for schedule blocks ({@code horarios}).
 */
public interface ScheduleDao {

    int insert(Schedule schedule);

    boolean delete(int scheduleId);

    List<Schedule> findByAssignment(int assignmentId);

    /** Every block of every assignment of the teacher, used to detect conflicts. */
    List<Schedule> findByTeacher(int teacherId);

    /** Every block of the classroom, used to detect classroom conflicts. */
    List<Schedule> findByClassroom(String classroom);
}
