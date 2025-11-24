package com.skillrat.user.repo;

import com.skillrat.user.domain.EmployeeSalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeSalaryStructureDao extends JpaRepository<EmployeeSalaryStructure, UUID> {
    List<EmployeeSalaryStructure> findByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);
    Optional<EmployeeSalaryStructure> findFirstByEmployeeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(UUID employeeId, LocalDate effectiveFrom);
}
