package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leave_balance")
@Getter
@Setter
@NoArgsConstructor
public class LeaveBalance extends BaseEntity {

    @Column(nullable = false)
    private UUID employeeId;

    @Column(nullable = false)
    private UUID b2bUnitId;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private LeaveType type = LeaveType.OTHER;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal allocated = BigDecimal.ZERO;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal consumed = BigDecimal.ZERO;
}
