package com.skillrat.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends User {

    // In SINGLE_TABLE inheritance this column exists for all rows, so it must be nullable
    // to allow base User inserts where employeeCode is not applicable.
    @Column(nullable = true, unique = true, length = 64)
    private String employeeCode;

    @Column(length = 128)
    private String designation;

    @Column(length = 128)
    private String department;

    @ManyToOne
    @JoinColumn(name = "band_id")
    private EmployeeOrgBand band;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private EmploymentType employmentType;

    @ManyToOne
    @JoinColumn(name = "reporting_manager_id")
    private User reportingManager;

    // Getters and Setters
    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public User getReportingManager() {
        return reportingManager;
    }
    
    public void setReportingManager(User reportingManager) {
        this.reportingManager = reportingManager;
    }

    public EmployeeOrgBand getBand() {
        return band;
    }

    public void setBand(EmployeeOrgBand band) {
        this.band = band;
    }

    // Default constructor
    public Employee() {
        // Default constructor
    }
    
    // Constructor with all fields
    public Employee(String username, String email, String passwordHash, String firstName, String lastName, 
                   boolean active, String employeeCode, String designation, String department, 
                   LocalDate hireDate, EmploymentType employmentType) {
        super(username, email, passwordHash, firstName, lastName, active);
        this.employeeCode = employeeCode;
        this.designation = designation;
        this.department = department;
        this.hireDate = hireDate;
        this.employmentType = employmentType;
    }
    
    // Helper methods
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", employeeCode='" + employeeCode + '\'' +
                ", designation='" + designation + '\'' +
                '}';
    }
}
