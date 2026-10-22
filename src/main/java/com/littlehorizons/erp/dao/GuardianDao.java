package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.StudentGuardian;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de acudientes (tabla {@code acudientes}) y su vínculo con los estudiantes.
 */
public interface GuardianDao extends CrudDao<Guardian> {

    /**
     * Busca un acudiente por su número de documento.
     */
    Optional<Guardian> findByDocument(String documentNumber);

    /**
     * Acudientes de un estudiante (el principal sale primero).
     */
    List<Guardian> findByStudent(int studentId);

    /**
     * Vincula un acudiente con un estudiante (tabla estudiante_acudiente).
     */
    void linkStudent(StudentGuardian link);
}
