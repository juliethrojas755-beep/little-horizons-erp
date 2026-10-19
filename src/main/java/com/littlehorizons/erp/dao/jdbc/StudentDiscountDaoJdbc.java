package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.StudentDiscountDao;
import com.littlehorizons.erp.model.StudentDiscount;
import com.littlehorizons.erp.model.enums.DiscountType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC implementation of {@link StudentDiscountDao} over {@code descuentos_estudiante}.
 */
public class StudentDiscountDaoJdbc extends BaseJdbcDao implements StudentDiscountDao {

    @Override
    public int insert(StudentDiscount d) {
        return insert("INSERT INTO descuentos_estudiante (id_estudiante, id_concepto, tipo_descuento, porcentaje_descuento, "
                        + "fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?, ?, ?)",
                d.getStudentId(), d.getConceptId(), d.getType(), d.getPercentage(), d.getStartDate(), d.getEndDate());
    }

    @Override
    public List<StudentDiscount> findByStudent(int studentId) {
        return queryList("SELECT * FROM descuentos_estudiante WHERE id_estudiante = ?", this::map, studentId);
    }

    @Override
    public List<StudentDiscount> findByStudentAndConcept(int studentId, int conceptId) {
        return queryList("SELECT * FROM descuentos_estudiante WHERE id_estudiante = ? AND id_concepto = ?",
                this::map, studentId, conceptId);
    }

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
