package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee_attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"employeeId", "date"}))
public class EmployeeAttendance extends BaseEntity {

    @Column(nullable = false)
    private UUID employeeId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    public enum Status { PRESENT, ABSENT, HALF_DAY, WEEK_OFF, HOLIDAY }
}
