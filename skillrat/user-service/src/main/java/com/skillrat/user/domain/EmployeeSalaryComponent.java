package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee_salary_components")
public class EmployeeSalaryComponent extends BaseEntity {

    @Column(nullable = false)
    private UUID salaryStructureId;

    @Column(nullable = false)
    private UUID componentId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
}
