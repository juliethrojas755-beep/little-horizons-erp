package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.enums.Term;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link AcademicRecordDao} sobre la tabla {@code registros_academicos}.
 * Todo el SQL de calificaciones vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class AcademicRecordDaoJdbc extends BaseJdbcDao implements AcademicRecordDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM registros_academicos ";

    /**
     * Guarda una calificación nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(AcademicRecord r) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, anio_academico, periodo_academico, "
                        + "valor_nota, observaciones, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                r.getStudentId(), r.getSubjectId(), r.getTeacherId(), r.getAcademicYear(), r.getTerm(),
                r.getGrade(), r.getNotes(), r.getRecordDate());
    }

    /**
     * Actualiza los datos de una calificación.
     *
     * @return {@code true} si se modificó alguna fila
     */
    @Override
    public boolean update(AcademicRecord r) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE registros_academicos SET valor_nota = ?, observaciones = ?, fecha_registro = ? WHERE id_registro = ?",
                r.getGrade(), r.getNotes(), r.getRecordDate(), r.getId()) > 0;
    }

    /**
     * Busca una calificación por su id.
     */
    @Override
    public Optional<AcademicRecord> findById(int recordId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_registro = ?", this::map, recordId);
    }

    /**
     * Busca los registros de un estudiante.
     */
    @Override
    public List<AcademicRecord> findByStudent(int studentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_estudiante = ? ORDER BY anio_academico, periodo_academico, id_materia",
                this::map, studentId);
    }

    /**
     * Busca las notas de una materia en un año académico.
     */
    @Override
    public List<AcademicRecord> findBySubject(int subjectId, int academicYear) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_materia = ? AND anio_academico = ?", this::map, subjectId, academicYear);
    }

    /**
     * Busca todos los registros de un año académico.
     */
    @Override
    public List<AcademicRecord> findByYear(int academicYear) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE anio_academico = ?", this::map, academicYear);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto AcademicRecord.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
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
