package com.skillrat.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@DiscriminatorValue("designation")
public class Designation {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private UUID b2bUnitId;

    @ManyToOne
    @JoinColumn(name = "band_id")
    private EmployeeOrgBand band;
}
