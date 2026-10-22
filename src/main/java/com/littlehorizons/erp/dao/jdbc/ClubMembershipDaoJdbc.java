package com.littlehorizons.erp.dao.jdbc;

import com.littlehorizons.erp.dao.ClubMembershipDao;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.enums.MembershipStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link ClubMembershipDao} sobre la tabla {@code membresias_club}.
 * Todo el SQL de membresías vive aquí; los servicios nunca ven una sentencia SQL.
 */
public class ClubMembershipDaoJdbc extends BaseJdbcDao implements ClubMembershipDao {

    // Parte común de las consultas: cada método le agrega su propio WHERE
    private static final String SELECT = "SELECT * FROM membresias_club ";

    /**
     * Guarda una membresía nuevo.
     *
     * @return el id generado por MySQL
     */
    @Override
    public int insert(ClubMembership m) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return insert("INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia) VALUES (?, ?, ?, ?)",
                m.getStudentId(), m.getClubId(), m.getJoinDate(), m.getStatus());
    }

    /**
     * Marca la membresía como Retirada con la fecha y el motivo.
     */
    @Override
    public boolean withdraw(int membershipId, LocalDate date, String reason) {
        // Los ? se reemplazan en orden por los valores de abajo: el orden de las columnas debe coincidir
        return update("UPDATE membresias_club SET estado_membresia = ?, fecha_retiro = ?, motivo_retiro = ? WHERE id_membresia = ?",
                MembershipStatus.WITHDRAWN, date, reason, membershipId) > 0;
    }

    /**
     * Busca las membresías de un club.
     */
    @Override
    public List<ClubMembership> findByClub(int clubId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_club = ? ORDER BY fecha_ingreso", this::map, clubId);
    }

    /**
     * Busca los registros de un estudiante.
     */
    @Override
    public List<ClubMembership> findByStudent(int studentId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "WHERE id_estudiante = ? ORDER BY fecha_ingreso DESC", this::map, studentId);
    }

    /**
     * Busca la membresía ACTIVA de un estudiante en un club.
     */
    @Override
    public Optional<ClubMembership> findActive(int studentId, int clubId) {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryOne(SELECT + "WHERE id_estudiante = ? AND id_club = ? AND estado_membresia = 'Activa'",
                this::map, studentId, clubId);
    }

    /**
     * Devuelve todos los registros de membresías.
     */
    @Override
    public List<ClubMembership> findAll() {
        // Consulta con ? (PreparedStatement); this::map convierte cada fila del resultado en un objeto
        return queryList(SELECT + "ORDER BY id_club, fecha_ingreso", this::map);
    }

    /**
     * Convierte la fila actual del ResultSet en un objeto ClubMembership.
     * Cada columna en español se copia al atributo en inglés que le corresponde.
     * Para las fechas y los enteros que pueden ser NULL se usan las ayudas de BaseJdbcDao.
     */
    private ClubMembership map(ResultSet rs) throws SQLException {
        ClubMembership membership = new ClubMembership();
        membership.setId(rs.getInt("id_membresia"));
        membership.setStudentId(rs.getInt("id_estudiante"));
        membership.setClubId(rs.getInt("id_club"));
        membership.setJoinDate(getDate(rs, "fecha_ingreso"));
        membership.setStatus(MembershipStatus.fromDbValue(rs.getString("estado_membresia")));
        membership.setWithdrawalDate(getDate(rs, "fecha_retiro"));
        membership.setWithdrawalReason(rs.getString("motivo_retiro"));
        return membership;
    }
}
