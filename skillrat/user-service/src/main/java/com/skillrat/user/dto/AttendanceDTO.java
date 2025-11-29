package com.skillrat.user.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.skillrat.user.domain.EmployeeAttendance.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceDTO {
        private UUID id;
        private UUID employeeId;
        private LocalDate date;
        private Status status;
        private int month;
        private int year;
}
