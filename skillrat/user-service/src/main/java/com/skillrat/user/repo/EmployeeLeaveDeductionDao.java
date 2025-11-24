package com.skillrat.user.repo;

import com.skillrat.user.domain.EmployeeLeaveDeduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeLeaveDeductionDao extends JpaRepository<EmployeeLeaveDeduction, UUID> {
    Optional<EmployeeLeaveDeduction> findByEmployeeIdAndMonthAndYear(UUID employeeId, int month, int year);
}
