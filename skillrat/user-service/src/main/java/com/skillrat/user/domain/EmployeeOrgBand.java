package com.skillrat.user.domain;

import com.skillrat.user.organisation.domain.B2BUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "employee_band")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeOrgBand {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "experience_min")
    private Integer experienceMin;

    @Column(name = "experience_max")
    private Integer experienceMax;

    @Column
    private Double salary;

    @OneToOne
    @JoinColumn(name = "b2b_unit_id", nullable = false)
    private B2BUnit b2bUnit;
}
