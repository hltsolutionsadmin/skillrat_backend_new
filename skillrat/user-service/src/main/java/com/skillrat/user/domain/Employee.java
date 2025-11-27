package com.skillrat.user.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("EMPLOYEE")
@Getter
@Setter
@NoArgsConstructor
public class Employee extends User {

    // In SINGLE_TABLE inheritance this column exists for all rows, so it must be nullable
    // to allow base User inserts where employeeCode is not applicable.
    @Column(nullable = true, unique = true, length = 64)
    private String employeeCode;

    @ManyToOne
    @JoinColumn(name = "designation_id")
    @JsonBackReference
    private Designation designation;

    @Column(length = 128)
    private String department;

    @jakarta.persistence.OneToOne
    @JoinColumn(name = "band_id")
    private EmployeeOrgBand band;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private EmploymentType employmentType;

    @ManyToOne
    @JoinColumn(name = "reporting_manager_id")
    private User reportingManager;


    // Constructor with all fields
    public Employee(String username, String email, String passwordHash, String firstName, String lastName, 
                   boolean active, String employeeCode, String designation, String department, 
                   LocalDate hireDate, EmploymentType employmentType) {
        super(username, email, passwordHash, firstName, lastName, active);
        this.employeeCode = employeeCode;
        this.department = department;
        this.hireDate = hireDate;
        this.employmentType = employmentType;
    }
}
