package com.skillrat.user.dto;

import com.skillrat.user.domain.EmploymentType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryDto {
    private UUID id;
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
}
