package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.TeacherAssignment;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de asignaciones profesor-materia (tabla {@code asignaciones_profesor}).
 */
public interface TeacherAssignmentDao {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(TeacherAssignment assignment);

    /**
     * Elimina un registro.
     *
     * @return {@code true} si se eliminó alguna fila
     */
    boolean delete(int assignmentId);

    /**
     * Busca un registro por su id.
     *
     * @return el registro, o vacío si no existe
     */
    Optional<TeacherAssignment> findById(int assignmentId);

    /**
     * Asignaciones de un profesor.
     */
    List<TeacherAssignment> findByTeacher(int teacherId);

    /**
     * Profesor encargado de una materia en un año; se usa al registrar una nota.
     */
    Optional<TeacherAssignment> findBySubjectAndYear(int subjectId, int academicYear);

    /**
     * Devuelve todos los registros.
     */
    List<TeacherAssignment> findAll();
}
