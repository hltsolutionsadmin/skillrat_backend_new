package com.skillrat.user.web;

import com.skillrat.user.domain.Employee;
import com.skillrat.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/business-units/{b2bUnitId}/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserService userService;

    @GetMapping
    //@PreAuthorize("hasAnyRole('BUSINESS_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @PathVariable UUID b2bUnitId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<Employee> employees = userService.getAllEmployees(b2bUnitId, search, pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<Employee> getEmployeeById(
            @PathVariable UUID b2bUnitId,
            @PathVariable UUID employeeId) {
        
        Employee employee = userService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employee);
    }


    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable UUID b2bUnitId,
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        
        Employee employee = userService.updateEmployee(
            employeeId,
            b2bUnitId,
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getMobile(),
            request.getDesignation(),
            request.getDepartment(),
            request.getActive(),
            request.getRoleIds()
        );
        
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN', 'HR_ADMIN')")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable UUID b2bUnitId,
            @PathVariable UUID employeeId) {
        
        userService.deleteEmployee(employeeId, b2bUnitId);
        return ResponseEntity.noContent().build();
    }

    // Request DTOs
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class CreateEmployeeRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @NotBlank @Email private String email;
        private String mobile;
        private String designation;
        private String department;
        private List<UUID> roleIds;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    public static class UpdateEmployeeRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @NotBlank @Email private String email;
        private String mobile;
        private String designation;
        private String department;
        private Boolean active;
        private List<UUID> roleIds;
    }
}
