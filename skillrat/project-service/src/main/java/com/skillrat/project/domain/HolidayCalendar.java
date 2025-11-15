package com.skillrat.project.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "holiday_calendar")
@Getter
@Setter
@NoArgsConstructor
public class HolidayCalendar extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private IndiaCity city;

    // Optional: calendar scoped to a B2B unit
    private UUID b2bUnitId;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<HolidayDay> days = new ArrayList<>();
}
