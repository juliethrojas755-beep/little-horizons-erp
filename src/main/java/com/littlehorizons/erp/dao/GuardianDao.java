package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.StudentGuardian;
import java.util.List;
import java.util.Optional;

/**
 * Data access for guardians ({@code acudientes}) and their link with students.
 */
public interface GuardianDao extends CrudDao<Guardian> {

    Optional<Guardian> findByDocument(String documentNumber);

    List<Guardian> findByStudent(int studentId);

    void linkStudent(StudentGuardian link);
}
