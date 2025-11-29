package com.skillrat.project.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeEntryCreateRequest {
   private UUID projectId;
   private UUID wbsId;
   private UUID memberId;
   private UUID employeeId;
   private LocalDate workDate;
   private BigDecimal hours;
   String notes;
}
