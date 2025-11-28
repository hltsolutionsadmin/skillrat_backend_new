package com.skillrat.project.dto.request;

import com.skillrat.project.domain.TimeEntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TimeEntryRequest {
    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotNull(message = "WBS ID is required")
    private UUID wbsId;

    @NotNull(message = "Project member ID is required")
    private UUID memberId;

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    @NotNull(message = "Hours are required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Hours must be greater than 0")
    @Digits(integer = 5, fraction = 2, message = "Hours must have up to 2 decimal places")
    private BigDecimal hours;

    private String notes;

    private TimeEntryType entryType = TimeEntryType.WORK;
}
