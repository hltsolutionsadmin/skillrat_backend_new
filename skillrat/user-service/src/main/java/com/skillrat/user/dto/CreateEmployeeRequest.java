package com.skillrat.user.dto;

import com.skillrat.user.domain.EmploymentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateEmployeeRequest {
    @NotNull private UUID b2bUnitId;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    private String mobile;
    private String designation;
    private String department;
    private EmploymentType employmentType;
    private LocalDate hireDate;
    private UUID reportingManagerId;
    @NotEmpty private List<UUID> roleIds;

    public UUID getB2bUnitId() { return b2bUnitId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getDesignation() { return designation; }
    public String getDepartment() { return department; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public LocalDate getHireDate() { return hireDate; }
    public UUID getReportingManagerId() { return reportingManagerId; }
    public List<UUID> getRoleIds() { return roleIds; }
}
