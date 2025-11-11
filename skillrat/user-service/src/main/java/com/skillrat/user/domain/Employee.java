package com.skillrat.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;
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

    @Column(length = 128)
    private String designation;

    @Column(length = 128)
    private String department;

    private LocalDate hireDate;
}
