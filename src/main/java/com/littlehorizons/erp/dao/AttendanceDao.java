package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Attendance;
import java.time.LocalDate;
import java.util.List;

/**
 * Data access for daily attendance ({@code asistencias}).
 */
public interface AttendanceDao {

    int insert(Attendance attendance);

    List<Attendance> findByStudent(int studentId);

    List<Attendance> findBetween(LocalDate from, LocalDate to);
}
