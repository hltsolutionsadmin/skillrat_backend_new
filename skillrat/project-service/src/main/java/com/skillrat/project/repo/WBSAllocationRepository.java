package com.skillrat.project.repo;

import com.skillrat.project.domain.WBSAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WBSAllocationRepository extends JpaRepository<WBSAllocation, UUID> {
    List<WBSAllocation> findByMember_Id(UUID memberId);
    Optional<WBSAllocation> findFirstByMember_IdAndWbsElement_IdAndActive(UUID memberId, UUID wbsId, boolean active);
    List<WBSAllocation> findByMember_IdAndActive(UUID memberId, boolean active);
    List<WBSAllocation> findByMember_IdAndWbsElement_IdAndActiveAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID memberId, UUID wbsId, boolean active, LocalDate from, LocalDate to);
}
