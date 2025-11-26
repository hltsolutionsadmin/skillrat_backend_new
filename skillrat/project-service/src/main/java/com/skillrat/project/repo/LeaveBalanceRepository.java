package com.skillrat.project.repo;

import com.skillrat.project.domain.LeaveBalance;
import com.skillrat.project.domain.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {
    Optional<LeaveBalance> findByEmployeeIdAndB2bUnitIdAndYearAndType(UUID employeeId, UUID b2bUnitId, Integer year, LeaveType type);
    List<LeaveBalance> findByEmployeeIdAndB2bUnitIdAndYear(UUID employeeId, UUID b2bUnitId, Integer year);
}
