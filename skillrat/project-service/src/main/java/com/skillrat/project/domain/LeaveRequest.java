package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_request")
@Getter
@Setter
@NoArgsConstructor
public class LeaveRequest extends BaseEntity {

    @Column(nullable = false)
    private UUID employeeId;

    @Column(nullable = false)
    private UUID b2bUnitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LeaveType type = LeaveType.OTHER;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal perDayHours = new BigDecimal("8.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LeaveStatus status = LeaveStatus.REQUESTED;

    private UUID approverId;

    private java.time.Instant decisionAt;

    @Column(length = 300)
    private String note;
}
