package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee_payslips", uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id","month","year"}))
public class EmployeePayslip extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalEarnings;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductions;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal unpaidLeaveDeduction = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;

    @Column(nullable = false)
    private OffsetDateTime generatedAt;

    @PrePersist
    public void prePersist() {
        generatedAt = OffsetDateTime.now();
    }
}
