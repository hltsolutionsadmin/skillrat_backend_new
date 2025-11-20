package com.skillrat.user.dto;

import com.skillrat.user.domain.LeaveStatus;
import com.skillrat.user.domain.LeaveType;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveDTO {
    private UUID id;
    private UUID employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;

}
