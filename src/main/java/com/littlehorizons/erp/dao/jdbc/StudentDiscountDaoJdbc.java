package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.StudentDiscountDao;
import com.littlehorizons.erp.model.StudentDiscount;
import com.littlehorizons.erp.model.enums.DiscountType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementación JDBC de {@link StudentDiscountDao} sobre la tabla {@code descuentos_estudiante}.
 * Todo el SQL de descuentos vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class StudentDiscountDaoJdbc extends BaseJdbcDao implements StudentDiscountDao {

    /**
     * Guarda un descuento nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(StudentDiscount d) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO descuentos_estudiante (id_estudiante, id_concepto, tipo_descuento, porcentaje_descuento, "
                        + "fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?, ?, ?)",
                d.getStudentId(), d.getConceptId(), d.getType(), d.getPercentage(), d.getStartDate(), d.getEndDate());
    }

    /**
     * Busca los registros de un estudiante.
     */
    @Override
    public List<StudentDiscount> findByStudent(int studentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM descuentos_estudiante WHERE id_estudiante = ?", this::map, studentId);
    }

    /**
     * Busca los descuentos de un estudiante para un concepto.
     */
    @Override
    public List<StudentDiscount> findByStudentAndConcept(int studentId, int conceptId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList("SELECT * FROM descuentos_estudiante WHERE id_estudiante = ? AND id_concepto = ?",
                this::map, studentId, conceptId);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto StudentDiscount.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private StudentDiscount map(ResultSet rs) throws SQLException {
        StudentDiscount discount = new StudentDiscount();
        discount.setId(rs.getInt("id_descuento"));
        discount.setStudentId(rs.getInt("id_estudiante"));
        discount.setConceptId(rs.getInt("id_concepto"));
        discount.setType(DiscountType.fromDbValue(rs.getString("tipo_descuento")));
        discount.setPercentage(rs.getBigDecimal("porcentaje_descuento"));
        discount.setStartDate(getDate(rs, "fecha_inicio"));
        discount.setEndDate(getDate(rs, "fecha_fin"));
        return discount;
    }
}
