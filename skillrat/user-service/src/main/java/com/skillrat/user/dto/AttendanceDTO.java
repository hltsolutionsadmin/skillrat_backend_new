package com.skillrat.user.dto;

import com.skillrat.user.domain.EmployeeAttendance.Status;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceDTO {
        private UUID id;
        private UUID employeeId;
        private LocalDate date;
        private Status status;
        private int month;
        private int year;
}
