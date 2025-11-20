package com.skillrat.user.service;

import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.EmploymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeService {
    Page<Employee> search(UUID b2bUnitId, String q, EmploymentType type, Pageable pageable);
    Optional<Employee> getById(UUID id);
    java.util.List<Employee> listByB2bUnit(UUID b2bUnitId);
    Employee create(UUID b2bUnitId,
                    String firstName,
                    String lastName,
                    String email,
                    String mobile,
                    String designation,
                    String department,
                    EmploymentType employmentType,
                    LocalDate hireDate,
                    UUID reportingManagerId,
                    java.util.List<UUID> roleIds);
    Employee update(UUID id,
                    String firstName,
                    String lastName,
                    String mobile,
                    String designation,
                    String department,
                    EmploymentType employmentType,
                    LocalDate hireDate,
                    UUID reportingManagerId);
}
