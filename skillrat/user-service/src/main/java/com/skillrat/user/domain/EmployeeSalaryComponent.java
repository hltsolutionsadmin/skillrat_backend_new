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
@Table(name = "employee_salary_components")
public class EmployeeSalaryComponent extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "salary_structure_id", nullable = false)
    private EmployeeSalaryStructure salaryStructure;

    @OneToOne
    @JoinColumn(name = "component_id", nullable = false)
    private SalaryComponent component;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
}
