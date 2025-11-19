package com.skillrat.user.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.EmploymentType;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.User;
import com.skillrat.user.repo.EmployeeRepository;
import com.skillrat.user.repo.RoleRepository;
import com.skillrat.user.repo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<Employee> search(String q, EmploymentType type, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return employeeRepository.search(query, type, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getById(UUID id) {
        return employeeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public java.util.List<Employee> listByB2bUnit(UUID b2bUnitId) {
        return employeeRepository.findByB2bUnitId(b2bUnitId);
    }

    @Transactional
    public Employee create(UUID b2bUnitId,
                           String firstName,
                           String lastName,
                           String email,
                           String mobile,
                           String designation,
                           String department,
                           EmploymentType employmentType,
                           LocalDate hireDate,
                           UUID reportingManagerId,
                           java.util.List<UUID> roleIds) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        // Admin validations: unique email/mobile, name present
        userRepository.findByEmailIgnoreCase(email).ifPresent(u -> { throw new IllegalArgumentException("Email already in use"); });
        if (mobile != null && !mobile.isBlank()) {
            userRepository.findByMobile(mobile).ifPresent(u -> { throw new IllegalArgumentException("Mobile already in use"); });
        }
        Employee e = new Employee();
        e.setFirstName(Objects.requireNonNull(firstName, "firstName is required"));
        e.setLastName(Objects.requireNonNull(lastName, "lastName is required"));
        e.setUsername(email.toLowerCase());
        e.setEmail(email.toLowerCase());
        e.setMobile(mobile);
        e.setDesignation(designation);
        e.setDepartment(department);
        e.setEmploymentType(employmentType);
        e.setHireDate(hireDate);
        if (reportingManagerId != null) {
            User rm = userRepository.findById(reportingManagerId).orElseThrow(() -> new IllegalArgumentException("Reporting manager not found"));
            e.setReportingManager(rm);
        }
        e.setEmployeeCode("EMP-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        e.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        e.setActive(true);
        e.setTenantId(tenantId);
        e.setB2bUnitId(b2bUnitId);
        e.setPasswordNeedsReset(true);
        e.setPasswordSetupToken(UUID.randomUUID().toString());
        e.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            e.setRoles(roles);
        }
        return employeeRepository.save(e);
    }

    @Transactional
    public Employee update(UUID id,
                           String firstName,
                           String lastName,
                           String mobile,
                           String designation,
                           String department,
                           EmploymentType employmentType,
                           LocalDate hireDate,
                           UUID reportingManagerId) {
        Employee e = employeeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        if (firstName != null && !firstName.isBlank()) e.setFirstName(firstName.trim());
        if (lastName != null && !lastName.isBlank()) e.setLastName(lastName.trim());
        if (mobile != null && !mobile.isBlank()) {
            userRepository.findByMobile(mobile)
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> { throw new IllegalArgumentException("Mobile already in use"); });
            e.setMobile(mobile);
        }
        if (designation != null) e.setDesignation(designation);
        if (department != null) e.setDepartment(department);
        if (employmentType != null) e.setEmploymentType(employmentType);
        if (hireDate != null) e.setHireDate(hireDate);
        if (reportingManagerId != null) {
            User rm = userRepository.findById(reportingManagerId).orElseThrow(() -> new IllegalArgumentException("Reporting manager not found"));
            e.setReportingManager(rm);
        }
        return employeeRepository.save(e);
    }
}
