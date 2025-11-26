package com.skillrat.user.domain;

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

    @Enumerated(EnumType.STRING) // store enum as string in DB
    @Column(nullable = false)
    private EmployeeBand name;

    @Column(nullable = false)
    private String b2bUnitId;
}
