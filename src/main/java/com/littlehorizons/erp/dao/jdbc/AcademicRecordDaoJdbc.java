package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.enums.Term;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link AcademicRecordDao} over {@code registros_academicos}.
 */
public class AcademicRecordDaoJdbc extends BaseJdbcDao implements AcademicRecordDao {

    private static final String SELECT = "SELECT * FROM registros_academicos ";

    @Override
    public int insert(AcademicRecord r) {
        return insert("INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, anio_academico, periodo_academico, "
                        + "valor_nota, observaciones, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                r.getStudentId(), r.getSubjectId(), r.getTeacherId(), r.getAcademicYear(), r.getTerm(),
                r.getGrade(), r.getNotes(), r.getRecordDate());
    }

    @Override
    public boolean update(AcademicRecord r) {
        return update("UPDATE registros_academicos SET valor_nota = ?, observaciones = ?, fecha_registro = ? WHERE id_registro = ?",
                r.getGrade(), r.getNotes(), r.getRecordDate(), r.getId()) > 0;
    }

    @Override
    public Optional<AcademicRecord> findById(int recordId) {
        return queryOne(SELECT + "WHERE id_registro = ?", this::map, recordId);
    }

    @Override
    public List<AcademicRecord> findByStudent(int studentId) {
        return queryList(SELECT + "WHERE id_estudiante = ? ORDER BY anio_academico, periodo_academico, id_materia",
                this::map, studentId);
    }

    @Override
    public List<AcademicRecord> findBySubject(int subjectId, int academicYear) {
        return queryList(SELECT + "WHERE id_materia = ? AND anio_academico = ?", this::map, subjectId, academicYear);
    }

    @Override
    public List<AcademicRecord> findByYear(int academicYear) {
        return queryList(SELECT + "WHERE anio_academico = ?", this::map, academicYear);
    }

    private AcademicRecord map(ResultSet rs) throws SQLException {
        AcademicRecord record = new AcademicRecord();
        record.setId(rs.getInt("id_registro"));
        record.setStudentId(rs.getInt("id_estudiante"));
        record.setSubjectId(rs.getInt("id_materia"));
        record.setTeacherId(rs.getInt("id_profesor"));
        record.setAcademicYear(rs.getInt("anio_academico"));
        record.setTerm(Term.fromDbValue(rs.getString("periodo_academico")));
        record.setGrade(rs.getBigDecimal("valor_nota"));
        record.setNotes(rs.getString("observaciones"));
        record.setRecordDate(getDate(rs, "fecha_registro"));
        return record;
    }
}
