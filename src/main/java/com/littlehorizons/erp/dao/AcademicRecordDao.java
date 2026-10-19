package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import java.util.List;
import java.util.Optional;

/**
 * Data access for grades ({@code registros_academicos}).
 */
public interface AcademicRecordDao {

    int insert(AcademicRecord record);

    boolean update(AcademicRecord record);

    Optional<AcademicRecord> findById(int recordId);

    List<AcademicRecord> findByStudent(int studentId);

    List<AcademicRecord> findBySubject(int subjectId, int academicYear);

    List<AcademicRecord> findByYear(int academicYear);
}
