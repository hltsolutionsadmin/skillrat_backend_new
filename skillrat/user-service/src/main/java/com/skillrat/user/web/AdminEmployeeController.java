package com.skillrat.user.web;

import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.EmploymentType;
import org.springframework.security.access.prepost.PreAuthorize;
import com.skillrat.user.service.EmployeeService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/employees")
@Validated
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    public AdminEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // List employees with filters and pagination
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<Employee> search(@RequestParam(value = "q", required = false) String q,
                                 @RequestParam(value = "employmentType", required = false) EmploymentType employmentType,
                                 Pageable pageable) {
        return employeeService.search(q, employmentType, pageable);
    }

    // Employee details
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Employee> get(@PathVariable("id") UUID id) {
        return employeeService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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
}
