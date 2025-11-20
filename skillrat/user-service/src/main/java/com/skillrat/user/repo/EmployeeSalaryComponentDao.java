package com.skillrat.user.repo;

import com.skillrat.user.domain.EmployeeSalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeSalaryComponentDao extends JpaRepository<EmployeeSalaryComponent, UUID> {
    List<EmployeeSalaryComponent> findBySalaryStructureId(UUID salaryStructureId);
}
