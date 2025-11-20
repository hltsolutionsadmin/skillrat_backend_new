package com.skillrat.user.web;

import com.skillrat.user.api.ApiResponse;
import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.EmploymentType;
import org.springframework.security.access.prepost.PreAuthorize;
import com.skillrat.user.security.B2BUnitAccessValidator;
import com.skillrat.user.service.EmployeeService;
import com.skillrat.user.dto.EmployeeSummaryDto;
import com.skillrat.user.dto.PageResponse;
import com.skillrat.user.dto.UserBriefDto;
import com.skillrat.user.dto.CreateEmployeeRequest;
import com.skillrat.user.dto.UpdateEmployeeRequest;
import com.skillrat.user.dto.EmployeeDetailsDto;
import com.skillrat.user.populator.EmployeePopulator;
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
    private final EmployeePopulator employeePopulator;

    public AdminEmployeeController(EmployeeService employeeService,
                                   B2BUnitAccessValidator b2bUnitAccessValidator,
                                   EmployeePopulator employeePopulator) {
        this.employeeService = employeeService;
        this.b2bUnitAccessValidator = b2bUnitAccessValidator;
        this.employeePopulator = employeePopulator;
    }

    // List employees with filters and pagination, scoped to a B2B unit
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ADMIN','HR_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeSummaryDto>>> search(@RequestParam("b2bUnitId") UUID b2bUnitId,
                                                   @RequestParam(value = "q", required = false) String q,
                                                   @RequestParam(value = "employmentType", required = false) EmploymentType employmentType,
                                                   Pageable pageable) {
        b2bUnitAccessValidator.validateCurrentUserBelongsTo(b2bUnitId);
        Page<Employee> page = employeeService.search(b2bUnitId, q, employmentType, pageable);

        List<EmployeeSummaryDto> items = page.getContent().stream()
                .map(employeePopulator::toSummary)
                .collect(Collectors.toList());

        PageResponse<EmployeeSummaryDto> body = new PageResponse<>(items, page.getTotalElements(), page.getNumber(), page.getSize());
        return ResponseEntity.ok(ApiResponse.ok(body));
    }
    
    // Employee details
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeDetailsDto>> get(@PathVariable("id") UUID id) {
        return employeeService.getById(id)
                .map(e -> ResponseEntity.ok(ApiResponse.ok(employeePopulator.toDetails(e))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // List employees by b2bUnitId
    @GetMapping("/byb2b/{b2bUnitId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmployeeSummaryDto>>> listByB2b(@PathVariable("b2bUnitId") UUID b2bUnitId) {
        List<EmployeeSummaryDto> items = employeeService.listByB2bUnit(b2bUnitId).stream()
                .map(employeePopulator::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    // Create employee
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeDetailsDto>> create(@RequestBody CreateEmployeeRequest req) {
        Employee e = employeeService.create(
                req.getB2bUnitId(),
                req.getFirstName(),
                req.getLastName(),
                req.getEmail(),
                req.getMobile(),
                req.getDesignation(),
                req.getDepartment(),
                req.getEmploymentType(),
                req.getHireDate(),
                req.getReportingManagerId(),
                req.getRoleIds()
        );
        return ResponseEntity.ok(ApiResponse.ok(employeePopulator.toDetails(e)));
    }

    // Update employee
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeDetailsDto>> update(@PathVariable("id") UUID id, @RequestBody UpdateEmployeeRequest req) {
        Employee e = employeeService.update(
                id,
                req.getFirstName(),
                req.getLastName(),
                req.getMobile(),
                req.getDesignation(),
                req.getDepartment(),
                req.getEmploymentType(),
                req.getHireDate(),
                req.getReportingManagerId()
        );
        return ResponseEntity.ok(ApiResponse.ok(employeePopulator.toDetails(e)));
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
