package com.skillrat.project.repo;

import com.skillrat.project.domain.LeaveRequest;
import com.skillrat.project.domain.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findByEmployeeIdAndStatus(UUID employeeId, LeaveStatus status);
    List<LeaveRequest> findByEmployeeIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(UUID employeeId, LocalDate to, LocalDate from);
}
