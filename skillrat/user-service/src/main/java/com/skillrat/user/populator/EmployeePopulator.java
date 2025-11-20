package com.skillrat.user.populator;

import com.skillrat.user.domain.Employee;
import com.skillrat.user.dto.EmployeeDetailsDto;
import com.skillrat.user.dto.EmployeeSummaryDto;
import com.skillrat.user.dto.UserBriefDto;
import org.springframework.stereotype.Component;

@Component
public class EmployeePopulator {
    public EmployeeSummaryDto toSummary(Employee e) {
        if (e == null) return null;
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

    public EmployeeDetailsDto toDetails(Employee e) {
        if (e == null) return null;
        UserBriefDto manager = null;
        if (e.getReportingManager() != null) {
            manager = new UserBriefDto(
                    e.getReportingManager().getId(),
                    e.getReportingManager().getFirstName(),
                    e.getReportingManager().getLastName()
            );
        }
        return new EmployeeDetailsDto(
                e.getId(),
                e.getB2bUnitId(),
                e.getFirstName(),
                e.getLastName(),
                e.getEmail(),
                e.getMobile(),
                e.getEmployeeCode(),
                e.getDesignation(),
                e.getDepartment(),
                e.getHireDate(),
                e.getEmploymentType(),
                manager,
                e.isActive()
        );
    }
}
