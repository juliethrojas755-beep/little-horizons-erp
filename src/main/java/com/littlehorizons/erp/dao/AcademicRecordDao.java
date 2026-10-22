package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de calificaciones (tabla {@code registros_academicos}).
 */
public interface AcademicRecordDao {

    /**
     * Guarda un registro nuevo.
     *
     * @return el id generado por la base de datos
     */
    int insert(AcademicRecord record);

    /**
     * Actualiza un registro existente.
     *
     * @return {@code true} si se actualizó alguna fila
     */
    boolean update(AcademicRecord record);

    /**
     * Busca un registro por su id.
     *
     * @return el registro, o vacío si no existe
     */
    Optional<AcademicRecord> findById(int recordId);

    /**
     * Notas de un estudiante.
     */
    List<AcademicRecord> findByStudent(int studentId);

    /**
     * Notas de una materia en un año.
     */
    List<AcademicRecord> findBySubject(int subjectId, int academicYear);

    /**
     * Todas las notas de un año académico.
     */
    List<AcademicRecord> findByYear(int academicYear);
}
