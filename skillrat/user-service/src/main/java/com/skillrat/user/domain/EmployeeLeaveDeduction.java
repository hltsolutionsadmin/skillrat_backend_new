package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import com.skillrat.user.organisation.domain.B2BUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "employee_leave_deduction")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeLeaveDeduction extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @OneToOne
    @JoinColumn(name = "b2b_unit_id", nullable = false)
    private B2BUnit b2bUnit;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private BigDecimal unpaidLeaveDays = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDeduction = BigDecimal.ZERO;
}
