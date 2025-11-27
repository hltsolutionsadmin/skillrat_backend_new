package com.skillrat.user.repo;

import com.skillrat.user.domain.EmployeeLeave;

import com.skillrat.user.domain.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmployeeLeaveDao extends JpaRepository<EmployeeLeave, UUID> {
    List<EmployeeLeave> findByEmployee_Id(UUID employeeId);
    List<EmployeeLeave> findByEmployee_IdAndStatus(UUID employeeId, LeaveStatus status);
    List<EmployeeLeave> findByEmployee_IdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID employeeId, LeaveStatus status, LocalDate end, LocalDate start);
}
