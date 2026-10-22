package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Schedule;
import java.util.List;

/**
 * Acceso a datos de bloques de horario (tabla {@code horarios}).
 */
public interface ScheduleDao {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(Schedule schedule);

    /**
     * Elimina un registro.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    boolean delete(int scheduleId);

    /**
     * Bloques de una asignación.
     */
    List<Schedule> findByAssignment(int assignmentId);

    /**
     * Todos los bloques de todas las asignaciones del profesor; sirve para detectar conflictos.
     */
    List<Schedule> findByTeacher(int teacherId);

    /**
     * Todos los bloques de un salón; sirve para detectar choques de salón.
     */
    List<Schedule> findByClassroom(String classroom);
}
