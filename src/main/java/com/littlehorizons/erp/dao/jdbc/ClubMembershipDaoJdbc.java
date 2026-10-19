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
 * JDBC implementation of {@link ClubMembershipDao} over {@code membresias_club}.
 */
public class ClubMembershipDaoJdbc extends BaseJdbcDao implements ClubMembershipDao {

    private static final String SELECT = "SELECT * FROM membresias_club ";

    @Override
    public int insert(ClubMembership m) {
        return insert("INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia) VALUES (?, ?, ?, ?)",
                m.getStudentId(), m.getClubId(), m.getJoinDate(), m.getStatus());
    }

    @Override
    public boolean withdraw(int membershipId, LocalDate date, String reason) {
        return update("UPDATE membresias_club SET estado_membresia = ?, fecha_retiro = ?, motivo_retiro = ? WHERE id_membresia = ?",
                MembershipStatus.WITHDRAWN, date, reason, membershipId) > 0;
    }

    @Override
    public List<ClubMembership> findByClub(int clubId) {
        return queryList(SELECT + "WHERE id_club = ? ORDER BY fecha_ingreso", this::map, clubId);
    }

    @Override
    public List<ClubMembership> findByStudent(int studentId) {
        return queryList(SELECT + "WHERE id_estudiante = ? ORDER BY fecha_ingreso DESC", this::map, studentId);
    }

    @Override
    public Optional<ClubMembership> findActive(int studentId, int clubId) {
        return queryOne(SELECT + "WHERE id_estudiante = ? AND id_club = ? AND estado_membresia = 'Activa'",
                this::map, studentId, clubId);
    }

    @Override
    public List<ClubMembership> findAll() {
        return queryList(SELECT + "ORDER BY id_club, fecha_ingreso", this::map);
    }

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
