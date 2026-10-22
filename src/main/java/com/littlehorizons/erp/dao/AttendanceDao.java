package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Attendance;
import java.time.LocalDate;
import java.util.List;

/**
 * Acceso a datos de asistencia diaria (tabla {@code asistencias}).
 */
public interface AttendanceDao {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(Attendance attendance);

    /**
     * Asistencia de un estudiante, la más reciente primero.
     */
    List<Attendance> findByStudent(int studentId);

    /**
     * Asistencia registrada entre dos fechas (incluidas).
     */
    List<Attendance> findBetween(LocalDate from, LocalDate to);
}
