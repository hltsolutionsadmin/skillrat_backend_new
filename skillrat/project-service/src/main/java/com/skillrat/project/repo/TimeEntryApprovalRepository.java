package com.skillrat.project.repo;

import com.skillrat.project.domain.TimeEntryApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimeEntryApprovalRepository extends JpaRepository<TimeEntryApproval, UUID> {
    List<TimeEntryApproval> findByTimeEntry_Id(UUID timeEntryId);
}
