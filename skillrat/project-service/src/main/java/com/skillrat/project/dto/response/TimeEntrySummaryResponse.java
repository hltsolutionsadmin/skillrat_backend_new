package com.skillrat.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntrySummaryResponse {
    private UUID employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalHours;
    private int workingDays;
    private int holidays;
    private int leaves;
    private Map<String, BigDecimal> hoursByProject;
    private List<DailySummary> dailySummaries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySummary {
        private LocalDate date;
        private String dayOfWeek;
        private boolean isHoliday;
        private boolean isWeekend;
        private boolean isLeave;
        private BigDecimal totalHours;
        private List<ProjectHours> projectHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectHours {
        private UUID projectId;
        private String projectName;
        private String taskName;
        private BigDecimal hours;
    }
}
