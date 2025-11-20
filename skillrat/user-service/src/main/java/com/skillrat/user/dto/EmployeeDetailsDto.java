package com.skillrat.user.dto;

import com.skillrat.user.domain.EmploymentType;
import java.time.LocalDate;
import java.util.UUID;

public class EmployeeDetailsDto {
    private UUID id;
    private UUID b2bUnitId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String employeeCode;
    private String designation;
    private String department;
    private LocalDate hireDate;
    private EmploymentType employmentType;
    private UserBriefDto reportingManager;
    private boolean active;

    public EmployeeDetailsDto() {}

    public EmployeeDetailsDto(UUID id, UUID b2bUnitId, String firstName, String lastName, String email, String mobile,
                              String employeeCode, String designation, String department, LocalDate hireDate,
                              EmploymentType employmentType, UserBriefDto reportingManager, boolean active) {
        this.id = id;
        this.b2bUnitId = b2bUnitId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.employeeCode = employeeCode;
        this.designation = designation;
        this.department = department;
        this.hireDate = hireDate;
        this.employmentType = employmentType;
        this.reportingManager = reportingManager;
        this.active = active;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getB2bUnitId() { return b2bUnitId; }
    public void setB2bUnitId(UUID b2bUnitId) { this.b2bUnitId = b2bUnitId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }
    public UserBriefDto getReportingManager() { return reportingManager; }
    public void setReportingManager(UserBriefDto reportingManager) { this.reportingManager = reportingManager; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
