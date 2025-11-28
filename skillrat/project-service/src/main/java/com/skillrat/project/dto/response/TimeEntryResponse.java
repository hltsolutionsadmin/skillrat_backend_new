package com.skillrat.project.dto.response;

import com.skillrat.project.domain.TimeEntryStatus;
import com.skillrat.project.domain.TimeEntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryResponse {
    private UUID id;
    private UUID projectId;
    private String projectName;
    private UUID wbsId;
    private String wbsName;
    private UUID memberId;
    private UUID employeeId;
    private LocalDate workDate;
    private BigDecimal hours;
    private String notes;
    private TimeEntryType entryType;
    private TimeEntryStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
