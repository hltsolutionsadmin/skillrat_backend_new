package com.skillrat.user.dto;

import com.skillrat.user.domain.EmploymentType;

import java.time.LocalDate;
import java.util.UUID;

public class UpdateEmployeeRequest {
    private String firstName;
    private String lastName;
    private String mobile;
    private String designation;
    private String department;
    private EmploymentType employmentType;
    private LocalDate hireDate;
    private UUID reportingManagerId;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMobile() { return mobile; }
    public String getDesignation() { return designation; }
    public String getDepartment() { return department; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public LocalDate getHireDate() { return hireDate; }
    public UUID getReportingManagerId() { return reportingManagerId; }
}
