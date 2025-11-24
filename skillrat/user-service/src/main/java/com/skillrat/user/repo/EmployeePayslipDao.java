package com.skillrat.user.repo;

import com.skillrat.user.domain.EmployeePayslip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeePayslipDao extends JpaRepository<EmployeePayslip, UUID> {
    Optional<EmployeePayslip> findByEmployeeIdAndMonthAndYear(UUID employeeId, int month, int year);
    List<EmployeePayslip> findByEmployeeIdOrderByYearDescMonthDesc(UUID employeeId);
}
