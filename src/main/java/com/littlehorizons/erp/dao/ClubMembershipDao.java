package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ClubMembership;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data access for club memberships ({@code membresias_club}).
 */
public interface ClubMembershipDao {

    int insert(ClubMembership membership);

    boolean withdraw(int membershipId, LocalDate date, String reason);

    List<ClubMembership> findByClub(int clubId);

    List<ClubMembership> findByStudent(int studentId);

    Optional<ClubMembership> findActive(int studentId, int clubId);

    List<ClubMembership> findAll();
}
