package com.skillrat.user.repo;

import com.skillrat.user.domain.EmployeePayslipComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeePayslipComponentDao extends JpaRepository<EmployeePayslipComponent, UUID> {
    List<EmployeePayslipComponent> findByPayslip_Id(UUID payslipId);
}
