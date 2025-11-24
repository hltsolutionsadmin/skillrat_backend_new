package com.skillrat.user.web;

import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.EmploymentType;
import org.springframework.security.access.prepost.PreAuthorize;
import com.skillrat.user.security.B2BUnitAccessValidator;
import com.skillrat.user.service.EmployeeService;
import com.skillrat.user.dto.EmployeeSummaryDto;
import com.skillrat.user.dto.PageResponse;
import com.skillrat.user.dto.UserBriefDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/employees")
@Validated
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final B2BUnitAccessValidator b2bUnitAccessValidator;

    public AdminEmployeeController(EmployeeService employeeService,
                                   B2BUnitAccessValidator b2bUnitAccessValidator) {
        this.employeeService = employeeService;
        this.b2bUnitAccessValidator = b2bUnitAccessValidator;
    }

    // List employees with filters and pagination, scoped to a B2B unit
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ADMIN','HR_ADMIN')")
    public PageResponse<EmployeeSummaryDto> search(@RequestParam("b2bUnitId") UUID b2bUnitId,
                                                   @RequestParam(value = "q", required = false) String q,
                                                   @RequestParam(value = "employmentType", required = false) EmploymentType employmentType,
                                                   Pageable pageable) {
        b2bUnitAccessValidator.validateCurrentUserBelongsTo(b2bUnitId);
        Page<Employee> page = employeeService.search(b2bUnitId, q, employmentType, pageable);

        List<EmployeeSummaryDto> items = page.getContent().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        return new PageResponse<>(items, page.getTotalElements(), page.getNumber(), page.getSize());
    }
    
    // Employee details
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Employee> get(@PathVariable("id") UUID id) {
        return employeeService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // List employees by b2bUnitId
    @GetMapping("/byb2b/{b2bUnitId}")
    @PreAuthorize("isAuthenticated()")
    public List<Employee> listByB2b(@PathVariable("b2bUnitId") UUID b2bUnitId) {
        return employeeService.listByB2bUnit(b2bUnitId);
    }

    // Create employee
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Employee> create(@RequestBody CreateEmployeeRequest req) {
        Employee e = employeeService.create(
                req.b2bUnitId,
                req.firstName,
                req.lastName,
                req.email,
                req.mobile,
                req.designation,
                req.department,
                req.employmentType,
                req.hireDate,
                req.reportingManagerId,
                req.roleIds
        );
        return ResponseEntity.ok(e);
    }

    // Update employee
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Employee> update(@PathVariable("id") UUID id, @RequestBody UpdateEmployeeRequest req) {
        Employee e = employeeService.update(
                id,
                req.firstName,
                req.lastName,
                req.mobile,
                req.designation,
                req.department,
                req.employmentType,
                req.hireDate,
                req.reportingManagerId
        );
        return ResponseEntity.ok(e);
    }

    public static class CreateEmployeeRequest {
        @NotNull public UUID b2bUnitId;
        @NotBlank public String firstName;
        @NotBlank public String lastName;
        @NotBlank @Email public String email;
        public String mobile;
        public String designation;
        public String department;
        public EmploymentType employmentType;
        public LocalDate hireDate;
        public UUID reportingManagerId;
        @NotEmpty public List<UUID> roleIds;
    }

    public static class UpdateEmployeeRequest {
        public String firstName;
        public String lastName;
        public String mobile;
        public String designation;
        public String department;
        public EmploymentType employmentType;
        public LocalDate hireDate;
        public UUID reportingManagerId;
    }

    private EmployeeSummaryDto toSummary(Employee e) {
        UserBriefDto manager = null;
        if (e.getReportingManager() != null) {
            manager = new UserBriefDto(
                    e.getReportingManager().getId(),
                    e.getReportingManager().getFirstName(),
                    e.getReportingManager().getLastName()
            );
        }
        return new EmployeeSummaryDto(
                e.getId(),
                e.getFirstName(),
                e.getLastName(),
                e.getEmail(),
                e.getMobile(),
                e.getEmployeeCode(),
                e.getDesignation(),
                e.getDepartment(),
                e.getHireDate(),
                e.getEmploymentType(),
                manager
        );
    }
}
