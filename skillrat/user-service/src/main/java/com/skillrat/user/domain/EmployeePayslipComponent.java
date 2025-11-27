package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee_payslip_components")
public class EmployeePayslipComponent extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "payslip_id", nullable = false)
    private EmployeePayslip payslip;

    @Column(nullable = false)
    private String componentName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalaryComponent.ComponentType componentType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
}
